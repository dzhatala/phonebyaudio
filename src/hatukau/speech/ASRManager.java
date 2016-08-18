package hatukau.speech;

import hatukau.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.EditText;

/**
 * @TODO, how to really cleaning hp HVite2.exe process ...
 * 
 * @author din
 * 
 */

public final class ASRManager {

	private static Vector listeners = new Vector();
	private static String[] bases;
	public static Context myContext = null;
	private static int numLoopers = 1;
	private static HViteLooper[] loopers = new HViteLooper[numLoopers];

	@SuppressWarnings("unchecked")
	public static void addListener(ASRListener l) {
		if (exist(l))
			return; // no need to
		listeners.add(l);
	}

	private static boolean exist(ASRListener l) {
		// TODO Auto-generated method stub
		int s = listeners.size();
		for (int i = 0; i < s; i++) {
			Object x = listeners.elementAt(i);
			if (x.equals(l))
				return true;
		}
		return false;
	}

	public static void removeListener(ASRListener l) {
		listeners.remove(l);

	}

	static void fireEventReady() {
		/*
		 * if (listeners != null) { listeners.recognizerStarted(); }
		 */
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				ASRListener l = (ASRListener) listeners.elementAt(i);
				l.recognizerStarted();
				;
				;
			}
		}

	}

	static void fireEventExit() {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				ASRListener l = (ASRListener) listeners.elementAt(i);
				l.recognizerExit();
				;
			}
		}
	}

	static int numReply = 0;
	static double currScore = -9E10; // score received from looper
	static String currLine = null; // line to pass upper

	static void fireEventComplete(Object source, String line) {
		d("fEC(), lr size=" + listeners.size());

		if (line.indexOf("frames") >= 0 | line.indexOf("_nOS_") >= 0
				| line.indexOf("_nToK_") >= 0) {
			numReply++;
			double scnow = getScore(line);
			if (scnow > currScore) {
				d("score reset:  (" + scnow + "<==" + currScore
						+ ") line is : " + line);
				currScore = scnow;
				currLine = line;
			}
		}
		d("numReply=" + numReply);

		if (numReply == getNumLoopers()) {
			if (listeners != null) {
				for (int i = 0; i < listeners.size(); i++) {
					ASRListener l = (ASRListener) listeners.elementAt(i);
					l.recognitionCompleted(currLine);
				}
			}
			numReply = 0;
		}
	}

	static double getScore(String line) {
		double ret = -1E10;
		d("getScore " + line);

		if (line.indexOf("frames") > 0 | line.indexOf("_nOS_") > 0
				| line.indexOf("_nToK_") > 0) {
			ret = -1E9;
		}
		int idx1 = line.indexOf("frames]");
		int idx2 = line.indexOf("[Ac");

		if (idx1 > 0 & idx2 > 0) {
			String ds = line.substring(idx1 + 7, idx2 - 1);
			d("parseDouble " + ds);
			ret = Double.parseDouble(ds);
		}

		return ret;
	}

	public static synchronized void sendCommand(String cmd) {
		d("sendCommand :" + cmd);
		if (cmd == null)
			return;
		if (cmd.length() <= 0)
			return;

		if (loopers != null) {

			for (int i = 0; i < getNumLoopers(); i++) {

				if (!loopers[i].isReady()) {
					d("send cancelled " + loopers[i].hashCode() + " not ready ");
					break;
				}
			}

			for (int i = 0; i < getNumLoopers(); i++) {
				if (loopers[i] != null)
					loopers[i].sendCMD(cmd);
			}

		}
		numReply = 0;
		currScore = -1E10;

	}

	private static void d(String string) {
		System.out.println("ASRM:" + string);

	}

	public static boolean isStarted() {

		for (int i = 0; i < getNumLoopers(); i++) {
			if (loopers[i] == null)
				return false;
		}

		return true;
	}

	@SuppressWarnings("static-access")
	public static void startRecognizer(Context ct, String ntvpath)
			throws Exception {
		myContext = ct;
		if (bases == null) {
			throw new Exception("setBases() first");
			// return ;
		}
		if (loopers != null)
			for (int i = 0; i < getNumLoopers(); i++) {
				if (loopers[i] != null)
					return;// already started

			}
		String target = "HVite2.arm."; // mito

		for (int i = 0; i < getNumLoopers(); i++) {
			loopers[i] = new HViteLooper(bases[i], target + i, ntvpath);

			loopers[i].start();
		}

		for (int i = 0; i < getNumLoopers(); i++) {

			try {
				int mysleep = 20;
				while (loopers[i].isReady() == false) {
					d("sleep wait looper number " + i);
					Thread.currentThread().sleep(1000);
					mysleep--;
					if (mysleep <= 0) {
						stopRecognizer();
						throw new Exception("HVL [" + i
								+ "] failed/too slow TO Running ");

					}
				}
			} catch (InterruptedException e) {

			}

		}

	}

	@SuppressWarnings("unused")
	private static boolean existSame(String src, String targetdir)
			throws Exception {
		// TODO Auto-generated method stub
		return (FileUtils.isSameFile(src, targetdir));

	}
