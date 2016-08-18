package test;

import hatukau.io.NewAsynchronousWavWriter;
import hatukau.speech.AsyncronWavWriter;
import hatukau.speech.NewWavFile;
import hatukau.speech.SpectralSubstraction;
import hatukau.speech.VADInfo;
import hatukau.speech.WavFile;
import hatukau.speech.WavFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.musicg.wave.WaveHeader;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

public class Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//test004();
		 //test005();
		//test006();
		
		//test007();
		test008();
		//test009();
		//test010();
	}
	
	static void test009(){
		d("Main.test009");
		long testNum=-2323;
		int bytesPerSample=2;
		byte []bytes=SpectralSubstraction.long2Bytes(testNum,bytesPerSample);
		d("bytes.length="+bytes.length);
		long newNum=SpectralSubstraction.bytes2Long(bytes,bytesPerSample);
		d( testNum+ " vs " + newNum);
	}

	static void test010(){
		d("Main.test010 NewAsynWavWriter");
		WaveHeader waveHeader = new WaveHeader();
		waveHeader.setChannels(1);
		waveHeader.setBitsPerSample(16);
		waveHeader.setSampleRate(16000);
		// writer=new AsyncronWavWriter("/mnt/sdcard/cakadidi/te
		File fs= new File("E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original.wav");
		long[] bl = new long[1024];
		byte  []bb =new byte[2*bl.length];
		final int bytesPerSample=2;
		try {
			WavFile wavFile = WavFile.openWavFile(fs);
			int tot=wavFile.readFrames(bl,bl.length);;
			int x=0;
			for(int i=0;i<tot; i++){
				byte []wfret=SpectralSubstraction.long2Bytes(bl[i],bytesPerSample);
				for (int j=0;j<wfret.length;j++){
					bb[x]=wfret[j];
					x++;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WavFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	 
		String ori="E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\AWWTest.wav";
		try {
			NewAsynchronousWavWriter writer = new NewAsynchronousWavWriter(ori, waveHeader);
			writer.startWrite();
			for (int i = 0; i < 10; i++)
				writer.appendBytes(bb, bb.length);
			writer.stop();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SpectralSubstraction.perform(ori,"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outawwclean.wav");
	}
	static void test008(){
		d("Main.test008");
		WaveHeader waveHeader = new WaveHeader();
		waveHeader.setChannels(1);
		waveHeader.setBitsPerSample(16);
		waveHeader.setSampleRate(16000);
		// writer=new AsyncronWavWriter("/mnt/sdcard/cakadidi/te
		File fs= new File("E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original.wav");
		long[] bl = new long[1024*100];
		byte  []bb =new byte[2*bl.length];
		final int bytesPerSample=2;
		try {
			NewWavFile wavFile = NewWavFile.openWavFile(fs);
			int tot=wavFile.readFrames(bl,bl.length);;
			int x=0;
			for(int i=0;i<tot; i++){
				byte []wfret=SpectralSubstraction.long2Bytes(bl[i],bytesPerSample);
				for (int j=0;j<wfret.length;j++){
					bb[x]=wfret[j];
					x++;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WavFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		String ori="E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\AWWTest.wav";
		try {
			AsyncronWavWriter writer = new AsyncronWavWriter(ori, waveHeader);
			writer.startWrite();
			for (int i = 0; i < 3; i++)
				writer.appendBytes(bb, bb.length);
			writer.stop();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SpectralSubstraction.perform(ori,"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outawwclean.wav");
	}
	static void test007() {
		d("SS.perform() testing spectral subs");
		SpectralSubstraction
				.perform("E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original.wav",
						"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outbolljava.wav" );
		SpectralSubstraction
		.perform("E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original2.wav",
				"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outbolljava2.wav" );
	}

	static void test006() {
		d("Main.test006() testing spectral subs");
		ReadExample2
				.testRead2(new String[] {
						"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original.wav",
						"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outbolljava.wav" });
		ReadExample2
		.testRead2(new String[] {
				"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original2.wav",
				"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outbolljava2.wav" });
	}

	static void test005() {
		d("Test005");
		FFT myfft = new FFT(6);

		ReadExample1.d("COS 2*pi=" + Math.cos(2 * Math.PI));

		float[] data = new float[] { 2, 0, 3, 0, -1, 0, 4, 0, 5, 0, 6, 0 }, data2 = new float[12];
		int FreqResol = data.length / 4 + 1;
		d("FreqResol=" + FreqResol);
		float[] Y2 = new float[data.length / 2], YPhase2 = new float[data.length / 2], Y = new float[FreqResol], Ph = new float[FreqResol];
		d("Datas");
		ReadExample1.dfft(data, 0, 6);

		myfft.complexForwardTransform(data);
		ReadExample1.d("FFT Result");
		ReadExample1.dfft(data, 0, 6);

		myfft.powerAndPhaseFromFFT(data, Y2, YPhase2);

		for (int i = 0; i < Y2.length; i++) {
			if (i < FreqResol) {
				Y[i] = Y2[i];
				Ph[i] = YPhase2[i];

			}
			/*
			 * d("Y=" + Y2[i] + " YPh=" + YPhase2[i] + " -y*cos=" + (Y2[i] *
			 * Math.cos(-YPhase2[i])) + "  y*sin=" + (Y2[i] *
			 * Math.sin(YPhase2[i])));
			 */
		}

		data2[0] = Y[0];
		data2[1] = 0;

		int j = 1;
		for (int i = 2; i < 2 * FreqResol; i += 2) {
			// d ("Y"+Y[i] +" Ph="+Ph[j]);

			data2[i] = (float) (Y[j] * Math.cos(-Ph[j]));
			data2[i + 1] = (float) (Y[j] * Math.sin(Ph[j]));

			if (i < 2 * FreqResol - 2) {
				// data2[data2.length - j-1] = i;
				data2[data2.length - 2 * j] = data2[i];
				data2[data2.length - 2 * j + 1] = -data2[i + 1];
			}
			j++;
		}

		ReadExample1.d("power phase to X results");
		ReadExample1.dfft(data2, 0, 6);

		myfft.complexBackwardsTransform(data);
		ReadExample1.dfft(data, 0, 6);

	}

	static void test004() {
		d("Main.test004() testing spectral subs");
		ReadExample1
				.testRead1(new String[] {
						"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\original3.wav",
						"E:\\RESEARCHS\\htk\\datas\\dv3win8micarray\\outbolljava3.wav" });
	}

	static void test003() {
		double gain = 1 - 0.34;

		String gs = gain + "";
		// gs="1.1230000";
		gs = gs.length() >= 4 ? gs.substring(0, 4) : gs.substring(0,
				gs.length());

		int y = gs.indexOf(".");

		String drgs = "";
		if (y >= 0) {

			String left = gs.substring(0, y);
			String right = gs.substring(y + 1, gs.length());
			drgs = left + "dot" + right;
		}

		d(gs);
		d(drgs);
	}

	static void test002() {

		d("testing msb lsb byte double manipulation ");
		byte x[] = new byte[2];

		// x[0] = 127;
		// x[0] = 0;
		x[0] = 127;
		x[1] = 127;

		short msb = x[1];
		int sample = (short) ((x[0])) | (msb << 8);

		d("new sample 1 = " + sample);

		x[0] = (byte) (sample & 0xff);
		x[1] = (byte) ((sample >> 8) & 0xff);
		msb = x[1];
		sample = (short) ((x[0])) | (msb << 8);

		d("new sample 2 = " + sample);

	}

	static void test001() {
		ReadExample.testRead(new String[] { "../../htkinstaller/data/test.wav",
				"../../htkinstaller/data/newtest.wav" });
	}

	public static void d(String x) {
		System.out.println(x);
	}

}

class ReadExample {
	public static void testRead(String[] args) {
		try {
			// Open the wav file specified as the first argument
			WavFile wavFile = WavFile.openWavFile(new File(args[0]));

			// Display information about the wav file
			wavFile.display();
			long numFrames = wavFile.getNumFrames();
			long sampleRate = wavFile.getSampleRate();
			int channels = wavFile.getNumChannels();
			int validBits = wavFile.getValidBits();
			WavFile nWavFile = WavFile.newWavFile(new File(args[1]), channels,
					numFrames, validBits, sampleRate);

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();

			// Create a buffer of 100 frames
			double[] buffer = new double[100 * numChannels];

			int framesRead;
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;

			double gain = 0.5, doubles;
			short msb, lsb, sample = 0;
			byte[] x = new byte[2];
			do {
				// Read frames into buffer
				framesRead = wavFile.readFrames(buffer, 100);

				for (int i = 0; i < framesRead; i++) {

					if (gain != 1.0) {
						doubles = buffer[i] * 1000;
						doubles *= gain;
						lsb = (short) doubles;
						msb = (short) doubles;
						msb = (short) (msb << 8);
						x[0] = (byte) lsb;
						x[1] = (byte) msb;

						sample = (short) x[0];
						msb = (short) x[1];
						sample |= (msb << 8);

						buffer[i] = (double) sample / 1000;
					}

				}

				nWavFile.writeFrames(buffer, framesRead);
				// Loop through frames and look for minimum and maximum value
				for (int s = 0; s < framesRead * numChannels; s++) {
					if (buffer[s] > max)
						max = buffer[s];
					if (buffer[s] < min)
						min = buffer[s];
				}

			} while (framesRead != 0);

			// Close the wavFile
			wavFile.close();
			nWavFile.close();

			// Output the minimum and maximum value
			System.out.printf("Min: %f, Max: %f\n", min, max);
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

class WriteExample {
	public static void main(String[] args) {
		try {
			int sampleRate = 44100; // Samples per second
			double duration = 5.0; // Seconds

			// Calculate the number of frames required for specified duration
			long numFrames = (long) (duration * sampleRate);

			// Create a wav file with the name specified as the first argument
			WavFile wavFile = WavFile.newWavFile(new File(args[0]), 2,
					numFrames, 16, sampleRate);

			// Create a buffer of 100 frames
			double[][] buffer = new double[2][100];

			// Initialise a local frame counter
			long frameCounter = 0;

			// Loop until all frames written
			while (frameCounter < numFrames) {
				// Determine how many frames to write, up to a maximum of the
				// buffer size
				long remaining = wavFile.getFramesRemaining();
				int toWrite = (remaining > 100) ? 100 : (int) remaining;

				// Fill the buffer, one tone per channel
				for (int s = 0; s < toWrite; s++, frameCounter++) {
					buffer[0][s] = Math.sin(2.0 * Math.PI * 400 * frameCounter
							/ sampleRate);
					buffer[1][s] = Math.sin(2.0 * Math.PI * 500 * frameCounter
							/ sampleRate);
				}
				// Write the buffer
				wavFile.writeFrames(buffer, toWrite);
			}

			// Close the wavFile
			wavFile.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

class ReadExample1 {

	static void d(String s) {
		Main.d(s);
	}

	public static void testRead1(String[] args) {
		try {
			// Open the wav file specified as the first argument
			WavFile wavFile = WavFile.openWavFile(new File(args[0]));

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
			WavFile nWavFile = WavFile.newWavFile(new File(args[1]), channels,
					numFrames-1*sampleRate, validBits, sampleRate);
			d("window W= " + W);
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
			d("NIS= " + NIS);
			// double Gamma = 1;
			// Create a buffer of 100 frames

			int wSP = (int) (SP * W);
			d("window shift wSP= " + wSP);
			double[] tmpwsp = new double[wSP * numChannels];
			double[] WBuffer = new double[W], afterwsBuffer = new double[W
					- wSP];
			int FreqResol = (int) (W / 2) + 1;
			d("FreqResol= " + FreqResol);
			float[] WBufferHw = new float[2 * W];
			double[] OverLapper = new double[W];
			float[] OverLapper_last = new float[W];
			float[] Y = new float[W], YPhase = new float[W], YPhase_last_1 = new float[FreqResol];
			float Beta = (float) 0.03;
			float[][] Y_NIS = new float[NIS][FreqResol];
			float[][] YPhase_NIS = new float[NIS][FreqResol];
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
			int totalFramesRead = framesRead;
			boolean lastSpeechFlag = false;
			boolean lastNoiseFlag = true;
			boolean just_copy = false;
			boolean error_read =false;
			String outYS="";
			int N_MTLB=2;
			// nWavFile.writeFrames(WBuffer, framesRead);
			do {

				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS) {
					// d("TotalYFrameRead"+TotalYFrameRead);
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
							d("error at frameNo " + TotalYFrameRead + " "
									+ framesRead + " < " + (W - wSP) + "");
							// System.exit(-1);
							error_read=true;
							break;
						}

						System.arraycopy(tmpwsp, 0, WBuffer, W - framesRead,
								framesRead);
					}
					for (int s = 0; s < W; s++) {
						if (WBuffer[s] > max)
							max = WBuffer[s];
						if (WBuffer[s] < min)
							min = WBuffer[s];
					}

					totalFramesRead += framesRead;

					int j = 0;
					for (int i = 0; i < WBuffer.length; i++) {

						WBufferHw[2 * i] = (float) WBuffer[i] * hammings[j];
						WBufferHw[2 * i + 1] = 0;
						j++;
					}
					ffter.complexForwardTransform(WBufferHw);
					// for (int i = 0; i < FreqResol; i++) {
					// Y[i]=0;
					// }
					ffter.powerAndPhaseFromFFT(WBufferHw, Y, YPhase);
					// ffter.powerPhaseFFT(WBufferHw, Y, YPhase);
					for (int i = 0; i < FreqResol; i++) {
						if (Y[i] < 0) {
							d("error at TotalYFrameRead=" + TotalYFrameRead
									+ " i=" + i + " Y[i]=" + Y[i] + " Y=" + Y);
							break;

						}
					}

				} else {

					if (NIS_RETRACE_FRAME_NO < NIS) {
						SpectralSubstraction.copyVector(
								Y_NIS[NIS_RETRACE_FRAME_NO], Y, FreqResol);
						NIS_RETRACE_FRAME_NO++;
						if (NIS_RETRACE_FRAME_NO == NIS)
							NIS_RETRACE = false;
					}

				}

				if (TotalYFrameRead >= 1 & TotalYFrameRead <= NIS
						& NIS_RETRACE_FRAME_NO < NIS) {
					// d("summing NIS nosie frames from "+frameNo);

					// copying phase
					for (int i = 0; i < FreqResol; i++) {
						NIS_sum[i] += Y[i];
						Y_NIS[TotalYFrameRead - 1][i] = Y[i];
						if (Y[i] < 0) {
							d("error at TotalYFrameRead=" + TotalYFrameRead
									+ "i=" + i + " Y[i]=" + Y[i]);
						}
						YPhase_NIS[TotalYFrameRead - 1][i] = YPhase[i];
					}

					if (TotalYFrameRead == NIS & !NIS_RETRACE) {
						NIS_RETRACE = true; // order to recompute
						//d("compute mean NIS");
						for (int i = 0; i < FreqResol; i++) {
							NIS_sum[i] = 0;
						}
						for (int i = 0; i < NIS; i++) {
							// darr("" + i, Y_NIS[i], 0, 3);
							for (int j = 0; j < FreqResol; j++) {
								NIS_sum[j] += Y_NIS[i][j];
							}
						}
						// darr(NIS_sum, 0, 1);
						for (int i = 0; i < FreqResol; i++) {
							N[i] = NIS_sum[i] / NIS;
						}
						/*d("Mean Noise MTLB: N");
						darr(N, 0, 3);
						*/
						// darr(NIS_sum, 100, 3);
						// darr(NIS_sum, 199, 2);
						frameProcessed = 1;// begin processing
					}

				}
				if (frameProcessed > 0) {// AFTER NIS

					if (frameProcessed > 2) {// begin averaging ...
						// if (frameProcessed == 1)
						// d("Begin magnitude average");
						/** Magnitude Average finding YS **/

						if (frameProcessed > 5) {
							SpectralSubstraction.copyVector(YS_last_3,
									YS_last_2, FreqResol);
						}

						
						if (frameProcessed > 4) {
							SpectralSubstraction.copyVector(YS_last_1,
									YS_last_2, FreqResol);
						}
						if (frameProcessed > 3) {
							SpectralSubstraction.copyVector(YS, YS_last_1,
									FreqResol);

						}

						for (int i = 0; i < FreqResol; i++) {
							YS_last_1[i] = (Y[i] + Y_last_1[i] + Y_last_2[i]) / 3;

							/*if ((frameProcessed < 6) & (i == 0)) {
								d("fp " + frameProcessed + " MTLB YS(:,"
										+ (frameProcessed - 2) + ") Avr "
										+ Y_last_2[i] + " " + Y_last_1[i] + " "
										+ Y[i] + "->" + YS[i]);
							}*/

						}
						/*outYS="YS fp:"+frameProcessed;
						for (int yd=0;yd<FreqResol ; yd++){
							outYS += "\t" +YS[yd];
						}
						d(outYS);
						*/
						if (frameProcessed > 7) {
							boolean debug = false;

							/*if (frameProcessed < 6) {
								darr("Y_last_2 " + frameProcessed
										+ " MTLB Y(:," + (frameProcessed - 3)
										+ ") ", Y_last_2, 0, 3);
								darr("Y_last_1 " + frameProcessed
										+ " MTLB Y(:," + (frameProcessed - 2)
										+ ") ", Y_last_1, 0, 3);
								darr("Y " + frameProcessed + " MTLB Y(:,"
										+ (frameProcessed - 1) + ") ", Y, 0, 3);
							}

							d("fp " + frameProcessed + " Mean Noise MTLB: N");
							darr(N, 0, 3);
							
							*/
							/*if (frameProcessed < 10){
								d("before vad NoiseCounter="+NoiseCounter[0] + " NoiseMargin="+NoiseMargin);
								darr(" before vad signal=", Y_last_2,0,3);
								darr(" before vad noise=", N,0,3);
								//debug=true;
							}*/	
							darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB) +" Y" , Y_last_1,0,3);
							darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB) + " YS", YS_last_1,0,3);
							darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB) + " N", N,0,3);
							d("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB) + " NoiseCounter "+ NoiseCounter[0]);
							
							VADInfo info = SpectralSubstraction.vad(Y_last_1,
									N, NoiseCounter, NoiseMargin, HangOver,
									debug);
							info.NoiseCounter = NoiseCounter[0];
							
							//d("Dist\t"+frameProcessed+"\t"+info.SpectralDist);
							/*if (frameProcessed < 22	){
								
								darr("after Y fp="+frameProcessed,Y_last_2,0,3 );
								darr("after YS fp="+frameProcessed,YS_last_1,0,3 );
								darr("after N fp="+frameProcessed,N,0,3 );
								d("after vad SpeechFlag="+info.SpeechFlag);
								d("fp="+frameProcessed+"/M:"+(frameProcessed-3)+ " after vad Dist="+info.SpectralDist);
							}*/

							/*if (frameProcessed < 2000	)
								if (info.SpeechFlag != lastSpeechFlag) {// change
								d("after vad SF changed to " + info.SpeechFlag + " at "
										+ frameProcessed + "/M:"+(frameProcessed-3));
								lastSpeechFlag = info.SpeechFlag;
								darr("Y", Y_last_2,0,3);
								darr("N",N,0,3);
							}
							/*byte []b=new byte[10];
							InputStreamReader ir=new InputStreamReader(System.in);
							BufferedReader r=new BufferedReader(ir);
							r.readLine();
							*/
							/*
							if (info.NoiseFlag != lastNoiseFlag) {// change
								d("NF changed to " + info.NoiseFlag + " at "
										+ frameProcessed + " ");
								lastNoiseFlag = info.NoiseFlag;
							}*/

							SpectralSubstraction.substract(YS_last_1, N, D);
								//darr("D", D, 0, 3);
							/*outYS="D fp:"+frameProcessed;
							for (int yd=0;yd<FreqResol ; yd++){
								outYS += "\t" +D[yd];
							}
							d(outYS);
							*/
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
								darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB)+" x_at_sf_0",X,0,3);

							} else {

								for (int j2 = 0; j2 < D.length; j2++) {
									if (D[j2] < NRM[j2]) {
										if (D[j2] > YS_last_2[j2] - N[j2]){
											//d("chto "+(YS_last_3[j2] - N[j2]));
											D[j2] = YS_last_2[j2] - N[j2];
										}
										if (D[j2] > YS[j2] - N[j2])
											D[j2] = YS[j2] - N[j2];
									}
								}
								// X(:,i)=max(D,0);
								SpectralSubstraction
										.copyVector(D, X, FreqResol);
								SpectralSubstraction.min2Zero(X);
								darr("fp="+frameProcessed+"/M:"+(frameProcessed-N_MTLB)+ " x_at_sf_1",X,0,3);
							}
							/*if (frameProcessed < 10) {
								d("D fp="+ frameProcessed+" MTLB="+(frameProcessed-3));
								darr("D " + frameProcessed, D, 0, 2);
								darr("D " + frameProcessed, D, 50, 2);
								darr("D " + frameProcessed, D, 120, 2);
								darr("D " + frameProcessed, D, 199, 2);
							}
							*/
							/*if (frameProcessed < 22 ) {
								d("X fp="+ frameProcessed+" MTLB="+(frameProcessed-3));
								darr("X " + frameProcessed, X, 0, 3);
							}*/

							/*for (int i = 0; i < FreqResol; i++) {
								if (i>0 & i<FreqResol-1){
									X[i]=(X[i-1]+X[i]+X[i+1])/3;
								}
							}*/
							
							/*for (int i = 0; i < FreqResol; i++) {
								X[i]*=hammings[i];
							}*/
						
							outYS="X_TXT\t"+frameProcessed;
							for (int yd=0;yd<FreqResol ; yd++){
								outYS += "\t" +X[yd];
							}
							d(outYS);

							outYS="YPHASE_TXT\t"+frameProcessed;
							for (int yd=0;yd<FreqResol ; yd++){
								outYS += "\t" +YPhase_last_1[yd];
							}
							d(outYS);


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

							if (frameProcessed == 4) {
								dfft(WBufferHw, 0, 2);
							}

							ffter.complexBackwardsTransform(WBufferHw);

							//SpectralSubstraction.copyVector(OverLapper,OverLapper_last,W);
							
							j = 0;
							//for (int i = 0; i < wSP; i++) {
							for (int i = 0; i < W; i++) {
										 //output[i]=WBufferHw[j];
								output[i] = WBufferHw[j] +OverLapper[i];
								j += 2;
							}
							SpectralSubstraction.zeroVector(OverLapper,W);
							
							j=0;
							for (int i = wSP; i < W; i++) {
									OverLapper[j]=output[i];
									j++;
							}
								
							nWavFile.writeFrames(output, wSP);
							// SpectralSubstraction.OverlapAdd2(WBufferHw,
							// YPhase, W, wSP,
							// output);
						}// if frameProcessed > 3

					}// if frameProcessed > 2

					if (frameProcessed > 1) {
						SpectralSubstraction.copyVector(Y_last_1, Y_last_2,
								FreqResol);
					}
					// ok
					if (frameProcessed > 0) {
						SpectralSubstraction.copyVector(YPhase, YPhase_last_1,
								FreqResol);
						SpectralSubstraction.copyVector(Y, Y_last_1, FreqResol);

						// vad
					}

					frameProcessed++;

				}// if frameProcessed >0

				// nWavFile.writeFrames(afterwsBuffer, framesRead);
				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS) {
					System.arraycopy(WBuffer, wSP, afterwsBuffer, 0, W - wSP);
					System.arraycopy(afterwsBuffer, 0, WBuffer, 0, W - wSP);
				}

			} while (framesRead != 0);
			// Close the wavFile
			wavFile.close();
			nWavFile.close();
			// Output the minimum and maximum value
			System.out.printf("Min: %f, Max: %f\n", min, max);

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static void darr(float[] wBuffer, int start, int len) {
		darr("", wBuffer, start, len);

	}

	public static String darr(String left, float[] wBuffer, int start, int len) {
		// TODO Auto-generated method stub
		int i = 0;
		String pr = "";
		for (i = start; i < start + len; i++) {
			pr += left + ": " + (i) + ": " + wBuffer[i] + "\n";
		}

		d(pr);
		return pr;
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

}

class ReadExample2 {

	static void d(String s) {
		Main.d(s);
	}

	public static void testRead2(String[] args) {
		try {
			// Open the wav file specified as the first argument
			WavFile wavFile = WavFile.openWavFile(new File(args[0]));

			// Display information about the wav file
			//wavFile.display();
			long numFrames = wavFile.getNumFrames();
			long sampleRate = wavFile.getSampleRate();
			int channels = wavFile.getNumChannels();
			int validBits = wavFile.getValidBits();
			WavFile nWavFile = WavFile.newWavFile(new File(args[1]), channels,
					numFrames, validBits, sampleRate);

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();
			int W = (int) (0.025 * (double) sampleRate);
			double SP = 0.4;
			//d("window W= " + W);
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
			//d("NIS= " + NIS);
			// double Gamma = 1;
			// Create a buffer of 100 frames

			int wSP = (int) (SP * W);
			//d("window shift wSP= " + wSP);
			double[] tmpwsp = new double[wSP * numChannels];
			double[] WBuffer = new double[W], afterwsBuffer = new double[W
					- wSP];
			int FreqResol = (int) (W / 2) + 1;
			//d("FreqResol= " + FreqResol);
			float[] WBufferHw = new float[2 * W];
			float[] Y = new float[W], YPhase = new float[W], YPhase_last = new float[FreqResol];
			float Beta = (float) 0.03;
			float[][] Y_NIS = new float[NIS][FreqResol];
			float[][] YPhase_NIS = new float[NIS][FreqResol];
			float[] N = new float[FreqResol];
			float[] NIS_sum = new float[FreqResol];
			float[] YS = new float[FreqResol];
			float[] Y_last_1 = new float[FreqResol];
			float[] Y_last_2 = new float[FreqResol]; // next
			float[] YS_last_1 = new float[FreqResol];
			float[] YS_last_2 = new float[FreqResol]; // next
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
			int totalFramesRead = framesRead;
			boolean lastSpeechFlag = false;
			boolean lastNoiseFlag = true;
			boolean just_copy;
			// nWavFile.writeFrames(WBuffer, framesRead);
			boolean error_read=false;
			do {

				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS) {
					// d("TotalYFrameRead"+TotalYFrameRead);
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
							//d("error at frameNo " + TotalYFrameRead + " "
								//	+ framesRead + " < " + (W - wSP) + "");
							// System.exit(-1);
							error_read=true;
									
							break;
						}

						System.arraycopy(tmpwsp, 0, WBuffer, W - framesRead,
								framesRead);
					}

					/*for (int s = 0; s < W; s++) {
						if (WBuffer[s] > max)
							max = WBuffer[s];
						if (WBuffer[s] < min)
							min = WBuffer[s];
					}*/

					totalFramesRead += framesRead;

					int j = 0;
					for (int i = 0; i < WBuffer.length; i++) {

						WBufferHw[2 * i] = (float) WBuffer[i] * hammings[j];
						WBufferHw[2 * i + 1] = 0;
						j++;
					}
					ffter.complexForwardTransform(WBufferHw);
					ffter.powerAndPhaseFromFFT(WBufferHw, Y, YPhase);
					for (int i = 0; i < FreqResol; i++) {
						if (Y[i] < 0) {
							d("error at TotalYFrameRead=" + TotalYFrameRead
									+ " i=" + i + " Y[i]=" + Y[i] + " Y=" + Y);
							break;
						}
					}

				} else {

					if (NIS_RETRACE_FRAME_NO < NIS) {
						SpectralSubstraction.copyVector(
								Y_NIS[NIS_RETRACE_FRAME_NO], Y, FreqResol);
						NIS_RETRACE_FRAME_NO++;
						if (NIS_RETRACE_FRAME_NO == NIS)
							NIS_RETRACE = false;
					}

				}

				if (TotalYFrameRead >= 1 & TotalYFrameRead <= NIS
						& NIS_RETRACE_FRAME_NO < NIS) {
					// d("summing NIS nosie frames from "+frameNo);

					// copying phase
					for (int i = 0; i < FreqResol; i++) {
						NIS_sum[i] += Y[i];
						Y_NIS[TotalYFrameRead - 1][i] = Y[i];
						if (Y[i] < 0) {
							d("error at TotalYFrameRead=" + TotalYFrameRead
									+ "i=" + i + " Y[i]=" + Y[i]);
							break;
						}
						YPhase_NIS[TotalYFrameRead - 1][i] = YPhase[i];
					}

					if (TotalYFrameRead == NIS & !NIS_RETRACE) {
						NIS_RETRACE = true; // order to recompute
						//d("compute mean NIS");
						for (int i = 0; i < FreqResol; i++) {
							NIS_sum[i] = 0;
						}
						for (int i = 0; i < NIS; i++) {
							// darr("" + i, Y_NIS[i], 0, 3);
							for (int j = 0; j < FreqResol; j++) {
								NIS_sum[j] += Y_NIS[i][j];
							}
						}
						// darr(NIS_sum, 0, 1);
						for (int i = 0; i < FreqResol; i++) {
							N[i] = NIS_sum[i] / NIS;
						}
						//d("Mean Noise MTLB: N");
						//darr(N, 0, 3);
						// darr(NIS_sum, 100, 3);
						// darr(NIS_sum, 199, 2);
						frameProcessed = 1;// begin processing
					}

				}
				if (frameProcessed > 0) {// AFTER NIS

					if (frameProcessed > 2) {// begin averaging ...
						// if (frameProcessed == 1)
						// d("Begin magnitude average");
						/** Magnitude Average finding YS **/

						if (frameProcessed > 4) {
							SpectralSubstraction.copyVector(YS_last_1,
									YS_last_2, FreqResol);
						}
						if (frameProcessed > 3) {
							SpectralSubstraction.copyVector(YS, YS_last_1,
									FreqResol);

						}

						for (int i = 0; i < FreqResol; i++) {
							YS[i] = (Y[i] + Y_last_1[i] + Y_last_2[i]) / 3;

							if ((frameProcessed < 0) & (i == 0)) {
								d("fp " + frameProcessed + " MTLB YS(:,"
										+ (frameProcessed - 2) + ") Avr "
										+ Y_last_2[i] + " " + Y_last_1[i] + " "
										+ Y[i] + "->" + YS[i]);
							}

						}

						if (frameProcessed > 3) {
							boolean debug = false;

							/*if (frameProcessed < 6) {
								darr("Y_last_2 " + frameProcessed
										+ " MTLB Y(:," + (frameProcessed - 3)
										+ ") ", Y_last_2, 0, 3);
								darr("Y_last_1 " + frameProcessed
										+ " MTLB Y(:," + (frameProcessed - 2)
										+ ") ", Y_last_1, 0, 3);
								darr("Y " + frameProcessed + " MTLB Y(:,"
										+ (frameProcessed - 1) + ") ", Y, 0, 3);
							}*/

							// d("fp "+frameProcessed+" Mean Noise MTLB: N");
							// darr(N, 0, 3);
							VADInfo info = SpectralSubstraction.vad(Y_last_1,
									N, NoiseCounter, NoiseMargin, HangOver,
									debug);
							info.NoiseCounter = NoiseCounter[0];

							if (info.SpeechFlag != lastSpeechFlag) {// change
								// d("SF changed to " +info.SpeechFlag +" at " +
								// frameProcessed+ " ");
								lastSpeechFlag = info.SpeechFlag;
							}

							if (info.NoiseFlag != lastNoiseFlag) {// change
								// d("NF changed to " +info.NoiseFlag +" at " +
								// frameProcessed+ " ");
								lastNoiseFlag = info.NoiseFlag;
							}

							SpectralSubstraction.substract(YS_last_1, N, D);
							//if (frameProcessed == 4)
								//darr("D", D, 0, 3);
							if (!info.SpeechFlag) {
								just_copy = false;
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
								just_copy = true;
							}

							
							WBufferHw[0] = X[0];
							WBufferHw[1] = 0;

							int j = 1;
							for (int i = 2; i < 2 * FreqResol; i += 2) {
								WBufferHw[i] = (float) (X[j] * Math
										.cos(-YPhase_last[j]));
								WBufferHw[i + 1] = (float) (X[j] * Math
										.sin(YPhase_last[j]));

								if (i < 2 * FreqResol - 2) {
									// data2[data2.length - j-1] = i;
									WBufferHw[WBufferHw.length - 2 * j] = WBufferHw[i];
									WBufferHw[WBufferHw.length - 2 * j + 1] = -WBufferHw[i + 1];
								}
								j++;
							}

							//if (frameProcessed == 4) {
								//dfft(WBufferHw, 0, 2);
							//}

							ffter.complexBackwardsTransform(WBufferHw);

							if (!just_copy) {
								j = 0;
								for (int i = 0; i < wSP; i++) {
									 output[i]=WBufferHw[j];
									//output[i] = WBufferHw[j] / hammings[i];
									j += 2;
								}
							} else {
								for (int i = 0; i < wSP; i++) {
									// output[i]=WBufferHw[j];
									output[i] = WBuffer[i];
								}

							}
							
							
							nWavFile.writeFrames(output, wSP);
						}// if frameProcessed > 3

					}// if frameProcessed > 2

					if (frameProcessed > 1) {
						SpectralSubstraction.copyVector(Y_last_1, Y_last_2,
								FreqResol);
					}
					// ok
					if (frameProcessed > 0) {
						SpectralSubstraction.copyVector(YPhase, YPhase_last,
								FreqResol);
						SpectralSubstraction.copyVector(Y, Y_last_1, FreqResol);

						// vad
					}

					frameProcessed++;

				}// if frameProcessed >0

				if (!NIS_RETRACE | NIS_RETRACE_FRAME_NO >= NIS) {
					System.arraycopy(WBuffer, wSP, afterwsBuffer, 0, W - wSP);
					System.arraycopy(afterwsBuffer, 0, WBuffer, 0, W - wSP);
				}

			} while (framesRead != 0);
			
			if(error_read){
				//end of stream
				//d("end of stream");
				while (nWavFile.getFramesRemaining() > 0) {
					nWavFile.writeFrames(output, wSP);//padding with last noise
				}
				//d("frames remaining =" +nWavFile.getFramesRemaining());
			}

			// Close the wavFile
			wavFile.close();
			nWavFile.close();
			// Output the minimum and maximum value
			//System.out.printf("Min: %f, Max: %f\n", min, max);

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static void darr(float[] wBuffer, int start, int len) {
		darr("", wBuffer, start, len);

	}

	public static void darr(String left, float[] wBuffer, int start, int len) {
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

}