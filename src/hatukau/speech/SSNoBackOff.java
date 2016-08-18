package hatukau.speech;

import java.io.File;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

public final class SSNoBackOff {

	private static long totalWritten;
	private static boolean debug = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			perform(args[0], args[1]);
			System.exit(0);
		}
		d("Usage: java. hatukau.speech.SpectralSubstraction src.wav tgt.wav");
		System.exit(-1);
	}

	// %function [NoiseFlag, SpeechFlag, NoiseCounter,
	// Dist]=vad(signal,noise,NoiseCounter,NoiseMargin,Hangover)
	/**
	 * 
	 * @param signal
	 * @param noise
	 * @param NoiseCounter
	 *            is inout an array variable of size 1, noise counter
	 * @param NoiseMargin
	 * @param Hangover
	 * @return
	 */
	public static VADInfo vad(final float[] signal, final float[] noise,
			final int[] NoiseCounter, final int NoiseMargin,
			final int Hangover, boolean debug) {

		VADInfo info = new VADInfo();

		d("vad NoiseCounter=" + NoiseCounter[0] + " NOiseMargin=" + NoiseMargin
				+ " Hangover=" + Hangover + "\n");

		float[] Dist = SpectralDist(signal, noise, noise.length);
		if (debug)
			darr("Dist", Dist, 0, 3);
		min2Zero(Dist);
		info.SpectralDist = mean(Dist);
		if (info.SpectralDist < NoiseMargin) {
			info.NoiseFlag = true;
			NoiseCounter[0]++;
		} else {
			info.NoiseFlag = false;
			NoiseCounter[0] = 0;
		}

		// % Detect noise only periods and attenuate the signal
		if (NoiseCounter[0] > Hangover) {
			info.SpeechFlag = false;
		} else {
			info.SpeechFlag = true;
		}

		return info;

	}

	public static float mean(final float[] vector) {
		float sum = 0;
		for (float f : vector) {
			sum += f;
		}

		return sum / vector.length;

	}

	/** find negative values and roundup to zero */
	public static void min2Zero(final float[] vector) {
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] < 0)
				vector[i] = 0;
		}
	}

	public static float[] SpectralDist(float[] signal_abs, float[] noise_abs,
			int len) {
		float[] ret = new float[len];
		for (int i = 0; i < len; i++) {
			// ssboll75.m SpectralDist= 20*(log10(signal)-log10(noise));

			ret[i] = (float) (20 * (Math.log10(signal_abs[i]) - Math
					.log10(noise_abs[i])));
		}

		return ret;
	}

	public static void copyVector(final float[] src, final float[] tgt, int len) {
		for (int i = 0; i < len; i++) {
			tgt[i] = src[i];
		}

	}

	public static void zeroVector(final float[] v, int len) {
		for (int i = 0; i < len; i++) {
			v[i] = 0;
		}

	}

	public static void zeroVector(final double[] v, int len) {
		for (int i = 0; i < len; i++) {
			v[i] = 0;
		}

	}

	/**
	 * 
	 * substract src from substractor and put in target
	 */
	public static void substract(final float[] src, final float[] substractor,
			final float[] target) {
		for (int i = 0; i < src.length; i++) {
			target[i] = src[i] - substractor[i];
		}

	}

	/**
	 * add src to tgt
	 * 
	 * @param src
	 * @param tgt
	 * @param target
	 */
	public static void add(final float[] src, final float[] tgt,
			final float[] target) {
		for (int i = 0; i < src.length; i++) {
			target[i] = src[i] + tgt[i];
		}

	}

	// OverlapAdd2(XNEW,yphase,windowLen,ShiftLen);
	public static void OverlapAdd2(final float[] XNEW, final float[] yphase,
			int windowLen, int ShiftLen, final float[] output) {

	}

	public static void d(String s) {
		if (!debug)
			return;
		System.out.print("SSUB: " + s);
	}

	public static void d(String s, VADInfo info) {
		d(s + " VADInfo, NF=" + info.NoiseFlag + " SF=" + info.SpeechFlag
				+ " NC=" + info.NoiseCounter + "  SDist=" + info.SpectralDist);

	}

	private static void darr(double[] wBuffer, int max) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		for (i = 0; i < 2; i++) {
			pr += (i) + ": " + wBuffer[i] * 1000 + "\n";
		}

		for (i = 160; i < 162; i++) {
			pr += (i) + ": " + wBuffer[i] * 1000 + "\n";
		}

		for (i = 398; i < 400; i++) {
			pr += (i) + ": " + wBuffer[i] * 1000 + "\n";
		}

		d(pr);
	}

	private static void darr(float[] wBuffer, int start, int len) {
		darr("", wBuffer, start, len);

	}

	private static void darr(String left, float[] wBuffer, int start, int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		for (i = start; i < start + len; i++) {
			pr += left + ": " + (i) + ": " + wBuffer[i] + "\n";
		}

		d(pr);
	}

	private static void darrvrtf(String left, float[] wBuffer, int start,
			int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		pr += " " + left + " " + start + "->" + (start + len - 1) + ": ";
		for (i = start; i < start + len; i++) {
			pr += wBuffer[i] + " ";
		}
		pr += "\n";
		d(pr);
	}

	private static void darrvrt(String left, float[] wBuffer, int start, int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		pr += " " + left + " " + start + "->" + (start + len - 1) + ": ";
		d(pr);
		for (i = start; i < start + len; i++) {
			// pr += wBuffer[i] +" " ;
			System.out.printf("%1.5f ", wBuffer[i]);
		}
		System.out.printf("\n");
		// d(pr);
		// d(pr);
	}

	private static void darrvrt(String left, double[] wBuffer, int start,
			int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		pr += " " + left + " " + start + "->" + (start + len - 1) + ": ";
		d(pr);
		for (i = start; i < start + len; i++) {
			// pr += wBuffer[i] +" " ;
			System.out.printf("%1.5f ", wBuffer[i]);
		}
		System.out.printf("\n");
		// d(pr);
		// d(pr);
	}

	private static void darr(String left, double[] wBuffer, int start, int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		for (i = start; i < start + len; i++) {
			pr += left + ": " + (i) + ": " + wBuffer[i] + "\n";
		}

		d(pr);
	}

	public static void dfft(final float[] x, int start, int len) {
		// TODO Auto-generated method stub
		// d("dfft " + x.length + " " + start + " " + len);
		int i = 0;
		String pr = "";
		if ((2 * start) > (x.length - 1) | i < 0) {
			d("error dfft [" + 2 * start + "]");
			return;
		}
		for (i = 2 * start; i < 2 * (start + len); i += 2) {
			// d(i+"");
			pr += ((i / 2)) + ": " + (x[i]) + "  " + (x[i + 1]) + "i";
			pr += "\n";
		}
		if (pr != "")
			d(pr);
	}

	public static void perform(String ssrc, String stgt) {
		try {
			debug = true;
			// Open the wav file specified as the first argument
			NewWavFile wavFile = NewWavFile.openWavFile(new File(ssrc));

			// Display information about the wav file
			wavFile.display();
			long numFrames = wavFile.getNumFrames();
			long sampleRate = wavFile.getSampleRate();
			int channels = wavFile.getNumChannels();
			int validBits = wavFile.getValidBits();

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();
			int W = (int) (0.025 * (double) sampleRate);
			double SP = 0.4;
			NewWavFile nWavFile = NewWavFile.newWavFile(new File(stgt),
					channels, numFrames, validBits, sampleRate);
			// d("window W= " + W);
			HammingWindow hw = new HammingWindow();
			float[] hammings = hw.generateCurve(W);
			/*
			 * d("hammings.length= " + hammings.length); dfft(hammings, 0, 1);
			 * dfft(hammings, 33, 1); dfft(hammings, 199, 1);
			 */
			// dfft(hammings,300,1);

			double IS = 0.25;
			double fs = sampleRate;
			int NIS = (int) ((IS * fs - W) / (SP * W) + 1);
			// d("NIS= " + NIS);
			// double Gamma = 1;
			// Create a buffer of 100 frames

			int wSP = (int) (SP * W);
			// d("window shift wSP= " + wSP);
			double[] tmpwsp = new double[wSP * numChannels];
			double[] WBuffer = new double[W], afterwsBuffer = new double[W
					- wSP];
			int FreqResol = (int) (W / 2) + 1;
			// d("FreqResol= " + FreqResol);
			float[] WBufferHw = new float[2 * W];
			double[] OverLapper = new double[W];
			float[] Y = new float[W], YPhase = new float[W], YPhase_last_1 = new float[FreqResol];
			float Beta = (float) 0.03;
			// float[][] Y_NIS = new float[NIS][FreqResol];
			// float[][] YPhase_NIS = new float[NIS][FreqResol];
			float[] N = new float[FreqResol];
			float[] NIS_sum = new float[FreqResol];
			float[] YS = new float[FreqResol];
			float[] Y_last_1 = new float[FreqResol];
			float[] Y_last_2 = new float[FreqResol]; // next
			float[] YS_last_1 = new float[FreqResol];
			float[] YS_last_2 = new float[FreqResol]; // next
			float[] YS_last_3 = new float[FreqResol]; // next
			float[] D = new float[FreqResol];
			float[] NRM = new float[FreqResol];
			float[] X = new float[FreqResol];
			double[] output = new double[W];
			int NoiseLength = 9, NoiseMargin = 3, HangOver = 8, NoiseCounter[] = new int[] { 0 };
			double min = Double.MAX_VALUE;
			FFT ffter = new FFT(W);
			double max = Double.MIN_VALUE;
			boolean NIS_RETRACE = false;
			int NIS_RETRACE_FRAME_NO = 0;
			int TotalYFrameRead = 0, frameProcessed = 0;
			int framesRead = wavFile.readFrames(WBuffer, wSP); // read a frame
			d("read success " + framesRead + "\n");
			int totalFramesRead = framesRead;
			boolean lastSpeechFlag = false;
			boolean lastNoiseFlag = true;
			boolean just_copy = false;
			boolean error_read = false;
			String outYS = "";
			int N_MTLB = 2;
			totalWritten = 0;
			// nWavFile.writeFrames(WBuffer, framesRead);
			do {
				framesRead = 0;
				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS) {
					d("\nFrameNumber " + TotalYFrameRead + "\n");
					TotalYFrameRead++;

					if (TotalYFrameRead == 1) {
						framesRead = 0;
						framesRead = wavFile.readFrames(afterwsBuffer, W - wSP); // read
																					// a
						System.arraycopy(afterwsBuffer, 0, WBuffer, W
								- framesRead, framesRead);
						// frame
					}

					if (TotalYFrameRead > 1) {
						framesRead = wavFile.readFrames(tmpwsp, wSP);
						if (framesRead < (wSP)) {
							// d("error at frameNo " + TotalYFrameRead + " "
							// + framesRead + " < " + (W - wSP) + "");
							error_read = true;
							// break;
						}

						System.arraycopy(tmpwsp, 0, WBuffer, W - framesRead,
								framesRead);
					}
					// d("read success "+framesRead+"\n");
					darrvrt("v0", WBuffer, 0, 10);
					darrvrt("v1", WBuffer, 390, 10);

					for (int s = 0; s < W; s++) {
						if (WBuffer[s] > max)
							max = WBuffer[s];
						if (WBuffer[s] < min)
							min = WBuffer[s];
					}

					totalFramesRead += framesRead;
					// d("totalFramesRead "+totalFramesRead+"\n");

					int j = 0;
					for (int i = 0; i < WBuffer.length; i++) {

						WBufferHw[2 * i] = (float) WBuffer[i] * hammings[j];
						WBufferHw[2 * i + 1] = 0;
						j++;
					}
					// darr("WBuffer",WBuffer,0,10);
					ffter.complexForwardTransform(WBufferHw);
					// for (int i = 0; i < FreqResol; i++) {
					// Y[i]=0;
					// }
					ffter.powerAndPhaseFromFFT(WBufferHw, Y, YPhase);
					// ffter.powerPhaseFFT(WBufferHw, Y, YPhase);
					darrvrt("Y", Y, 0, 10);
					darrvrt("Y", Y, 390, 10);
					for (int i = 0; i < FreqResol; i++) {
						if (Y[i] < 0) {
							d("error at TotalYFrameRead=" + TotalYFrameRead
									+ " i=" + i + " Y[i]=" + Y[i] + " Y=" + Y
									+ "\n");
							break;

						}
					}

				} else {

					if (NIS_RETRACE_FRAME_NO < NIS) {
						// SSNoBackOff.copyVector(
						// Y_NIS[NIS_RETRACE_FRAME_NO], Y, FreqResol);
						NIS_RETRACE_FRAME_NO++;
						if (NIS_RETRACE_FRAME_NO == NIS)
							NIS_RETRACE = false;
					}

				}

				if (TotalYFrameRead >= 1 & TotalYFrameRead <= NIS
						& NIS_RETRACE_FRAME_NO < NIS) {
					// d("summing NIS nosie frames from "+TotalYFrameRead+"\n");

					/*
					 * for (int i = 0; i < FreqResol; i++) { if (TotalYFrameRead
					 * == 1) NIS_sum[i] += Y[i]; else { NIS_sum[i]
					 * *=(TotalYFrameRead-1); NIS_sum[i] += Y[i]; NIS_sum[i]
					 * /=TotalYFrameRead;
					 * 
					 * }
					 * 
					 * }
					 */
					// copying phase
					for (int i = 0; i < FreqResol; i++) {
						NIS_sum[i] += Y[i];

						// if(TotalYFrameRead <= NIS) Y[i]=0;
						// Y_NIS[TotalYFrameRead - 1][i] = Y[i];
						if (Y[i] < 0) {
							// d("error at TotalYFrameRead=" + TotalYFrameRead
							// + "i=" + i + " Y[i]=" + Y[i]);
						}
						// YPhase_NIS[TotalYFrameRead - 1][i] = YPhase[i];
					}

					// darr("NIS_SUM",NIS_sum,1,3);

					if (TotalYFrameRead == NIS & !NIS_RETRACE) {
						NIS_RETRACE = true; // order to recompute
						// d("compute mean NIS");
						/*
						 * for (int i = 0; i < FreqResol; i++) { // NIS_sum[i] =
						 * 0; } for (int i = 0; i < NIS; i++) { // darr("" + i,
						 * Y_NIS[i], 0, 3); for (int j = 0; j < FreqResol; j++)
						 * { NIS_sum[j] += Y_NIS[i][j]; } }
						 */
						// darr(NIS_sum, 0, 1);
						for (int i = 0; i < FreqResol; i++) {
							N[i] = NIS_sum[i] / NIS;
						}
						// frameProcessed = 1;// begin processing
					} else {
						for (int i = 0; i < FreqResol; i++) {
							N[i] = NIS_sum[i] / TotalYFrameRead;
						}
					}

				}
				NIS_RETRACE = false;
				if (frameProcessed > 0 | true) {// AFTER NIS

					if (frameProcessed > 2 | true) {// begin averaging ...
						// if (frameProcessed == 1)
						// d("Begin magnitude average");
						/** Magnitude Average finding YS **/

						if (frameProcessed > 5 | true) {
							SSNoBackOff.copyVector(YS_last_3, YS_last_2,
									FreqResol);
						}

						if (frameProcessed > 4 | true) {
							SSNoBackOff.copyVector(YS_last_1, YS_last_2,
									FreqResol);
						}
						if (frameProcessed > 3 | true) {
							SSNoBackOff.copyVector(YS, YS_last_1, FreqResol);

						}

						for (int i = 0; i < FreqResol; i++) {
							YS_last_1[i] = (Y[i] + Y_last_1[i] + Y_last_2[i]) / 3;

						}
						darrvrt("Y_last_1", Y_last_1, 0, 10);
						darrvrt("Y_last_2", Y_last_2, 0, 10);
						darrvrt("YS_last_1", YS_last_1, 0, 10);

						if (frameProcessed > 7 | true) {
							boolean debug = false;

							VADInfo info = SSNoBackOff.vad(Y_last_1, N,
									NoiseCounter, NoiseMargin, HangOver, debug);
							info.NoiseCounter = NoiseCounter[0];

							SSNoBackOff.substract(YS_last_1, N, D);
							darrvrt("N", N, 0, 10);
							darrvrt("D", D, 0, 10);

							if (!info.SpeechFlag) {
								// smoothing noise
								for (int i = 0; i < FreqResol; i++) {
									N[i] = (NoiseLength * N[i] + Y_last_1[i])
											/ (NoiseLength + 1);

								}

								// update maximum noise residue
								for (int i = 0; i < FreqResol; i++) {
									if (NRM[i] < D[i])
										NRM[i] = D[i];

									X[i] = Beta * Y_last_1[i];
								}

							} else {

								for (int j2 = 0; j2 < D.length; j2++) {
									if (D[j2] < NRM[j2]) {
										if (D[j2] > YS_last_2[j2] - N[j2]) {
											// d("chto "+(YS_last_3[j2] -
											// N[j2]));
											D[j2] = YS_last_2[j2] - N[j2];
										}
										if (D[j2] > YS[j2] - N[j2])
											D[j2] = YS[j2] - N[j2];
									}
								}
								// X(:,i)=max(D,0);
								SSNoBackOff.copyVector(D, X, FreqResol);
								darrvrt("D3", D, 0, 10);
								SSNoBackOff.min2Zero(X);
								// darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB)+
								// " x_at_sf_1",X,0,3);
							}

							darrvrt("D2", D, 0, 10);
							darrvrt("X", X, 0, 10);
							darrvrt("X4", X, 190, 11);
							WBufferHw[0] = X[0];
							WBufferHw[1] = 0;

							int j = 1;
							for (int i = 2; i < 2 * FreqResol; i += 2) {
								WBufferHw[i] = (float) (X[j] * Math
										.cos(-YPhase_last_1[j]));
								WBufferHw[i + 1] = (float) (X[j] * Math
										.sin(YPhase_last_1[j]));

								if (i < 2 * FreqResol - 2) {
									// data2[data2.length - j-1] = i;
									WBufferHw[WBufferHw.length - 2 * j] = WBufferHw[i];
									WBufferHw[WBufferHw.length - 2 * j + 1] = -WBufferHw[i + 1];
								}
								j++;
							}

							darrvrt("YPhase_last_1", YPhase_last_1, 0, 10);
							darrvrt("wbufferhw", WBufferHw, 0, 10);
							darrvrt("wbufferhw", WBufferHw, 393, 10);
							darrvrt("wbufferhw", WBufferHw, 790, 10);
							ffter.complexBackwardsTransform(WBufferHw);

							j = 0;
							for (int i = 0; i < W; i++) {
								output[i] = WBufferHw[j];
								j += 2;
							}
							darrvrt("output", output, 0, 10);
							for (int i = 0; i < W; i++) {
								output[i] += OverLapper[i];
							}

							SSNoBackOff.zeroVector(OverLapper, W);

							j = 0;
							for (int i = wSP; i < W; i++) {
								OverLapper[j] = output[i];
								j++;
							}

							// @TODO smoothing first
							if (TotalYFrameRead <= NIS)
								for (int i = 0; i < FreqResol; i++) {
									output[i] = 0;
								}

							if (frameProcessed == 0) {
								totalWritten += nWavFile.writeFrames(output, W
										- wSP);

							} else {
								if (framesRead < wSP) {
									totalWritten += nWavFile.writeFrames(
											output, framesRead);
								}

								totalWritten += nWavFile.writeFrames(output,
										wSP);
							}
							// d("totalWritten "+totalWritten);
							// SpectralSubstraction.OverlapAdd2(WBufferHw,
							// YPhase, W, wSP,
							// output);
						}// if frameProcessed > 3

					}// if frameProcessed > 2

					if (frameProcessed > 1 | true) {
						SSNoBackOff.copyVector(Y_last_1, Y_last_2, FreqResol);
					}
					// ok
					if (frameProcessed > 0 | true) {
						SSNoBackOff
								.copyVector(YPhase, YPhase_last_1, FreqResol);
						SSNoBackOff.copyVector(Y, Y_last_1, FreqResol);

						// vad
					}

					d("totalWritten " + totalWritten);
					d("totalRead- totalWritten "
							+ (totalFramesRead - totalWritten));
					frameProcessed++;

					if (framesRead < wSP) {
						break; // end of file
					}

				}// if frameProcessed >0

				// nWavFile.writeFrames(afterwsBuffer, framesRead);
				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS | true) {
					System.arraycopy(WBuffer, wSP, afterwsBuffer, 0, W - wSP);
					System.arraycopy(afterwsBuffer, 0, WBuffer, 0, W - wSP);
				}

			} while (framesRead > 0);
			// Close the wavFile
			wavFile.close();
			nWavFile.display();
			nWavFile.close();

			// Output the minimum and maximum value
			// System.out.printf("Min: %f, Max: %f\n", min, max);

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static byte[] long2Bytes(long val, int bytesPerSample) {
		int bufferPointer = 0;
		byte[] buffer = new byte[bytesPerSample];
		for (int b = 0; b < bytesPerSample; b++) {

			buffer[bufferPointer] = (byte) (val & 0xFF);
			val >>= 8;
			bufferPointer++;
		}

		return buffer;
	}

	public static long bytes2Long(final byte[] buffer, final int bytesPerSample) {
		long val = 0;
		int bufferPointer = 0;
		for (int b = 0; b < bytesPerSample; b++) {

			int v = buffer[bufferPointer];
			if (b < bytesPerSample - 1 || bytesPerSample == 1)
				v &= 0xFF;
			val += v << (b * 8);

			bufferPointer++;
		}

		return val;
	}

}
