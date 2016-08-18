package hatukau.io;

/*import java.io.IOException;
 import java.nio.file.CopyOption;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;

 /*public class CopyFilesNew {

 public static void main(String... aArgs) throws IOException {
 Path FROM = Paths.get("C:\\Temp\\from.txt");
 Path TO = Paths.get("C:\\Temp\\to.txt");
 //overwrite existing file, if exists
 CopyOption[] options = new CopyOption[]{
 StandardCopyOption.REPLACE_EXISTING,
 StandardCopyOption.COPY_ATTRIBUTES
 }; 
 Files.copy(FROM, TO, options);
 }

 } */

/*In older JDKs, however, copying a file involves a lot more code. 
 * It can be done either with FileChannels or with basic streams. 
 * Usually, the FileChannel technique is faster.

 Here's an example showing both techniques.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;

/**
 * Copy files, using two techniques, FileChannels and streams. Using
 * FileChannels is usually faster than using streams.
 */
public final class FileUtils {

	/* Change these settings before running this class. */

	/** The file to be copied. */
	// public static final String INPUT_FILE = "C:\\TEMP\\cottage.jpg";

	/**
	 * The name of the copy to be created by this class. If this file doesn't
	 * exist, it will be created, along with any needed parent directories.
	 */
	// public static final String COPY_FILE_TO = "C:\\TEMP10\\cottage_2.jpg";

	/** Run the example. */
	/*
	 * public static void main(String... aArgs) throws IOException{ File source
	 * = new File(INPUT_FILE); File target = new File(COPY_FILE_TO); CopyFiles
	 * test = new CopyFiles(); test.copyWithChannels(source, target, false);
	 * //test.copyWithStreams(source, target, false); log("Done."); }
	 */

	/** This may fail for VERY large files. */
	private void copyWithChannels(File aSourceFile, File aTargetFile,
			boolean aAppend) {
		log("Copying files with channels.");
		ensureTargetDirectoryExists(aTargetFile.getParentFile());
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			try {
				inStream = new FileInputStream(aSourceFile);
				inChannel = inStream.getChannel();
				outStream = new FileOutputStream(aTargetFile, aAppend);
				outChannel = outStream.getChannel();
				long bytesTransferred = 0;
				// defensive loop - there's usually only a single iteration :
				while (bytesTransferred < inChannel.size()) {
					bytesTransferred += inChannel.transferTo(0,
							inChannel.size(), outChannel);
				}
			} finally {
				// being defensive about closing all channels and streams
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
			}
		} catch (FileNotFoundException ex) {
			log("File not found: " + ex);
		} catch (IOException ex) {
			log(ex);
		}
	}

	public static void copyWithStreams(String INPUT_FILE, String COPY_FILE_TO) {
		File source = new File(INPUT_FILE);
		File target = new File(COPY_FILE_TO);
		// CopyFiles test = new CopyFiles();
		// test.copyWithChannels(source, target, false);
		copyWithStreams(source, target, false);

	}

	public static void copyWithStreams(File aSourceFile, File aTargetFile,
			boolean aAppend) {
		log("Copying files with streams.");
		ensureTargetDirectoryExists(aTargetFile.getParentFile());
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			try {
				byte[] bucket = new byte[32 * 1024];
				inStream = new BufferedInputStream(new FileInputStream(
						aSourceFile));
				outStream = new BufferedOutputStream(new FileOutputStream(
						aTargetFile, aAppend));
				int bytesRead = 0;
				while (bytesRead != -1) {
					bytesRead = inStream.read(bucket); // -1, 0, or more
					if (bytesRead > 0) {
						outStream.write(bucket, 0, bytesRead);
					}
				}
			} finally {
				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
			}
		} catch (FileNotFoundException ex) {
			log("File not found: " + ex);
		} catch (IOException ex) {
			log(ex);
		}
	}

	static void ensureTargetDirectoryExists(File aTargetDir) {
		if (!aTargetDir.exists()) {
			aTargetDir.mkdirs();
		}
	}

	private static void log(Object aThing) {
		System.out.println(String.valueOf(aThing));
	}

	public static void runShellCmd(String[] cmds) throws IOException,
			InterruptedException {
		Process p = Runtime.getRuntime().exec(cmds[0] + "\n");
		DataOutputStream os = new DataOutputStream(p.getOutputStream());
		for (int i = 1; i < cmds.length; i++) {
			String tmpCmd = cmds[i];
			os.writeBytes(tmpCmd + "\n");
		}
		os.writeBytes("exit\n");
		os.flush();
		p.waitFor();
	}

	public static void testAsset(Activity v) {

	}

	/**
	 * check if existing file exist and same in the targetdir ....
	 * 
	 * @param source
	 *            file name;
	 * @param targetdir
	 *            target dir to check
	 * @return
	 */
	public static boolean isSameFile(String src, String targetdir)
			throws Exception {
		// TODO check date and size;

		File fsrc = new File(src);
		if (!fsrc.exists())
			throw new Exception("Source is not exist :" + src);
		;
		if (fsrc.isDirectory())
			throw new Exception(src + "is a directory  not file");
		File ftd = new File(targetdir);
		if (!ftd.exists())
			throw new Exception("Target dir. " + targetdir + " not exist");
		String shortName = fsrc.getName();

		File newF = new File(targetdir + "/" + shortName);
		// if(newF.exists())return false;
		if (newF.lastModified() <= 0) {
			d("not exist:" + newF.getAbsolutePath());
			return false;// not exist

		}

		Date ds = new Date(), dn = new Date();
		ds.setTime(fsrc.lastModified());
		dn.setTime(newF.lastModified());

		if (newF.lastModified() < fsrc.lastModified()) {
			d("different modif. time:" + dn + "   <<< " + ds);
			return false;
		}
		if (newF.length() != fsrc.length())
			return false;
		return true;

	}

	private static void d(String string) {
		System.out.println("FileUtils : " + string);

	}

	/*
	 * get the text file line reader
	 */
	public static ConfigLineReader getLineReader(String fname,
			boolean withChannels) throws Exception {

		File fsrc = new File(fname);
		if (!fsrc.exists())
			throw new Exception("Source is not exist :" + fname);
		;

		if (fsrc.isDirectory())
			throw new Exception(fname + "is a directory  not file");

		final BufferedReader rdr = new BufferedReader(new FileReader(fname));

		ConfigLineReader ret = new ConfigLineReader() {
			public String next() {
				try {
					return rdr.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};

		return null;
	}

	public static void copyFile(String assetPath, String localPath)
			throws IOException {
		debug("copyFile " + assetPath + " = > " + localPath);
		InputStream in = new FileInputStream(assetPath);
		FileOutputStream out = new FileOutputStream(localPath);
		int read;
		byte[] buffer = new byte[4096];
		while ((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		out.close();
		in.close();

	}

	private static void debug(String string) {
		// TODO Auto-generated method stub

		System.out.println("FileUtils.java " + string);

	}

}
