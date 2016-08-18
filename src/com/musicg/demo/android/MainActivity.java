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

import java.util.Date;

import com.hatukau.cakadidi.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import hatukau.io.FileExplorer;
import hatukau.speech.ASRListener;
import hatukau.speech.ASRManager;
import hatukau.speech.HViteLooper;
import hatukau.speech.TrainPattern;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends Activity implements RecorderListener {

	public static MainActivity mainApp;

	public static final int DETECT_NONE = 0;
	public static final int DETECT_WHISTLE = 1;
	public static int selectedDetection = DETECT_NONE;

	// detection parameters
	private WavWriterThread writerThread;
	private RecorderThread recorderThread;
	// views
	private View mainView, listeningView, newTestView, testView;
	private Button whistleButton, testButton;

	// @Override
	public static void log(String x) {
		System.out.println("###=> " + x);
	}

	public static void log(Object o) {
		if (o == null)
			log("NULL");
		else
			log(o.toString());
	}

	private String SDCARD = Environment.getExternalStorageDirectory().getPath();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);
		log("MainActivity.onCreate()");
		mainApp = this;

		// set views
		LayoutInflater inflater = LayoutInflater.from(this);
		mainView = inflater.inflate(R.layout.main, null);
		listeningView = inflater.inflate(R.layout.listening, null);
		testView = inflater.inflate(R.layout.testing, null);
		newTestView = inflater.inflate(R.layout.newtesting, null);

		setContentView(mainView);
		log(mainView);
		whistleButton = (Button) this.findViewById(R.id.whistleButton);
		whistleButton.setText("Record Trainer");
		testButton = (Button) this.findViewById(R.id.testButton);

		log(whistleButton);
		whistleButton.setOnClickListener(new ClickEvent());
		testButton.setOnClickListener(new ClickEvent());
		fillFilesList();

	}

	private TextView listLabel;

	public void listhvites(View v) {
		listLabel = (TextView) this.findViewById(R.id.textView1);
		listLabel.setText("HVites List or 3 top processes");

		int[] pids = new int[1];
		boolean found = HViteLooper.killFirst(this, true, pids);

		if (!found)
			listLabel.setText("HVites not Found");
		else {
			listLabel.setText("HVites found with pids=" + pids[0]);
		}

		/*
		 * ListView l = (ListView) MainActivity.mainApp
		 * .findViewById(R.id.filesView);
		 * 
		 * try { ArrayAdapter<String> lad = new ArrayAdapter<String>(this,
		 * R.layout.row, FileExplorer.getDir(SDCARD + "/cakadidi/trains"));
		 * 
		 * if (l != null) l.setAdapter(lad); } catch (Exception ex) {
		 * ex.printStackTrace(); }
		 */

	}

	void popInfo(final String msg) {
		runOnUiThread(new Runnable() {

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

	private void fillFilesList() {
		getAssets();
		ListView l = (ListView) MainActivity.mainApp
				.findViewById(R.id.filesView);

		try {
			ArrayAdapter<String> lad = new ArrayAdapter<String>(this,
					R.layout.row, FileExplorer.getDir(SDCARD
							+ "/cakadidi/trains"));

			if (l != null)
				l.setAdapter(lad);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private boolean isTesting;

	private void goTestView() {
		setContentView(newTestView);

	}

	private void goTestViewOld() {
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		setContentView(testView);
		final String startText = "MULAI";
		final String endText = "BERHENTI";

		final Button recbt = (Button) MainActivity.mainApp
				.findViewById(R.id.htkButton);
		if (recbt == null) {
			log("Can;t instantiate R.id.htkButton");
			return;
		}
		recbt.setText(startText);

		final CheckBox checkFilter = (CheckBox) MainActivity.mainApp
				.findViewById(R.id.checkFilter);

		if (checkFilter == null) {
			log("Can;t instantiate R.id.checkFilter");
			return;
		}

		recbt.setOnClickListener(new OnClickListener() {

			ASRListener al = null;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isTesting) {
					recorderThread = new RecorderThread();
					recorderThread.setRecorderListener(new RecorderListener() {

						@Override
						public void voiceStart() {
							runOnUiThread(new Runnable() {
								public void run() {
									EditText x = (EditText) mainApp
											.findViewById(R.id.output2);
									String info = "	#V#	VOICE PHASE START at "
											+ new Date();
									MainActivity.mainApp.d(info);
									x.setText(info);
									if (writerThread != null) {
										// writerThread.reTarget();
										try {
											writerThread.stopWriting();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											// e.printStackTrace();
										}
										writerThread.reTarget();
										writerThread
												.setBackupBuffer(recorderThread
														.getSilDetInfo());
										writerThread.setSimulateOnly(false);

									}
								}
							});
						}

						@Override
						public void voiceEnd() {
							voiceEnd2(true);
						}

						public void voiceEnd2(final boolean doREC) {
							runOnUiThread(new Runnable() {
								public void run() {
									EditText x = (EditText) mainApp
											.findViewById(R.id.output2);
									String info = "SILENCE #SIL#	PHASE start at "
											+ new Date();
									// x.setText(x.getText() + "	" + info);
									x.setText(info);
									d(info);
									if (writerThread != null) {
										writerThread.setSimulateOnly(true);

										if (doREC)
											try {
												/*
												 * stopWriting is performed
												 * toWWTHif (false)
												 * writerThread.stopWriting();
												 */
												ASRManager.sendCommand("REC");
											} catch (Exception e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
									}
								}
							});
						}

						@Override
						public void absValue(double averageAbsValue) {
							// TODO Auto-generated method stub
							runOnUiThread(new Runnable() {
								public void run() {
									if (recorderThread == null)
										return;
									EditText x = (EditText) mainApp
											.findViewById(R.id.output);
									String savg = recorderThread
											.getCurrAverage() + "";
									String info = "ABS        : "
											+ savg.substring(0, 3);
									info += "Curr Thre        : "
											+ recorderThread.getSilVTreshold();
									info += "   numSCons : "
											+ recorderThread
													.getSamplesConsidered();
									info += "  sumCons  : "
											+ recorderThread.getSumConsidered();

									SildetInfo detail = (SildetInfo) recorderThread
											.getDebugInfo();

									info += ",  HEAD CT	: "
											+ detail.creationTime;
									info += ",  LAST CT	: "
											+ detail.last.creationTime;
									info += ",  H-L		: "
											+ (detail.last.creationTime - detail.creationTime);

									x.setText(info);

								}
							});

						}

						@Override
						public void voiceIsNoise() {
							// TODO Auto-generated method stub
							mainApp.d("voiceIsNoise()");
							voiceEnd2(false);
						}
					});
					recorderThread.setUseSilDet(true);
					EditText et = (EditText) findViewById(R.id.threshold);
					recorderThread.setSilVTreshold(Double.parseDouble(et
							.getText() + ""));
					recorderThread.start();
					writerThread = new WavWriterThread(recorderThread, SDCARD
							+ "/cakadidi/test2.w");
					writerThread.setSimulateOnly(true);// no write at first
					writerThread.start();
					isTesting = true;
					// ASRManager.addListener(new ASRListener() {
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
							// TODO Auto-generated method stub
							final String fl = lines;
							runOnUiThread(new Runnable() {
								public void run() {
									EditText output = (EditText) findViewById(R.id.output3);
									// TextView v =null;
									String ns = ASRManager.parse(fl);
									String filtered = ns;
									if (checkFilter.isChecked()) {
										filtered = ASRManager.filter(ns,
												new String[] { "SIL", "NOI" });

									}
									output.setText(filtered);

									output = (EditText) findViewById(R.id.outFiltered);
									String now = output.getText() + "";
									ns = ASRManager.parseMeaning(fl);

									if (ns.length() > 0) {

										output.setText(now + " " + ns);
									}
								}

							});

						}
					};
					ASRManager.addListener(al);
					try {
						// mainApp.getAssets().open("HVite2.arm");
						ASRManager.setNumLoopers(1);
						ASRManager
								.setBases(new String[] { SDCARD + "/cakadidi" });
						ASRManager.startRecognizer(MainActivity.mainApp,
								getFilesDir().getPath());
						recbt.setText(endText);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						// testingE(e);
						popInfo("Gagal Start ASR : " + e.getMessage());
					}

				} else {
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
						}

						writerThread = null;
					}
					if (al != null)
						ASRManager.removeListener(al);
					ASRManager.stopRecognizer();

					isTesting = false;
					recbt.setText(startText);
				}

			}
		});

	}

	protected void d(String info) {
		System.out.println(info);

	}

	private void goHomeView() {
		setContentView(mainView);

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
			}
			runOnUiThread(new Runnable() {
				public void run() {
					TextView v = (TextView) MainActivity.mainApp
							.findViewById(R.id.textInfo);
					v.setText(writerThread.getInfo());
				}
			});
			writerThread = null;
		}
		selectedDetection = DETECT_NONE;
		fillFilesList();
	}

	private void goListeningView() {
		setContentView(listeningView);
		/*
		 * ArrayAdapter<String> lad = new ArrayAdapter<String>(this,
		 * android.R.layout.simple_list_item_1, new String[] { "Item 1",
		 * "Item 2","Item 3","Item 4" });
		 */
		ArrayAdapter<String> lad = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				TrainPattern.getAllTrainPatternName());

		log("go listening view");
		ListView l = (ListView) this.findViewById(R.id.patterns);
		log(l);
		l.setAdapter(lad);
		// l.setse
		/*
		 * l.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		 * {
		 * 
		 * @Override public void onItemSelected(AdapterView<?> parent, View
		 * view, int position, long id) { // TODO Auto-generated method stub
		 * log("onItemSelected"); patternsOnClick(parent,view);
		 * 
		 * }
		 * 
		 * @Override public void onNothingSelected(AdapterView<?> parent) { //
		 * TODO Auto-generated method stub log("onNothingSelected");
		 * patternsOnClick(parent,null);
		 * 
		 * } });
		 */

		l.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				log("onItemClick");
				patternsOnClick(parent, view);

			}

		});
		/*
		 * l.seto
		 */

		// l.seton

		final Button recbt = (Button) MainActivity.mainApp
				.findViewById(R.id.recordButton);
		recbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isRecording) {
					selectedDetection = DETECT_WHISTLE;

					recorderThread = new RecorderThread();
					recorderThread.setRecorderListener(MainActivity.mainApp);

					recorderThread.start();
					writerThread = new WavWriterThread(recorderThread, null);

					writerThread.setWavName(null);
					writerThread.reTarget();
					writerThread.start();

					isRecording = true;
					recbt.setText("Berhenti");
				} else {

					if (writerThread != null) {

						try {
							writerThread.stopThread();
							writerThread.stopWriting();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						writerThread = null;
					}

					if (recorderThread != null) {
						recorderThread.stopRecording();
						recorderThread = null;
					}

					selectedDetection = DETECT_NONE;
					isRecording = false;
					recbt.setText("Rekam");
				}
			}
		});
	}

	boolean isRecording = false;

	View lastSel = null;
	int bgCol = Color.WHITE;

	void patternsOnClick(AdapterView<?> parent, View v) {
		log(parent);
		int cnt = parent.getCount();
		for (int i = 0; i < cnt; i++) {
			Object o = parent.getItemAtPosition(i);
			log(o.getClass() + " : " + o);
			if (o instanceof TextView) {
				TextView tv = (TextView) o;
				tv.setBackgroundColor(bgCol);
			}
		}
		/*
		 * if (v.getBackground() instanceof ColorDrawable) { ColorDrawable c =
		 * (ColorDrawable) v.getBackground(); // bgCol=c.getColor();
		 * if(bgCol<0)bgCol=c.getColor(); if (lastSel != null&&bgCol!=-1)
		 * lastSel.setBackgroundColor(bgCol);
		 * //lastSel.setBackgroundDrawable(c); lastSel = v;
		 * 
		 * }
		 */
		if (lastSel != null)
			lastSel.setBackgroundColor(bgCol);
		if (v != null)
			v.setBackgroundColor(Color.GREEN);
		lastSel = v;

		// log(v.getClass()+"");
		/*
		 * bgCol=v.getB v.setBackgroundColor(Color.GREEN);
		 * if(lastSel!=null)lastSel.setBackgroundColor(bgcol);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Quit demo");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			finish();
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goHomeView();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class ClickEvent implements OnClickListener {
		public void onClick(View view) {
			if (view == whistleButton) {
				goListeningView();
			}

			if (view == testButton) {
				goTestView();
			}
		}

	}

	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void absValue(double averageAbsValue) {
		// TODO Auto-generated method stub
		final double f = averageAbsValue;
		runOnUiThread(new Runnable() {
			public void run() {
				TextView abs = (TextView) MainActivity.mainApp
						.findViewById(R.id.absEnergy);
				log(abs);

				if (abs != null) {
					String x = f + "";

					abs.setText("AVG ABS: " + x.substring(0, 3) + "");
					// abs.refreshDrawableState();
				}
			}
		});

	}

	@Override
	public void voiceStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void voiceEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void voiceIsNoise() {
		// TODO Auto-generated method stub

	}

	public String getSDCARD() {
		return SDCARD;
	}

	public void hvitetest1w(View v) {

		String DATAS = getFilesDir().getPath();
		String SDCARD = Environment.getExternalStorageDirectory().getPath();
		try {
			String[] bases = new String[] { SDCARD + "/cakadidi/hmm0" };
			ASRManager.setNumLoopers(1);
			ASRManager.setBases(bases);
			ASRManager.addListener(new ASRListener() {

				@Override
				public void recognitionCompleted(String lines) {
					// TODO Auto-generated method stub

				}

				@Override
				public void recognizerExit() {
					// TODO Auto-generated method stub

				}

				@Override
				public void recognizerStarted() {
					// TODO Auto-generated method stub

				}

			});

			ASRManager.startRecognizer(this, DATAS);
			// loopers[0].sendCMD("REC");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void hvitetest1wREC(View v) {
		HViteLooper[] loopers = ASRManager.getLoopers();
		//loopers[0].get
		loopers[0].sendCMD("REC");

	}

	public void hvitetest1wEXIT(View v) {
		HViteLooper[] loopers = ASRManager.getLoopers();
		loopers[0].sendCMD("EXIT");
		ASRManager.stopRecognizer();

	}

}
