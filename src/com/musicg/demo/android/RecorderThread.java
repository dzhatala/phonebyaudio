/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * musicg api in Google Code: http://code.google.com/p/musicg/
 * Android Application in Google Play: https://play.google.com/store/apps/details?id=com.whistleapp
 * 
 */

package com.musicg.demo.android;

import hatukau.speech.FrameData;
import hatukau.speech.WriteExample;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class RecorderThread extends Thread {

	private AudioRecord audioRecord;
	private boolean isRecording;
	// private int channelConfiguration =
	// AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	// private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private int sampleRate = 16000;

	// private int sampleRate = 8000;
	// private int frameByteSize = 4096; // for 1024 fft size (16bit sample
	// size)
	// byte[] buffer=null;
	private RecorderListener listener;

	//private boolean useAutomaticGain = false;
	double gain = 1.0;

	double fasterEndVFactor = 1.3; // exponential decay
									// shorten

	// will be multiplied with threshold
	// tail ..
	private enum PHASE_TYPE {
		PHASE_SIL, PHASE_V
	};

	private PHASE_TYPE currPhase = PHASE_TYPE.PHASE_SIL;

	// use by count average
	/** maximum length to recognize in seconds **/
	private double maxSecLength = 7.00;
	private double intervalDetection = 1.2; //
	private double samplesConsidered = 1; // 0 can't be divided so 1;
	private double sumConsidered = 0;// absolute total
	// head to be removed while
	private SildetInfo HEAD = new SildetInfo();
	long lastPrint = -1;
	double INT_1 = 0.25; // miliseconds debugs
	double silDetInterval = 0.15;
	private boolean useSilDet = false;
	private boolean voicePhase;

	private double silVTreshold = 13.0;
	private double currAverage = 0; // current average;

	private long voiceMinTime = 1000; // minimal segment length (in ms) to
										// consider as voice
	private long voiceTime = 0; // curr time ..

	public RecorderThread() {
		int recBufSize = AudioRecord.getMinBufferSize(sampleRate,
				channelConfiguration, audioEncoding); // need to be larger than
														// size of a frame
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate, channelConfiguration, audioEncoding, recBufSize);
		// buffer = new byte[frameByteSize];
	}

	public AudioRecord getAudioRecord() {
		return audioRecord;
	}

	public boolean isRecording() {
		// return this.isAlive() && isRecording;
		return (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);
	}

	public void startRecording() {
		try {
			d(" startRecording ");
			HEAD.creationTime = System.currentTimeMillis();
			HEAD.numB = 1;// avoid 0/0
			HEAD.sumAb = 0;
			HEAD.last = HEAD;
			currAverage = 0;
			setSamplesConsidered(1);
			setSumConsidered(0);
			audioRecord.startRecording();
			isRecording = true;
			voiceTime = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setSsamplesConsider(int i) {
		// TODO Auto-generated method stub

	}

	public void stopRecording() {
		d("stopRecording");
		try {
			audioRecord.stop();
			isRecording = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getFrameBytes(FrameData data) {
		int ret = audioRecord.read(data.buffer, 0, data.buffer.length);

		if (ret <= 0)
			return -1;

		float sample = 0, ugs, lsb, msb = 0;
		double tot = 0;
		float doubles;
		for (int i = 0; i < ret; i += 2) {
			// sample = (short) ((data.buffer[i]) | data.buffer[i + 1] << 8);
			int x = (data.buffer[i] & 0xFF);
			x |= (data.buffer[i + 1] << 8);
			sample = (float) x;
			if (gain != 1.0) {

				sample *= gain;
				ugs = sample;
				if (sample >= 32767f) {
					data.buffer[i] = (byte) 0xFF;
					data.buffer[i + 1] = 0x7F;
				} else if (sample <= -32768f) {
					data.buffer[i] = 0x00;
					data.buffer[i + 2] = (byte) 0x80;
				} else {
					int s = (int) (0.5f + sample); // Here, dithering would be
													// more appropriate
					data.buffer[i] = (byte) (s & 0xFF);
					data.buffer[i + 1] = (byte) (s >> 8 & 0xFF);
				}
			}
			tot += Math.abs(sample);

		}

		SildetInfo info = new SildetInfo();
		info.numB = ret;
		info.sumAb = tot;
		info.data = new byte[ret];
		System.arraycopy(data.buffer, 0, info.data, 0, ret);
		setSamplesConsidered(getSamplesConsidered() + info.numB);
		setSumConsidered(getSumConsidered() + info.sumAb);
		info.creationTime = System.currentTimeMillis();
		HEAD.last.next = info;
		HEAD.last = info;

		// countAverage();
		setCurrAverage(getSumConsidered() / getSamplesConsidered() / 2);

		// no input

		long now = System.currentTimeMillis();

		long intv = now - lastPrint;
		double trueTS = getSilVTreshold();

		/** state changes **/
		if ((intv >= INT_1 * 1000 && currPhase == PHASE_TYPE.PHASE_SIL)
				|| (intv >= 3 * INT_1 * 1000 && currPhase == PHASE_TYPE.PHASE_V)) {
			// d("RT: state changes");
			if (currPhase == PHASE_TYPE.PHASE_V) {
				// trueTS=trueTS*1.7;
				trueTS = trueTS * fasterEndVFactor;
			}
			if (isUseSilDet()) {

				boolean checkB = currPhase == PHASE_TYPE.PHASE_V
						&& System.currentTimeMillis() - voiceTime >= (long) (maxSecLength * 1000);
				if (currPhase == PHASE_TYPE.PHASE_V)
					d("checkB=" + checkB + ", voice length= "
							+ (System.currentTimeMillis() - voiceTime));
				if (getCurrAverage() < trueTS || checkB) {
					// return -1;
					if (currPhase == PHASE_TYPE.PHASE_V) {

						currPhase = PHASE_TYPE.PHASE_SIL;
						long voiceLength = System.currentTimeMillis()
								- voiceTime;
						// d("voiceLength ="+((double)voiceLength/1000)+" s");
						if (voiceLength <= voiceMinTime) {
							if (listener != null)
								listener.voiceIsNoise();// this is not voice, as
														// predicted but noiese
							d("RecorderThread: voiceIsNoise()");
						} else {
							if (listener != null)
								listener.voiceEnd();

						}

						data.V2SIL = true;

					}
				} else {
					if (getCurrAverage() >= trueTS) {
						d("Threashold exceed " + currAverage + " > "
								+ silVTreshold + " truthresh " + trueTS
								+ " checkB " + checkB);
						if (currPhase == PHASE_TYPE.PHASE_SIL) {
							currPhase = PHASE_TYPE.PHASE_V;
							voiceTime = System.currentTimeMillis();
							if (listener != null)
								listener.voiceStart();
							data.SIL2V = true;
						}
					}
				}
			} // isUse...h
				// @TODO removed in production
			/*
			 * debug("################ AVG ABS: " + getCurrAverage() + ", intv="
			 * + intv+" numbytes="+ret);
			 */
			lastPrint = now;
			if (listener != null) {
				if (currPhase == PHASE_TYPE.PHASE_SIL & getCurrAverage() > 0) {
					Thread x = new Thread() {
						public void run() {
							listener.absValue(getCurrAverage());
						}
					};
					x.start();

				}
			}

		}

		/** moving on .... **/
		double trueInterval = intervalDetection * 1000;
		/* change this to remove tail .... */
		if (currPhase == PHASE_TYPE.PHASE_V) {
			trueInterval = trueInterval * 0.30;
		}

		if ((HEAD.last.creationTime - HEAD.creationTime) >= trueInterval) {

			// System.out.println("Removing ...");
			setSamplesConsidered(getSamplesConsidered() - HEAD.numB);
			setSumConsidered(getSumConsidered() - HEAD.sumAb);
			HEAD.next.last = HEAD.last;
			HEAD.data = null; // empty memory
			HEAD = HEAD.next;

			// HEAD.last=last;

		}

		return ret;
	}

	
	/**
	 * consume bytes
	 * 
	 * @param buffer
	 * @return
	 */
	public int getFrameBytesOld(FrameData data) {
		int ret = audioRecord.read(data.buffer, 0, data.buffer.length);

		if (ret <= 0)
			return -1;

		float sample = 0, ugs, lsb, msb = 0;
		double tot = 0;
		float doubles;
		for (int i = 0; i < ret; i += 2) {
			// sample = (short) ((data.buffer[i]) | data.buffer[i + 1] << 8);
			int x = (data.buffer[i] & 0xFF);
			x |= (data.buffer[i + 1] << 8);
			sample = (float) x;
			if (gain != 1.0) {

				sample *= gain;
				ugs = sample;
				if (sample >= 32767f) {
					data.buffer[i] = (byte) 0xFF;
					data.buffer[i + 1] = 0x7F;
				} else if (sample <= -32768f) {
					data.buffer[i] = 0x00;
					data.buffer[i + 2] = (byte) 0x80;
				} else {
					int s = (int) (0.5f + sample); // Here, dithering would be
													// more appropriate
					data.buffer[i] = (byte) (s & 0xFF);
					data.buffer[i + 1] = (byte) (s >> 8 & 0xFF);
				}
			}
			tot += Math.abs(sample);

		}

		SildetInfo info = new SildetInfo();
		info.numB = ret;
		info.sumAb = tot;
		info.data = new byte[ret];
		System.arraycopy(data.buffer, 0, info.data, 0, ret);
		setSamplesConsidered(getSamplesConsidered() + info.numB);
		setSumConsidered(getSumConsidered() + info.sumAb);
		info.creationTime = System.currentTimeMillis();
		HEAD.last.next = info;
		HEAD.last = info;

		// countAverage();
		setCurrAverage(getSumConsidered() / getSamplesConsidered() / 2);

		// no input

		long now = System.currentTimeMillis();

		long intv = now - lastPrint;
		double trueTS = getSilVTreshold();

		/** state changes **/
		if ((intv >= INT_1 * 1000 && currPhase == PHASE_TYPE.PHASE_SIL)
				|| (intv >= 3 * INT_1 * 1000 && currPhase == PHASE_TYPE.PHASE_V)) {
			// d("RT: state changes");
			if (currPhase == PHASE_TYPE.PHASE_V) {
				// trueTS=trueTS*1.7;
				trueTS = trueTS * fasterEndVFactor;
			}
			if (isUseSilDet()) {

				boolean checkB = currPhase == PHASE_TYPE.PHASE_V
						&& System.currentTimeMillis() - voiceTime >= (long) (maxSecLength * 1000);
				if (currPhase == PHASE_TYPE.PHASE_V)
					d("checkB=" + checkB + ", voice length= "
							+ (System.currentTimeMillis() - voiceTime));
				if (getCurrAverage() < trueTS || checkB) {
					// return -1;
					if (currPhase == PHASE_TYPE.PHASE_V) {

						currPhase = PHASE_TYPE.PHASE_SIL;
						long voiceLength = System.currentTimeMillis()
								- voiceTime;
						// d("voiceLength ="+((double)voiceLength/1000)+" s");
						if (voiceLength <= voiceMinTime) {
							if (listener != null)
								listener.voiceIsNoise();// this is not voice, as
														// predicted but noiese
							d("RecorderThread: voiceIsNoise()");
						} else {
							if (listener != null)
								listener.voiceEnd();

						}

						data.V2SIL = true;

					}
				} else {
					if (getCurrAverage() >= trueTS) {
						d("Threashold exceed " + currAverage + " > "
								+ silVTreshold + " truthresh " + trueTS
								+ " checkB " + checkB);
						if (currPhase == PHASE_TYPE.PHASE_SIL) {
							currPhase = PHASE_TYPE.PHASE_V;
							voiceTime = System.currentTimeMillis();
							if (listener != null)
								listener.voiceStart();
							data.SIL2V = true;
						}
					}
				}
			} // isUse...h
				// @TODO removed in production
			/*
			 * debug("################ AVG ABS: " + getCurrAverage() + ", intv="
			 * + intv+" numbytes="+ret);
			 */
			lastPrint = now;
			if (listener != null) {
				if (currPhase == PHASE_TYPE.PHASE_SIL & getCurrAverage() > 0) {
					Thread x = new Thread() {
						public void run() {
							listener.absValue(getCurrAverage());
						}
					};
					x.start();

				}
			}

		}

		/** moving on .... **/
		double trueInterval = intervalDetection * 1000;
		/* change this to remove tail .... */
		if (currPhase == PHASE_TYPE.PHASE_V) {
			trueInterval = trueInterval * 0.30;
		}

		if ((HEAD.last.creationTime - HEAD.creationTime) >= trueInterval) {

			// System.out.println("Removing ...");
			setSamplesConsidered(getSamplesConsidered() - HEAD.numB);
			setSumConsidered(getSumConsidered() - HEAD.sumAb);
			HEAD.next.last = HEAD.last;
			HEAD.data = null; // empty memory
			HEAD = HEAD.next;

			// HEAD.last=last;

		}

		return ret;
	}

	private void d(String string) {
		// TODO Auto-generated method stub
		System.out.println("RecorderThread " + string);
	}

	private void countAverage() {
		// TODO Auto-generated method stub

	}

	public void run() {
		startRecording();
	}

	public void setRecorderListener(RecorderListener l) {
		this.listener = l;
	}

	public double getIntervalDetection() {
		return intervalDetection;
	}

	public void setIntervalDetection(double intervalDetection) {
		this.intervalDetection = intervalDetection;
	}


	public boolean isUseSilDet() {
		return useSilDet;
	}

	public void setUseSilDet(boolean useSilDet) {
		d("setUseSilvDet " + useSilDet);
		this.useSilDet = useSilDet;
	}

	public double getSamplesConsidered() {
		return samplesConsidered;
	}

	public void setSamplesConsidered(double samplesConsidered) {
		this.samplesConsidered = samplesConsidered;
	}

	public double getSumConsidered() {
		return sumConsidered;
	}

	public void setSumConsidered(double sumConsidered) {
		this.sumConsidered = sumConsidered;
	}

	public double getCurrAverage() {
		return currAverage;
	}

	public void setCurrAverage(double currAverage) {
		this.currAverage = currAverage;
	}

	/**
	 * for debuggin info
	 * 
	 * @return
	 */

	public Object getDebugInfo() {
		return HEAD;
	}

	public SildetInfo getSilDetInfo() {
		return HEAD;
	}

	public double getSilVTreshold() {
		return silVTreshold;
	}

	public void setSilVTreshold(double silVTreshold) {
		this.silVTreshold = silVTreshold;
	}

	/**
	 * @return the voiceMinTime
	 */
	public long getVoiceMinTime() {
		return voiceMinTime;
	}

	/**
	 * @param voiceMinTime
	 *            the voiceMinTime to set
	 */
	public void setVoiceMinTime(long voiceMinTime) {
		this.voiceMinTime = voiceMinTime;
	}

	/**
	 * @return the maxSecLength
	 */
	public double getMaxSecLength() {
		return maxSecLength;
	}

	/**
	 * @param maxSecLength
	 *            the maxSecLength to set
	 */
	public void setMaxSecLength(double maxSecLength) {
		this.maxSecLength = maxSecLength;
	}

	public void debug(final String s) {
		// do not block main thread;
		Thread x = new Thread() {
			public void run() {
				System.out.println("RecorderThread.debug: " + s);
			}

		};
		x.start();

	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

}