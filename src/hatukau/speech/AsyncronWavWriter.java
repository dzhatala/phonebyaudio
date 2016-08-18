package hatukau.speech;

import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import utils.Debug;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

/**
 * Write to wav file asynchronously ....
 * 
 * @author Hatala, Z
 * 
 */
class DataBuffer {
	int id=-1;
	byte []data=null;
	DataBuffer next;
	DataBuffer last; /*end of queueue */
	
}
public class AsyncronWavWriter {

	WaveHeader h = null;
	String fn = null;
	//FileOutputStream fos = null;
	RandomAccessFile fos=null;
	long written = 0; // bytes is written, wav data length ?
	protected boolean writing = false; // is we writing ?
	DataBuffer HEAD=null;
	/*DataBuffer LAST=HEAD;*/
	/**
	 * prevention
	 */
	protected AsyncronWavWriter() {

	}

	public AsyncronWavWriter(String fn, WaveHeader h) {
		this.h = h;
		this.fn = fn;
		
	}

	public AsyncronWavWriter(String fn, Wave w) {
		this(fn, w.getWaveHeader());

	}

	long startTime=System.currentTimeMillis();
	/**
	 * blocking init
	 * 
	 * @throws Exception
	 */
	public void startWrite() throws Exception {
		
		if(fos!=null){
			//fos.delete();
			fos.close();
		}

		fos = new RandomAccessFile(fn, "rw");
		fos.setLength(0);
		fos.seek(0);
		Debug.log("AVW: << "+fn );
		written=0;
		updateHeader(fos, this.h);
		//rf.close();
		writing = true;
		/*
		 * fos.write(wave.getBytes()); fos.close();
		 */
	}
	
	public synchronized void stop(){
		writing=false;
	}
	
	public synchronized boolean  isWriting(){
		return writing;
	}
	
	public synchronized void reset() throws IOException{
		fos.seek(0);
	}
	/**
	 * block write
	 * @param frames
	 * @throws Exception
	 */
	 
	public synchronized void appendBytes(byte[] frame,int size) throws Exception {
		if(frame==null)return ;
		if (!isWriting())
			throw new Exception("Bad state, not in writing state ...");
		
		
		
		if(true)
		if (fos != null) {
			//for (int i=0;i<size;i+=2){
			
			fos.write(frame,0,size);
			/* lines belowww is a very bad code */
			/*for (int i=0;i<size;i++){
				fos.writeByte(frame[i]);
			}*/
			
			written += size;
			//System.out.println("Writer : append total="+written);
		}
	}
	
