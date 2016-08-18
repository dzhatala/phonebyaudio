package hatukau.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

;

//public class FileExplorer extends ListActivity
public class FileExplorer
// extends ListActivity
{

	/**
	 * return file name prefix based on current date time
	 */
	public static String autoFileNamePrefix() {

		String ret = null;
		// java.text.
		// java.
		//underscore can be double clicked
		SimpleDateFormat fmter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		ret=fmter.format(new Date());
		return ret;
	}

	/*
	 * private List<String> item = null;
	 * 
	 * private List<String> path = null;
	 */
	private static String root = "/";

	private TextView myPath;

	/** Called when the activity is first created. */

	/*
	 * @Override
	 * 
	 * public void onCreate(Bundle savedInstanceState) {
	 * 
	 * super.onCreate(savedInstanceState);
	 * 
	 * setContentView(R.layout.main);
	 * 
	 * myPath = (TextView)findViewById(R.id.path);
	 * 
	 * getDir(root);
	 * 
	 * }
	 */

	public static List<String> getDir(String dirPath) throws Exception

	{

		File[] ret = null;

		// myPath.setText("Location: " + dirPath);

		ArrayList<String> item = new ArrayList<String>();

		ArrayList<String> path = new ArrayList<String>();

		File f = new File(dirPath);
		if (!f.isDirectory())
			throw new Exception(dirPath + " is no a directory");
		File[] files = f.listFiles();

		if (!dirPath.equals(root))

		{

			item.add(root);

			path.add(root);

			item.add("../");
			path.add(f.getParent());

		}

		for (int i = 0; i < files.length; i++)

		{

			File file = files[i];

			path.add(file.getPath());

			if (file.isDirectory())

				item.add(file.getName() + "/");

			else

				item.add(file.getName());

		}

		return item;
		/*
		 * ArrayAdapter<String> fileList =
		 * 
		 * new ArrayAdapter<String>(this, R.layout.row, item);
		 * 
		 * setListAdapter(fileList);
		 */
	}

	/*
	 * @Override
	 * 
	 * protected void onListItemClick(ListView l, View v, int position, long id)
	 * throws Exception {
	 * 
	 * 
	 * 
	 * File file = new File(path.get(position));
	 * 
	 * 
	 * 
	 * if (file.isDirectory())
	 * 
	 * {
	 * 
	 * if(file.canRead())
	 * 
	 * getDir(path.get(position));
	 * 
	 * else
	 * 
	 * {
	 * 
	 * new AlertDialog.Builder(this)
	 * 
	 * .setTitle("[" + file.getName() + "] folder can't be read!")
	 * 
	 * .setPositiveButton("OK",
	 * 
	 * new DialogInterface.OnClickListener() {
	 * 
	 * 
	 * 
	 * @Override
	 * 
	 * public void onClick(DialogInterface dialog, int which) {
	 * 
	 * // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * }).show();
	 * 
	 * }
	 * 
	 * }
	 * 
	 * else
	 * 
	 * {
	 * 
	 * new AlertDialog.Builder(this)
	 * 
	 * .setTitle("[" + file.getName() + "]")
	 * 
	 * .setPositiveButton("OK",
	 * 
	 * new DialogInterface.OnClickListener() {
	 * 
	 * 
	 * 
	 * @Override
	 * 
	 * public void onClick(DialogInterface dialog, int which) {
	 * 
	 * // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * }).show();
	 * 
	 * }
	 * 
	 * }
	 */

}