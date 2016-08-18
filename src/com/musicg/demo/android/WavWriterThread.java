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

import hatukau.io.FileExplorer;
import hatukau.speech.AsyncronWavWriter;
import hatukau.speech.FrameData;

import java.util.LinkedList;

import com.musicg.api.WhistleApi;
import com.musicg.dsp.LinearInterpolation;
import com.musicg.wave.WaveHeader;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;

public class WavWriterThread extends Thread {

	private RecorderThread recorder;
	private WaveHeader waveHeader;
	private volatile Thread _thread;

	private WavWriterListener listener;
	AsyncronWavWriter writer = null;
	private int BUFF_SIZE = 8192 * 4;
	private boolean simulateOnly;
	private String wavName;
	byte[] last_buffer;
	int last_buffer_size;
	private SildetInfo backUp;
	
	private String SDCARD=Environment.getExternalStorageDirectory().getPath();;
	/**
	 * 
	 * @param recorder
	 * @param fname
	 *            null automatic filename will be created
	 */
	public WavWriterThread(RecorderThread recorder, String name) {
		this.recorder = recorder;
		setWavName(name);
		;
		AudioRecord audioRecord = recorder.getAudioRecord();

		int bitsPerSample = 0;
		if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
			bitsPerSample = 16;
		} else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
			bitsPerSample = 8;
		}
		

		int channel = 0;
		// whistle detection only supports mono channel
		if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
			channel = 1;
		}

		waveHeader = new WaveHeader();
		waveHeader.setChannels(channel);
		waveHeader.setBitsPerSample(bitsPerSample);
		waveHeader.setSampleRate(audioRecord.getSampleRate());
		// writer=new AsyncronWavWriter("/mnt/sdcard/cakadidi/test.wav",
		// waveHeader);
		// initFile();
	}

	public void initFile() {
		debug("WWTH: initFile()");
		String fname = getWavName() == null ?  SDCARD +"/cakadidi/trains/"
				+ FileExplorer.autoFileNamePrefix() + ".wav" : getWavName();
		writer = new AsyncronWavWriter(fname, waveHeader);
	}

	private void initBuffer() {
	}

	public void start() {
		debug("start");
		if(_thread==null)
		_thread = new Thread(this);
		/*try {
			if(writer==null)initFile();
			writer.startWrite();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		_thread.start();

	}

	/**
	 * write to new file
	 */
	public void reTarget() {
		debug("WWTH: retarget()" + getWavName());
		try {
			if (writer != null) {
				// writer.startWrite();;
				// writer.close();
				// writer=null;
			}
			initFile();
			writer.startWrite();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * stop and flush data;
	 * 
	 * @throws Exception
	 */
	public void stopWriting() throws Exception {
		debug("stop writing ");
		if (writer != null){
				
			debug("WWTH :stopW writer!=null is closed()");
			writer.close();
		}
		else {
			debug("WWTH :stopW writer already null");
			
			}
		writer = null;
		if (listener != null)
			listener.onClose("NULL");

	}

	public void stopThread() {

		_thread = null;
	}

	public void run() {
		try {
			/*
			 * byte[] buffer; initBuffer();
			 */
			FrameData data = new FrameData();

			data.buffer = new byte[getBUFF_SIZE()];
			last_buffer = new byte[getBUFF_SIZE()];
			last_buffer_size = 0;
			/**/

			Thread thisThread = Thread.currentThread();
			int ret = 0;
			boolean last_simulateOnly = isSimulateOnly();
			while (_thread == thisThread) {
				if(!recorder.isRecording()){
					debug("recorder not running .. sleeping ...");
					Thread.currentThread().sleep(1000);
					continue;
				}
				// detect sound
				System.arraycopy(data.buffer, 0, last_buffer, 0,
						data.buffer.length);
				last_buffer_size = ret;
				last_simulateOnly = isSimulateOnly();
				data.SIL2V=false;
				data.V2SIL=false;
				ret = recorder.getFrameBytes(data);
				if (data.V2SIL) {
					stopWriting();
					setSimulateOnly(true);
					data.buffer=new byte[getBUFF_SIZE()];
					last_buffer = new byte[getBUFF_SIZE()];
					
					continue;
					
				}
				
				
				
				// audio analyst
				if (ret > 0 && !isSimulateOnly() && writer != null) {

					if (last_simulateOnly)
						writeLastBuffer();
					writer.appendBytes(data.buffer, ret);

				}
				
				if (data.SIL2V) {
					data.buffer=new byte[getBUFF_SIZE()/4];
					last_buffer = new byte[getBUFF_SIZE()/4];
					
					continue;
				}
				
				
				
				

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * writing last buffer for sil det use only
	 * 
	 * @throws Exception
	 */
	public void writeLastBuffer() throws Exception {
		// if(writer!=null)
		// if(last_buffer_size>0)
		// writer.appendBytes(last_buffer, last_buffer_size);

		SildetInfo info = backUp;
		if (writer != null)
			while (info != null) {
				if (info.next == null)
					break;// not writing last
				writer.appendBytes(info.data, info.numB);
				info = info.next;

			}

	}

	public String getInfo() {
		if (writer != null)
			return writer.getWavInfo();
		return "NO INFO";
	}

	public int getBUFF_SIZE() {
		return BUFF_SIZE;
	}

	public void setBUFF_SIZE(int bUFF_SIZE) {
		BUFF_SIZE = bUFF_SIZE;
	}

	public boolean isSimulateOnly() {
		return simulateOnly;
	}

	public synchronized void setSimulateOnly(boolean simulateOnly) {
		debug("WWT  sSO()" + simulateOnly);
		this.simulateOnly = simulateOnly;
	}

	private void debug(String string) {
		System.out.println("WWTh " +string);
	}

	public String getWavName() {
		return wavName;
	}

	protected void setWavName(String wavName) {
		this.wavName = wavName;
	}

	public void setBackupBuffer(SildetInfo info) {
		this.backUp = info;
	}
}