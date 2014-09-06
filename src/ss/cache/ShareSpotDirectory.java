package ss.cache;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class ShareSpotDirectory {
	public enum Site{PICASA,YOUTUBE,FLICKR};

	private static boolean isSDCardInserted(){
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;

	}

	public static boolean isRootCreated(){
		File directory = new File("/sdcard/Sharespot");
		if(directory.exists()&&directory.isDirectory())
			return true;
		return false;
	}

	public static void createRootDirectory(Context context){
		if(isSDCardInserted()){
			File directory = new File("/sdcard/Sharespot");
			if(directory.exists()==false)
				if(directory.mkdirs());
		}
		else {
			Toast.makeText(context, "SD CARD not Mounted", Toast.LENGTH_SHORT).show();
		}

	}
	public static void createSiteDirectory(Site site){
		File directory;
		String dirPath;
		switch(site){
			case PICASA :  
			if(isSDCardInserted()){
				dirPath="/sdcard/Sharespot" + File.separator + "Picasa";
				directory = new File(dirPath);
				if(directory.exists()==false)
					directory.mkdirs();
				directory = new File(dirPath + File.separator + "Large");
				if(directory.exists()==false)
					directory.mkdirs();
				directory = new File(dirPath + File.separator + "Small");
				if(directory.exists()==false)
					directory.mkdirs();
			} 
			break;
			case FLICKR :  
				if(isSDCardInserted()){
					dirPath="/sdcard/Sharespot" + File.separator + "Flickr";
					directory = new File(dirPath);
					if(directory.exists()==false)
						directory.mkdirs();
					directory = new File(dirPath + File.separator + "Large");
					if(directory.exists()==false)
						directory.mkdirs();
					directory = new File(dirPath + File.separator + "Small");
					if(directory.exists()==false)
						directory.mkdirs();
				} 
				break;
		}
	}
}
