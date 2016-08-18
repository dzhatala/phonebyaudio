package hatukau.speech;

import hatukau.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

class HViteStatus {
	boolean ready;

	synchronized public boolean isReady() {
		return ready;
	}

	synchronized public void setReady(boolean ready) {
		this.ready = ready;
		HViteLooper.instance.debug("myHVS.setReady " + ready);
	}
}
/**
 * HViteLooper hash Tight Coupling with ASR Manager
 * 
 * @author joesmart
 *
 */
@SuppressLint("DefaultLocale")
public class HViteLooper extends Thread {
	static HViteLooper instance=null;
	private String ncmd = "REC";
	String base1 = Environment.getExternalStorageDirectory().getPath()
			+ "/cakadidi";
	@SuppressWarnings("unused")
	private static String ntvpath = "/system/bin"; // native path for hvite2.arm
	final HViteStatus myHViteStatus = new HViteStatus();

	/* no slash compact filenama */
	private static String armName = "HVite2.arm"; //

	/*
	 * private HViteLooper() {
	 * 
	 * }
	 */

	public HViteLooper(String base, String an, String ntvPath) {
		setBase(base);
		setArmName(an);
		instance=this;
	}

	public void setBase(String b) {
		debug("HVL::setBase()==>" + b);
		base1 = b;
	}

	public String getBase() {
		return base1;
	}

