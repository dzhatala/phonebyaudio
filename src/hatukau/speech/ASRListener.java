package hatukau.speech;

public interface ASRListener {
	void recognitionCompleted(String lines);
	void recognizerExit();
	void recognizerStarted();
	
}