/**
 * set numloopers first
 * @param b
 */
	public static void setBases(String b[]) {
		bases = b;
	}

	public static void stopRecognizer() {
		// instruct HVite2 to stop
		d("stopRecognizer()");
		for (int i = 0; i < getNumLoopers(); i++) {

			if (loopers[i] != null) {
				d("AM: Sending Exit");
				loopers[i].sendCMD("EXIT");
				currScore = -9E10;
				currLine = "_nTok";

			}
			loopers[i] = null;

		}
	}

	public static String parse(String x) {
		// TODO Auto-generated method stub
		d("ASRM:parse(" + x + ")");
		String xs[] = x.split("==");
		xs = xs[0].split(" ");
		String ret = "";
		for (int i = 0; i < xs.length; i++) {
			if (xs[i].indexOf("#NOISE") >= 0) {
				ret += "_";
				continue;
			}
			if (xs[i].indexOf("SILENCE") >= 0) {
				ret += "_n_";
				continue;
			}
			ret += " " + xs[i];
		}
		return ret;
	}

	public static String parseMeaning(String x) {
		// TODO Auto-generated method stub
		d("ASRM:parseMeaning(" + x + ")");
		String[] xs = x.split("==");
		String[] ys = xs[0].split(" ");
		String ret = "";
		for (int i = 0; i < ys.length; i++) {
			if (ys[i].indexOf("N1") >= 0) {
				ret += "1";
				continue;
			}
			if (ys[i].indexOf("N2") >= 0) {
				ret += "2";
				continue;
			}
			if (ys[i].indexOf("N3") >= 0) {
				ret += "3";
				continue;
			}
			ret += "" + ys[i];
		}
		return ret;
	}

	public static String filter(String x, String[] ignores) {
		// TODO Auto-generated method stub
		d("ASRM:filter(" + x + ")");
		String[] ys = x.split(" ");
		String ret = "";
		boolean toignore = false;
		for (int i = 0; i < ys.length; i++) {

			for (int j = 0; j < ignores.length; i++) {

				if (ys[i].indexOf(ignores[j]) >= 0) {
					toignore = true;
					break;

				}
			}
			if (!toignore) {
				ret += " " + ys[i];
			}
		}
		return ret;
	}

	public static int getNumLoopers() {
		return numLoopers;
	}

	public static void setNumLoopers(int numLoopers) throws Exception {
		if (isStarted())
			throw new Exception("Already started, STOP first ");
		ASRManager.numLoopers = numLoopers;
		loopers = new HViteLooper[ASRManager.numLoopers];
		bases = null;
	}

	public static boolean isReady() {
		for (int i = 0; i < getNumLoopers(); i++) {

			if (!loopers[i].isReady())
				return false;

		}
		return true;
	}
	
	public static HViteLooper []getLoopers(){
		return loopers;
	}

}
