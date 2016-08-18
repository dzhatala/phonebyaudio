package hatukau.speech;

import java.io.IOException;

import hatukau.io.FileExplorer;
import hatukau.io.FileUtils;

import com.hatukau.cakadidi.R;
import com.musicg.demo.android.MainActivity;
import com.musicg.demo.android.RecorderListener;
import com.musicg.demo.android.RecorderThread;
import com.musicg.demo.android.WavWriterThread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Speech Recognition Input Method Service
 * @author joe
 *
 */
public class SRImeService extends InputMethodService implements
		OnKeyboardActionListener {

	private KeyboardView kv;
	private Keyboard keyboard;
	public static String SDCARD = null;
	public static SRImeService instance = null;
	public String DATAS = null;
	private boolean caps = false;
	private TextView info1 = null;
	private TextView info2 = null;
	private View indicator = null;

	private CheckBox useNumbers = null;
	private CheckBox useLetters = null;
	private CheckBox useSentences = null;
	private CheckBox useBackup = null;

	private SeekBar gainBar = null;
	private SeekBar threshBar = null;

	UndoInfo undoInfo = new UndoInfo();

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		InputConnection ic = getCurrentInputConnection();
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:
			ic.deleteSurroundingText(1, 0);
			break;
		case Keyboard.KEYCODE_SHIFT:
			caps = !caps;
			keyboard.setShifted(caps);
			kv.invalidateAllKeys();
			break;
		case Keyboard.KEYCODE_DONE:
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_ENTER));
			break;
		default:
			char code = (char) primaryCode;
			if (Character.isLetter(code) && caps) {
				code = Character.toUpperCase(code);
			}
			ic.commitText(String.valueOf(code), 1);
		}
	}

	@Override
	public void onPress(int primaryCode) {

	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeUp() {
	}

	public View onCreateInputView1() {
		d("onCreateInputView1");
		kv = (KeyboardView) getLayoutInflater()
				.inflate(R.layout.keyboard, null);
		keyboard = new Keyboard(this, R.layout.qwerty);
		kv.setKeyboard(keyboard);
		kv.setOnKeyboardActionListener(this);
		DATAS = getFilesDir().getPath();
		SDCARD = Environment.getExternalStorageDirectory().getPath();
		d("DATAS=" + DATAS);
		d("SDCARD=" + SDCARD);
		/*
		 * Thread simulator = new Thread() { public void run() { try {
		 * Instrumentation inst = new Instrumentation();
		 * debug(" injecting ... "); for (int i = 0; i < 1; ++i) {
		 * 
		 * InputConnection ic = getCurrentInputConnection(); ic.commitText("a",
		 * 1); ic.commitText("w", 1); ic.commitText("i", 1); ic.commitText("l",
		 * 1); Thread.sleep(2000); } } catch (InterruptedException e) { } } };
		 * 
		 * simulator.start();
		 */
		return kv;
	}

	final int MAX_GAIN = 9;
	final int GAIN_INIT = 4;

	void alterGain(int progress) {

		double gain = 1.0;
		if (progress != GAIN_INIT) {

			double factor = (double) progress - (double) GAIN_INIT;
			factor = factor / (MAX_GAIN - GAIN_INIT);
			gain += factor;

		}

		d("setGain " + gain);
		recorderThread.setGain(gain);

	}

	void alterSILVT(int progress) {

		double silvt = mythreshold;
		if (progress != GAIN_INIT) {
			double factor = (double) progress - (double) GAIN_INIT;
			factor = factor / (MAX_GAIN - GAIN_INIT);
			silvt += mythreshold * factor;
		}
		d("setSILVT " + silvt);
		recorderThread.setSilVTreshold(silvt);
	}

	public View onCreateInputView() {
		d("onCreateInputView");
		DATAS = getFilesDir().getPath();
		SDCARD = Environment.getExternalStorageDirectory().getPath();
		d("DATAS=" + DATAS);
		d("SDCARD=" + SDCARD);
		indicator = getLayoutInflater().inflate(R.layout.indicator, null);
		if (indicator != null) {
			info1 = (TextView) indicator.findViewById(R.id.info1);
			info2 = (TextView) indicator.findViewById(R.id.info2);
			gainBar = (SeekBar) indicator.findViewById(R.id.gainBar);
			threshBar = (SeekBar) indicator.findViewById(R.id.threshBar);

			useNumbers = (CheckBox) indicator.findViewById(R.id.useNumbers);
			useLetters = (CheckBox) indicator.findViewById(R.id.useLetters);
			useSentences = (CheckBox) indicator.findViewById(R.id.useWords);
			useBackup = (CheckBox) indicator.findViewById(R.id.useBackup);

			CompoundButton.OnCheckedChangeListener usesListener = new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					stopRecognizer();
					while (started) {
						d("button checked, wait to stop before restarted ..");
						try {
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					startRecognizer();
				}
			};

			useNumbers.setOnCheckedChangeListener(usesListener);
			useLetters.setOnCheckedChangeListener(usesListener);
			useSentences.setOnCheckedChangeListener(usesListener);

			useBackup.setChecked(false);
			useSentences.setEnabled(false);
			useNumbers.setChecked(true);
			useLetters.setChecked(false);

			threshBar.setMax(MAX_GAIN);
			threshBar.setProgress(GAIN_INIT);
			gainBar.setMax(MAX_GAIN);
			gainBar.setProgress(GAIN_INIT);

			gainBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					d("gain bar progress changed to " + progress);
					alterGain(progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

			});

			threshBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					d("silv bar progress changed to " + progress);
					alterSILVT(progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

			});

		}
		return indicator;
	}

	public void onFinishInput() {
		d("onFinishInput()");
		stopRecognizer();
		super.onFinishInput();
	}

	public void onStartInputView(EditorInfo info, boolean restarting) {
		d("onStartInputView");

		if (!started) {
			startRecognizer();
		}
		super.onStartInputView(info, restarting);

	}

	static void d(String x) {
		System.out.println("SRI " + x);
	}

	class LongHelper {

		private long value = 0;

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}

	}

	final LongHelper timer1 = new LongHelper();
	boolean recThStabilized = true;
	double lastABSValue = 0;

	enum SRIState {
		DETECTION_STATE, RECORDING_STATE, PROCESSING_STATE, STOPPED_STATE,STOPPING_STATE,STARTING_STATE
	}

	SRIState currState = SRIState.STOPPED_STATE;
	String[] hmmbases = null; // hmmbases
	private boolean useSpectralSubstraction = true;
	private String wavfilename = "/cakadidi/test1.w";

	protected void startRecognizer() {
 
		if(false){
			d("startRecognizer() disabled!");
			return ;
		}
		d("Starting recognizer ...");
		currState=SRIState.STARTING_STATE;
		infoNonBlocking(-1);
		recorderThread = new RecorderThread();
		recorderThread.setRecorderListener(new RecorderListener() {

			@Override
			public void voiceStart() {
				d("voiceSTART");
				if (writerThread != null) {
					// writerThread.reTarget();
					try {
						writerThread.stopWriting();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						errorInfo(e.getMessage());
					}
					writerThread.reTarget();
					writerThread.setBackupBuffer(recorderThread.getSilDetInfo());
					writerThread.setSimulateOnly(false);

				}
				infoVoiceDetected();

			}

			@Override
			public void voiceEnd() {
				d("voiceEND");
				vE1(false);

			}

			@Override
			public void absValue(final double averageAbsValue) {
				final long wait = 1000;
				lastABSValue = averageAbsValue;
				Thread runner = new Thread() {
					public void run() {
						long now = System.currentTimeMillis();
						if (now - timer1.getValue() > wait
								& recorderThread != null) {
							d("ABS:" + averageAbsValue + " THRESHOLD :"
									+ recorderThread.getSilVTreshold());
							timer1.setValue(now);
							infoNonBlocking(averageAbsValue);
						}
					}
				};

				runner.start();

			}

			@Override
			public void voiceIsNoise() {

				vE1(false);
			}
		});
		al = new ASRListener() {

			@Override
			public void recognizerStarted() {
				// TODO Auto-generated method stub

			}

			@Override
			public void recognizerExit() {
				// TODO Auto-generated method stub

			}

			@Override
			public void recognitionCompleted(String lines) {
				r1(lines);
			}
		};
		ASRManager.addListener(al);

		try {
			hmmbases = fixBases();
			if (hmmbases == null) {
				stopRecognizer();
				return;

			}
			if (hmmbases.length == 0) {
				stopRecognizer();
				return;
			}
			recorderThread.setUseSilDet(false);
			recorderThread.start();
			while (!recorderThread.isRecording()) {
				Thread.currentThread().sleep(1000);

			}
			currState = SRIState.RECORDING_STATE;
			String fname = cleanWavFileName;
			if (useSpectralSubstraction) {
				fname = wavfilename;
			}
			writerThread = new WavWriterThread(recorderThread, SDCARD + fname);
			writerThread.setSimulateOnly(true);// no write at first
			writerThread.start();// consume recorderThread

			double sumSil = 0;

			while (silMeasurementCounter > 0) {
				d("Measuring Silence");
				Thread.currentThread().sleep(1000);
				sumSil += lastABSValue;
				silMeasurementCounter--;
			}

			mythreshold = 2.5 * (sumSil / SIL_MEASURE_SECONDS);
			recorderThread.setSilVTreshold(mythreshold);
			recorderThread.setUseSilDet(true);

			ASRManager.setNumLoopers(hmmbases.length);
			ASRManager.setBases(hmmbases);
			ASRManager.startRecognizer(this, getFilesDir().getPath());
			started = true;
		} catch (Exception e) {
			errorInfo("SRI: " + e.getMessage());
			e.printStackTrace();
			stopRecognizer();
		}

	}

	private static WavWriterThread writerThread;
	private static RecorderThread recorderThread;
	ASRListener al = null;
	boolean started = false;
	final int SIL_MEASURE_SECONDS = 2;
	int silMeasurementCounter = SIL_MEASURE_SECONDS; //
	private int counterThresReset=0;
	private double sumEnergy=0.0;

	protected void stopRecognizer() {
		d("Stoping recognizer ... ");
		if (!started) {
			d("stRg not started forcing ..");
			//return;
		}
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (writerThread != null) {

			try {
				writerThread.stopThread();
				writerThread.stopWriting();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errorInfo(e.getMessage());
			}

			writerThread = null;
		}
		if (al != null)
			ASRManager.removeListener(al);
		ASRManager.stopRecognizer();
		started = false;
		silMeasurementCounter = SIL_MEASURE_SECONDS; //
		currState=SRIState.STOPPED_STATE;
		infoNonBlocking(-1.0);
	}

	private void infoVoiceDetected() {
		final Handler handler = new Handler(Looper.getMainLooper());
		if (info1 != null) {

			handler.post(new Runnable() {
				public void run() {
					info1.setText("Terdeteksi!");
				}
			});
		}

		if (info2 != null) {

			handler.post(new Runnable() {
				public void run() {
					info2.setText(" Merekam ... !");
				}
			});
		}

	}
	/**
	 * non blocking ..
	 * @param d
	 */
	private void infoNonBlocking(final double d) {

		Thread runner=new Thread(){
			public void run(){
				renderABS1(d);
			}
		};
		
		runner.start();
	}
		private void renderABS1(double currEnergy) {
		final Handler handler = new Handler(Looper.getMainLooper());
		if (currState == SRIState.STARTING_STATE) {
			if (info1 != null) {

				handler.post(new Runnable() {
					public void run() {
						info1.setText("Sedang ");
					}
				});

			}

			if (info2 != null) {

				handler.post(new Runnable() {
					public void run() {
						info2.setText(" Memulai");
					}
				});
			}

			return;
		}

		if (currState == SRIState.PROCESSING_STATE) {
			if (info1 != null) {
				final String ds = currEnergy + "     ";

				handler.post(new Runnable() {
					public void run() {
						info1.setText("Sedang ");
					}
				});

			}

			if (info2 != null) {

				handler.post(new Runnable() {
					public void run() {
						info2.setText(" Menerjemah");
					}
				});
			}

			return;
		}
		
		
		if (currState == SRIState.STOPPED_STATE) {
			if (info1 != null) {

				handler.post(new Runnable() {
					public void run() {
						info1.setText("Sedang ");
					}
				});

			}

			if (info2 != null) {

				handler.post(new Runnable() {
					public void run() {
						info2.setText(" Berhenti");
					}
				});
			}

			return;
		}

		
		// debug("renderABS " + currEnergy);
		if (!started)
			return;

		counterThresReset++;
		sumEnergy+=currEnergy;
		
		if(counterThresReset>1){
			recorderThread.setSilVTreshold(2.5*sumEnergy/counterThresReset);
			counterThresReset=0;
			sumEnergy=0;
			
		}
		
		if (currState == SRIState.RECORDING_STATE) {
			if (info1 != null) {
				final String ds = currEnergy + "     ";
				String gs1 = recorderThread.getGain() + "     ";
				final String gs = gs1.substring(0, 4);

				handler.post(new Runnable() {
					public void run() {
						info1.setText("Energi(x" + gs + "): "
								+ ds.substring(0, 4));
					}
				});
			}
			if (info2 != null) {

				String s1 = recorderThread.getSilVTreshold() + "     ";
				final String s2 = s1.substring(0, 4);
				handler.post(new Runnable() {
					public void run() {
						info2.setText(" Batas: " + s2);
					}
				});
			}

		}

	}

	private void doEraseAll() {

		d("doEraseAll");
		InputConnection ic = getCurrentInputConnection();
		doSelectAll();
		ic.commitText("", 1);

	}

	private void doCopy() {

		d("doCopy");
		InputConnection ic = getCurrentInputConnection();
		ic.performContextMenuAction(android.R.id.copy);

	}

	private void doPaste() {

		d("doPaste");
		InputConnection ic = getCurrentInputConnection();
		ic.performContextMenuAction(android.R.id.paste);

	}

	private void doSelectAll() {
		d("doSelectAll");
		InputConnection ic = getCurrentInputConnection();
		ic.performContextMenuAction(android.R.id.selectAll);

	}

	private void doCopyAll() {
		doSelectAll();
		doCopy();
	}

	private void doErase() {
		d("doErase");
		InputConnection ic = getCurrentInputConnection();
		ic.deleteSurroundingText(1, 0);

	}

	private void doMultipleErase(MultipleEraseInfo mei) {

		d("doMEI " + mei.times + " times");
		if (0 < mei.times & mei.times <= 9)
			for (int i = 0; i < mei.times; i++) {
				doErase();
			}

	}

	/**
	 * 
	 * @param lines
	 *            htk lines with format SIL <TOKS> SIL
	 */
	private void parse(String lines) {
		d("parseLines :  " + lines);
		if (lines != null) {
			String[] xs = lines.split("==");
			String[] ys = xs[0].split(" ");

			// multiple token first;
			d("ys[].length " + ys.length);

			MultipleEraseInfo mei = SRKeyboard.getMultipleEraseCommand(ys);

			if (mei != null) {

				doMultipleErase(mei);
				return;
			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.UNDO) {
				doUndo();
				return;

			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.SELECT_ALL) {
				doSelectAll();
				return;

			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.ERASE_ALL) {
				doEraseAll();
				return;

			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.COPY_ALL) {
				doCopyAll();
				return;

			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.COPY) {
				doCopy();
				return;

			}

			if (SRKeyboard.getCommand(ys) == SRKeyboard.PASTE) {
				doPaste();
				return;

			}
			int[] redoKeys = new int[ys.length];
			int j = 0;
			for (int i = 0; i < ys.length; i++) {

				int key = SRKeyboard.getCommand(ys[i]);

				if (key != 0) {
					doKey(key);
					redoKeys[j] = key;
					j++;
					continue;
				}

				key = SRKeyboard.getChar(ys[i]);
				if (key != 0) {
					doKey(key);
					redoKeys[j] = key;
					j++;
					continue;
				}
				continue;
			}

			if (j > 0) {
				undoInfo.postUndoKeys(redoKeys, j);
			}

		}
	}

	private void doUndo() {
		d("doUndo lastType=" + undoInfo.getLastUndoType());
		int type = undoInfo.getLastUndoType();

		if (type == UndoInfo.UNDO_KEYS) {
			cursorMoveRightest();
			InputConnection ic = getCurrentInputConnection();
			for (int i = 0; i < undoInfo.getKeys2Undo().length; i++) {
				ic.deleteSurroundingText(1, 0);
			}

			undoInfo.postRedoKeys(undoInfo.getKeys2Undo(),
					undoInfo.getKeys2Undo().length);
		}

		if (type == UndoInfo.REDO_KEYS) {
			cursorMoveRightest();
			InputConnection ic = getCurrentInputConnection();
			int[] keys = undoInfo.getKeys2Redo();
			for (int i = 0; i < keys.length; i++) {
				doKey(keys[i]);
			}

			undoInfo.postUndoKeys(keys, keys.length);
		}

	}

	private void cursorMoveRightest() {
		// TODO Auto-generated method stub
		d("Warn cursorMoveRightest() NOT implemented");
	}

	private void doKey(int primaryCode) {
		d("doKey() " + primaryCode);
		InputConnection ic = getCurrentInputConnection();
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:

			CharSequence cs = ic.getSelectedText(0);
			if (cs != null) {
				doEraseAll();
				break;
			}
			doErase();
			break;
		case Keyboard.KEYCODE_SHIFT:
			caps = !caps;
			// keyboard.setShifted(caps);
			// kv.invalidateAllKeys();
			break;
		case Keyboard.KEYCODE_DONE:
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_ENTER));
			break;
		default:
			char code = (char) primaryCode;
			if (Character.isLetter(code) && caps) {
				code = Character.toUpperCase(code);
			}

			if (Character.isLetter(code) && !caps) {
				code = Character.toLowerCase(code);
			}
			ic.commitText(String.valueOf(code), 1);
		}
	}

	private boolean commitCommand(String tok) {

		return false;
	}

	/*
	 * private void commitToken(String tok) {
	 * 
	 * // TODO Auto-generated method stub InputConnection ic =
	 * getCurrentInputConnection(); String tc = SRKeyboard.getText(tok); if (tc
	 * != null) { ic.commitText(tc, 1); return; }
	 * 
	 * }
	 */
	boolean backup = true;

	private void doBackup() {
		if (useBackup.isChecked()) {
			String gs = recorderThread.getGain() + "";

			gs = gs.length() >= 4 ? gs.substring(0, 4) : gs.substring(0,
					gs.length());
			int y = gs.indexOf(".");
			String drgs = "";
			if (y >= 0) {
				String left = gs.substring(0, y);
				String right = gs.substring(y + 1, gs.length());
				drgs = left + "dot" + right;
			}

			gs = gs.length() >= 3 ? gs.substring(0, 3) : gs.substring(0,
					gs.length() - 1);
			if (writerThread != null) {
				// String ori = writerThread.getWavName();
				String dirty = SDCARD + wavfilename;
				String clean = SDCARD + cleanWavFileName;
				String newName = SDCARD + "/cakadidi/trains/zcherry_g_" + drgs
						+ "_" + FileExplorer.autoFileNamePrefix() + ".wav";
				String newName2 = SDCARD + "/cakadidi/trains/zcherry_g_" + drgs
						+ "_" + FileExplorer.autoFileNamePrefix() + "_cl.wav";
				try {
					d("doBackup " + clean + "-->" + newName2);
					FileUtils.copyFile(clean, newName2);
					/*
					 * if (useSpectralSubstraction) { d("doBackup " + dirty +
					 * "-->" + newName); FileUtils.copyFile(dirty, newName); }
					 */
				} catch (IOException e) {
					// TODO Auto-generated catch block
					errorInfo(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	void popInfo(final String msg) {
		d(msg);

		final Handler handler = new Handler(Looper.getMainLooper());

		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				AlertDialog ad = new AlertDialog.Builder(MainActivity.mainApp)
						.create();
				ad.setCancelable(false); // This blocks the 'BACK' button
				ad.setMessage(msg);
				ad.setButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				ad.show();

			}
		});

	}

	void errorInfo(String emsg) {
		stopRecognizer();
		popInfo("error" + emsg);
	}

	private double mythreshold = 25.0;
	private String cleanWavFileName = "/cakadidi/test2.w";

	void r1(String lines) {
		recThStabilized = false;
		recorderThread.startRecording();
		currState = SRIState.RECORDING_STATE;
		d(" rc " + lines);

		if (lines != null) {
			if (lines.indexOf("UNBUSYFORCE") < 0)
				parse(lines);
		}

		try {
			Thread.currentThread().sleep(2000);
			int wait = 5;
			while (wait > 0) {
				double curr = recorderThread.getCurrAverage();
				double th = recorderThread.getSilVTreshold();
				d("wait recorderThread to stabilized" + curr + "  > " + th);
				Thread.currentThread().sleep(1000);
				if (curr < th)
					break;
				wait--;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorInfo(e.getMessage());
		}
		recorderThread.setUseSilDet(true);

		recThStabilized = true;

	}

	void vE1(boolean noise) {
		if (!recThStabilized) {
			d("VE return RecorderThread not stabilized  ");
			return;
		}

		if (ASRManager.isReady()) {
			d("VE return ASRM not ready");

		}
		if (writerThread != null) {

			try {
				writerThread.setSimulateOnly(true);
				writerThread.stopWriting();
				recorderThread.setUseSilDet(false);
				recorderThread.stopRecording();
				if (recorderThread.isRecording()) {
					errorInfo("can't stop recording ");
					return;
				}
				if (noise)
					return;
				if (!ASRManager.isReady())
					return;
				currState = SRIState.PROCESSING_STATE;
				if (useSpectralSubstraction) {
					SpectralSubstraction.perform(SDCARD + wavfilename, SDCARD
							+ cleanWavFileName);
				}
				d("Sending REC to ASRM ");
				ASRManager.sendCommand("REC");
				Thread runner = new Thread() {
					public void run() {
						doBackup();// using another thread for backup ;
					}
				};
				runner.start();

			} catch (Exception e) {
				// TODO Auto-generated catch
				// block
				errorInfo(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * hmm0 always for NUMBER_CMD hmm1 always for LETTERS hmm2 always for WORDS
	 * 
	 * @param bases
	 */
	String[] fixBases() {
		String[] bases = null;
		int numbases = 1;
		if (useNumbers.isChecked())
			numbases++;
		if (useLetters.isChecked())
			numbases++;
		if (useSentences.isChecked())
			numbases++;

		// default if no check
		if (numbases == 0) {
			return null;
		}

		bases = new String[numbases];

		int idx = 0;
		bases[idx] = SDCARD + "/cakadidi" + "/hmm0";
		idx++;
		if (useNumbers.isChecked()) {
			bases[idx] = SDCARD + "/cakadidi" + "/hmm1";
			idx++;
		}

		if (useLetters.isChecked()) {
			bases[idx] = SDCARD + "/cakadidi" + "/hmm2";
			idx++;
		}

		if (useSentences.isChecked()) {
			bases[idx] = SDCARD + "/cakadidi" + "/hmm3";
			idx++;
		}

		return bases;

	}
	
	public static void hvitetest1w(){
		/*SRImeService srv=new SRImeService();
		srv.onCreateInputView();
		srv.setNumlooper(1);*/
	}
}