	public static boolean killFirst(Context ctx, boolean justFind, int pids[]) {
		debug("FINDING ZOMBIE HVITE2.ARM");
		List<ActivityManager.RunningAppProcessInfo> processes;
		List<ActivityManager.RunningServiceInfo> services;
		ActivityManager amg;
		

		if(ctx==null)ctx=ASRManager.myContext;

		amg = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		// list all running process
		processes = amg.getRunningAppProcesses();
		services = amg.getRunningServices(100);
		int i, size = processes.size();
		boolean found = false;
		int hvitePID = -1;
		debug("Total " + size + " processes is Running ");
		String name = null;
		ntvpath = ctx.getFilesDir().getPath();
		
		String needle = ntvpath + "/" + armName;
		for (i = 0; i < size; i++) {
			name = processes.get(i).processName;
			hvitePID = processes.get(i).pid;
			debug("process " + hvitePID + " " + name);
			if (name.indexOf(needle) >= 0) {
				if (hvitePID != android.os.Process.myPid()) {
					found = true;
					break;
				}
			}

			if (i == size - 1)
				found = false;
		}

		debug("Total " + size + " services is Running ");

		size=services.size();
		for (i = 0; i < size; i++) {
			name = services.get(i).process;
			hvitePID = services.get(i).pid;
			debug("service " + hvitePID + " " + name);
			if (name.indexOf(needle) >= 0) {
				if (hvitePID != android.os.Process.myPid()) {
					found = true;
					break;
				}
			}

			if (i == size - 1)
				found = false;
		}

		try {
			debug("search needle=" + needle + " using /system/bin/ps");
			Process p = Runtime.getRuntime().exec("/system/bin/ps");
			InputStream stdout = p.getInputStream();
			int read, BUFF_LEN = 512;
			byte[] buffer = new byte[BUFF_LEN];
			String out = "";
			while ((read = stdout.read(buffer)) > 0)
				while (true) {
					read = stdout.read(buffer);
					out += new String(buffer, 0, read);
					if (read < BUFF_LEN) {
						// we have read everything
						break;
					}
				}
			p.waitFor();
			if (out.indexOf(needle) >= 0) {
				int idx1 = out.indexOf(needle);
				int idx2 = out.indexOf("\n", idx1);
				String last = out.substring(idx1, idx2);
				debug("needle found is : " + last);
				int backoff = 20;
				int start = idx2 - backoff;
				int idx3 = out.indexOf("\n", start);
				String dbg = out.substring(start, idx2);
				while (idx3 == idx2) {
					debug("backoff search at:" + start);
					debug("bs " + dbg);
					idx3 = out.indexOf("\n", start);
					start = start - backoff;
					Thread.currentThread().sleep(1);
					dbg = out.substring(start, idx2);
				}
				String line = out.substring(idx3 + 1, idx2);
				debug("line is " + line);
				String[] pinfos = line.split(" ");

				debug("splitted length =" + pinfos.length);
				for (i = 0; i < pinfos.length; i++)
					debug("pinfos[" + i + "]=" + pinfos[i]);
				hvitePID = Integer.parseInt(pinfos[3]);
				name = null;
				found = true;

			} else {
				debug("needle " + needle + " not found by /system/bin/ps");
				debug(out);
			}
		} catch (InterruptedException e) {
			debug("WARNING: shell command not finish");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (justFind) {
			if (pids != null)
				if (pids.length > 0) {
					if (found)
						pids[0] = hvitePID;
					else
						pids[0] = -1;

				}
			return found;

		}
		// killing
		if (found) {
			debug("HVite2.arm running with PID=" + hvitePID);
			debug("Killing " + hvitePID);
			android.os.Process.killProcess(hvitePID);
			android.os.Process.sendSignal(hvitePID,
					android.os.Process.SIGNAL_KILL);
			if (name != null) {
				debug("Killing bg " + name);
				amg.killBackgroundProcesses(name);
			}
		} else {
			debug("HVite2.arm not running");
		}

		return found;
	}

	boolean err=false;
	synchronized boolean isErr(){
		return err;
	}
	
	synchronized void setErr(boolean e){
		err=e;
	}
	
	@SuppressLint("DefaultLocale")
	public synchronized void run() {

		// Instruct HVite2 to start here
		// Start HViteLoopHere, no neehd to use JNI because Communication it's
		// performed
		// using TCP IP or socket
		//
		String sdpath = Environment.getExternalStorageDirectory().getPath();
		boolean isErr = false;
		Process p = null;
		// String cmd = executable + " -o S -T 1 -n 10 2  -C " + base1
		killFirst(null,false, null);
		if (killFirst(null,true, null)) {
			debug("can't kill existing zombie ");
			return;
		}
		try {
			String src = getBase() + "/" + armName;
			boolean cek1 = FileUtils.isSameFile(src, ntvpath);
			if (!cek1) {
				debug("Copying binary ... ");
				FileUtils.copyFile(src, ntvpath + "/" + armName);
				debug("COPY SUCCESS ");

			} else {
				debug("using Hvite2.arm old binary ...");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			debug("Error copying to binary folder  :" + e.getMessage());
			e.printStackTrace();
			return;
		}
		String newexecutable = ntvpath + "/" + getArmName();
		debug("setting binary ");
		File mbin = new File(newexecutable);
		mbin.setExecutable(true);
		String transform = " -J xforms mllr1 -J classes ";
		String grammarfactor = " -p -30.0  ";
		/*String cmd = newexecutable + grammarfactor + " -o S -T 1 " + transform
				+ "  -C " + base1 + "/config_hvite_wav -S " + base1
				+ "/word.scp -H " + base1 + "/macros -H " + base1
				+ "/hmmdefs_join -w " + base1 + "/net-cherry.net " + base1
				+ "/cherry.dicts " + base1 + "/cherryphones";
		*/
		String cmd = newexecutable + grammarfactor + " -o S -T 1 " + transform
				+ "  -C " + base1 + "/config_hvite_wav -S " + base1
				+ "/word.scp -H " + base1 + "/allhmm " + " -w " + base1
				+ "/net-cherry.net " + base1 + "/cherry.dicts " + base1
				+ "/cherryphones";
		final String  hashC= hashCode()+" ";
		boolean firstTime = true;
		try {
			debug("HVL: running arm cmd: " + cmd);

			p = Runtime.getRuntime().exec(cmd);
			final BufferedReader inp = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					p.getOutputStream()));
			final BufferedReader err = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			
			Thread respondReader = new Thread() {
				public void run() {
					String line = "";
					debug("Waiting respond from HVite2 .... ");
					while (true&!isErr()) {
						try {
							debug(hashC+"resRdr : waiting ....");
							line = inp.readLine(); // blocking
							if (line == null) {
								hvSleep(1000);
								continue;
							}
							debug(hashC + "resRdr :  resp#: " + line); // that's ok

							if (line.indexOf("READY") >= 0) {
								myHViteStatus.setReady(true);
							}

							if (line.indexOf("frames") > 0) {
								ASRManager.fireEventComplete(this, line);

							}
							if (line.indexOf("EXIT") >= 0) {
								ASRManager.fireEventExit();
								break;
							}
							if (line.indexOf("NOT UNDERSTOOD") >= 0) {
								// print("NU received\n");

							}

							if (line.toUpperCase().indexOf("NO OBSERVATION") >= 0) {
								// print("NU received\n");
								ASRManager.fireEventComplete(this, "_nOS_");

							}

							if (line.toUpperCase().indexOf("NO TOKEN") >= 0) {
								// print("NU received\n");
								ASRManager.fireEventComplete(this, "_nToK_");

							}

						} catch (IOException ex) {
							debug(hashC+ "rsprdr " + ex.getMessage());
							ex.printStackTrace();
							break;
						}
					}

				}
			};

			respondReader.start();

			Thread respondErrReader = new Thread() {
				public void run() {
					while (true&!isErr()) {
						try {
							debug("errRdr : waiting ....");
							String eline = err.readLine(); // blocking
							if (eline == null) {
								hvSleep(1000);
								continue;
							}

							debug("errRdr : errline=" + eline);

							if (eline.indexOf("ERROR") > 0) {
								setErr(true);
								ASRManager.fireEventExit();
								break;
							}

						} catch (Exception ex) {
							debug("errRdr" + ex.getMessage());
							ex.printStackTrace();
							break;
						}

					}
				}

			};

			respondErrReader.start();
			int[] armpids = new int[] { -1 };

			if (true)
				while (!isErr) {
					if (!firstTime) {
						ncmd = "";
						debug("HViteLooper: wait() !");
						if (armpids[0] == -1)
							killFirst(null,true, armpids);
						if (armpids[0] > 0)
							android.os.Process
									.setThreadPriority(
											armpids[0],
											android.os.Process.THREAD_PRIORITY_BACKGROUND);
						wait();

					} else {
						firstTime = false;
					}

					if (ncmd != null)
						if (ncmd.equalsIgnoreCase("EXIT")) {
							out.write(ncmd + "\n");
							out.flush();
							break;
						}

					if (!ncmd.equalsIgnoreCase("") & myHViteStatus.isReady()) {

						debug("Passing " + ncmd + " to HVite2");
						if (armpids[0] > 0)
							android.os.Process
									.setThreadPriority(
											armpids[0],
											android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

						out.write(ncmd + "\n");
						out.flush();
						myHViteStatus.setReady(false);
					} else {
						debug("Not passing : " + ncmd + " ready="
								+ myHViteStatus.isReady());
					}
				}
			p.waitFor();
			/*
			 * out.write( "Second Line...\n" ); out.flush(); line =
			 * inp.readLine(); print("response2: " + line ); // returns an empty
			 * string, if it returns,,, inp.close(); out.close();
			 */
			// p.destroy();;
		} catch (Exception ex) {
			debug("Error running " + ex.getMessage());
			ex.printStackTrace();
			return;
		}
		debug("HViteLooper:Cleaning up ...");
		if (p != null)
			try {

				p.destroy();
			} catch (Exception ex) {
				// already quit
			}
		ASRManager.fireEventExit();

	}

	public static void debug(String s) {
		System.out.println("static HVL:"   + s);
		System.out.flush();
	}

	boolean isReady() {

		return myHViteStatus.isReady();
	}

	public synchronized void sendCMD(String cmd) {
		debug("sendCMD " + cmd);
		ncmd = cmd;
		if (isReady())
			notify();
		else {
			debug("not ready");
		}
	}

	public synchronized void exit() {

		sendCMD("EXIT");
	}

	public String getArmName() {
		return armName;
	}

	public void setArmName(String armName) {
		this.armName = armName;
	}

	private void hvSleep(long l) {
		try {
			debug("hvSleep");
			Thread.currentThread().sleep(l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