	void fixHTKLoadErr(){
		try {
			fos = new RandomAccessFile(fn, "rw");
			d("fixHTKLoadErr .. "+fn+", written="+written +" file length="+fos.length()+" l-w ="+(fos.length()-written));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
		
	}

	public void close() throws Exception {
		while(isWriting())stop();
		
		/*if (isWriting())
			throw new Exception("Bad state, not in writing state ...");
			*/
		if (fos != null) {
			//fos.close();
			//h.setChunkSize(written);
			//RandomAccessFile fos = new RandomAccessFile(fn, "rw");
			//h.setChunkSize(written+4)
			//d(fos.length()+"" +" written="+written);
			//written=fos.length()-8;
			updateHeader(fos, h);
			//rf.seek(0);
			
			d("Writer closing .. "+fn+", written="+written +" file length="+fos.length());
			fos.close();
			
		}
		
	   fixHTKLoadErr();
		
		writing = false;
		
		fos=null;
	}
	String info="";
	public String getWavInfo(){
		return info;
	}

	/**
	 * update wav header
	 * 
	 * @throws Exception
	 */
	protected void updateHeader(RandomAccessFile fos, WaveHeader waveHeader)
			throws Exception {
		// WaveHeader waveHeader = this.h;
		if (waveHeader == null)
			throw new Exception("Header not set");
		
		
		int byteRate = waveHeader.getByteRate();
		//int audioFormat = waveHeader.getAudioFormat();
		int longSampleRate = waveHeader.getSampleRate();
		byte RECORDER_BPP = (byte)waveHeader.getBitsPerSample();
		int channels = waveHeader.getChannels();
		//int channels=1;
		//long totalDataLen = waveHeader.getChunkSize();
		//totalDataLen=written*8;
		
		//fix written for load error in htk ...
		//written = fos.getFilePointer()-44;
		
		long totalDataLen=2*written/(RECORDER_BPP/8);
		
		long totalAudioLen= totalDataLen;
		
		info="byteRate="+byteRate+"\n";
		info+="longSampleRate="+longSampleRate+"\n";
		info+="RECORDER_BPP="+RECORDER_BPP+"\n";
		info+="channels="+channels+"\n";
		info+="totalDataLen="+totalDataLen+"\n";
		info+="written="+written+"\n";
		long now=System.currentTimeMillis();
		info+="Interval="+(now-startTime)+" ms\n";
		
		/*System.out.println("####### Writer:READY TO UPDATE RIFF HEADER ##########3");
		System.out.println("byteRate="+byteRate);
		System.out.println("longSampleRate="+longSampleRate);
		System.out.println("RECORDER_BPP="+RECORDER_BPP);
		System.out.println("channels="+channels);
		System.out.println("totalDataLen="+totalDataLen);
		System.out.println("written="+written);
		System.out.println("####### Writer:READY TO UPDATE RIFF HEADER ##########3");
		*///System.out.println("byteRate="+byteRate);
		
		
		//@TODO FIX
		/* byteRate=longSampleRate*RECORDER_BPP/8;
		 totalDataLen=byteRate*10;
		 totalAudioLen=totalDataLen+44;
		*/
		
		
		//totalDataLen+= waveHeader.getSubChunk1Size();
		//long subChunk2Size = waveHeader.getSubChunk2Size();
		//int blockAlign = waveHeader.getBlockAlign();
		
		/*int longSampleRate=8000;
		byte RECORDER_BPP= 16;
		int channels=1;
		int byteRate=longSampleRate*RECORDER_BPP/8;
		long totalDataLen=byteRate*50;
		long totalAudioLen=totalDataLen+44;
		*/
		
		if (fos == null)
			throw new Exception("Bad output stream : NULL");
		byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		fos.seek(0);
        fos.write(header, 0, 44);
		d("Writer: 44 bytes Wav Header Writen");
		//fos.seek(0);
		/*fos.write(new byte[] { (byte) (chunkSize), (byte) (chunkSize >> 8),
				(byte) (chunkSize >> 16), (byte) (chunkSize >> 24) });
		fos.write(WaveHeader.WAVE_HEADER.getBytes());
		fos.write(WaveHeader.FMT_HEADER.getBytes());
		fos.write(new byte[] { (byte) (subChunk1Size),
				(byte) (subChunk1Size >> 8), (byte) (subChunk1Size >> 16),
				(byte) (subChunk1Size >> 24) });
		fos.write(new byte[] { (byte) (audioFormat), (byte) (audioFormat >> 8) });
		fos.write(new byte[] { (byte) (channels), (byte) (channels >> 8) });
		fos.write(new byte[] { (byte) (sampleRate), (byte) (sampleRate >> 8),
				(byte) (sampleRate >> 16), (byte) (sampleRate >> 24) });
		fos.write(new byte[] { (byte) (byteRate), (byte) (byteRate >> 8),
				(byte) (byteRate >> 16), (byte) (byteRate >> 24) });
		fos.write(new byte[] { (byte) (blockAlign), (byte) (blockAlign >> 8) });
		fos.write(new byte[] { (byte) (bitsPerSample),
				(byte) (bitsPerSample >> 8) });
		fos.write(WaveHeader.DATA_HEADER.getBytes());
		fos.write(new byte[] { (byte) (subChunk2Size),
				(byte) (subChunk2Size >> 8), (byte) (subChunk2Size >> 16),
				(byte) (subChunk2Size >> 24) });
		*/

		// if(true)throw new Exception("not implemented");
	}
	
	/*public  static void updateHeader1(DataOutput out, 
			long totalDataLen, long totalAudioLen,long longSampleRate, byte byteRate,
			byte channels,byte RECORDER_BPP ) throws IOException{
		*/
    public static String  testWavHeader()throws Exception{		
		String ret="AWWOUTtest.wav";
    	int longSampleRate=8000;
		byte RECORDER_BPP= 16;
		int channels=1;
		int byteRate=longSampleRate*RECORDER_BPP/8;
		long totalDataLen=0;
		long totalAudioLen=totalDataLen+44;
		long duration=5;
		RandomAccessFile out=new RandomAccessFile(ret,"rw");
		out.setLength(0);
		out.seek(0);
		byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
		totalDataLen=byteRate*duration;
		totalAudioLen=totalDataLen;
		for (int i=0;i<totalDataLen;i++){
			//Short s=(Short)new java.util.Random().nextGaussian();
			out.writeShort(1);
		}
		
		header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        
		header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.seek(0);

		out.write(header,0,header.length);
		out.close();
		return ret;
	}


	/**
	 * test write wav file
	 * @param args
	 */
	public static void main(String args[]){
		/*
		RandomAccessFile rf=new RandomAccessFile("test.wav","rw");
		*/
		
		try{
			
			testWavHeader();
			
		}catch (Exception ex){
				ex.printStackTrace();
		}
	}
	
	public static void d(String s){
		System.out.println(AsyncronWavWriter.class.getCanonicalName()+ ": " +s);
	}
	
}