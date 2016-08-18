package hatukau.io;

import hatukau.speech.NewWavFile;
import hatukau.speech.SpectralSubstraction;
import hatukau.speech.WavFile;
import hatukau.speech.WavFileException;

import java.io.File;
import java.io.IOException;

import com.musicg.wave.WaveHeader;

public class NewAsynchronousWavWriter {

	private String fname;
	private WaveHeader h;
	private NewWavFile wavFile;

	/**
	 * 
	 * @param fullOutputName
	 * @param waveHeader
	 */
	public NewAsynchronousWavWriter(String fullOutputName, WaveHeader waveHeader) {
		// TODO Auto-generated constructor stubh
		this.h = waveHeader;
		this.fname = fullOutputName;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void startWrite() {
		// TODO Auto-generated method stub

		try {
			wavFile = NewWavFile.newWavFile(new File(fname), h.getChannels(),
					0, h.getBitsPerSample() / 8, h.getSampleRate());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void appendBytes(byte[] bb, int length) throws Exception {
		int bytesPerSample = h.getBitsPerSample() / 8;
		if (length % bytesPerSample != 0) {
			throw new Exception("To write not multiple of bytes Per Sample");

		}

		int numLong = bb.length / bytesPerSample;
		byte[] temp = new byte[bytesPerSample];
		for (int i = 0; i < numLong; i += bytesPerSample) {
			for (int j = 0; j < bytesPerSample; j++) {
				temp[j] = bb[i];
			}

			long l2write = SpectralSubstraction
					.bytes2Long(temp, bytesPerSample);
			wavFile.incNumFrames2(wavFile.getNumFrames()+1);
			wavFile.writeFrames(new long[] { l2write }, 1);
		}

		// TODO Auto-generated method stub
		// long []ret SpectralSubstraction.bytes2Long()
		// SpectralSubstraction.bytes2Long()
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public void close() throws IOException {
		// TODO Auto-generated method stub
		d("Close " + fname + " getNF() " + wavFile.getNumFrames() + "getFR()"
				+ wavFile.getFramesRemaining());
		wavFile.close();
	}

	public static void d(String s) {
		System.out.println("NAWW: " + s);
	}
}
