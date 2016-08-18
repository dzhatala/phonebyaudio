package com.musicg.demo.android;

public interface RecorderListener {
	void absValue(double averageAbsValue);

	void voiceStart();// sil det feature

	void voiceEnd(); // sill det feature

	void voiceIsNoise();

}
