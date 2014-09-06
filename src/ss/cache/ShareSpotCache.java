package ss.cache;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ShareSpotCache<ResponseData> {
	String FileName,RootDirectory;

	public enum cacheType{IMAGE,FEED};
	public ShareSpotCache(String FileName,String RootDirectory){
		this.FileName = FileName;
		this.RootDirectory = RootDirectory;
	}

	/*---------------------get the Cached Content---------------------*/
	@SuppressWarnings("unchecked")
	public ResponseData retriveCache(cacheType type) throws IOException{
		String pathName = this.RootDirectory + File.separator + this.FileName;
		switch(type){
		case IMAGE: 
			return (ResponseData) BitmapFactory.decodeFile(pathName);
		case FEED:	
			StringBuilder text = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(pathName));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			return (ResponseData) text.toString();
		default:	
			return null;
		}
	}

	/*---------------------Save the Cache Content---------------------*/
	public void saveCache(cacheType type,ResponseData content) throws IOException{
		String pathName = this.RootDirectory + File.separator + this.FileName;
		File file = new File(pathName);
		if(!file.exists()){
			if(file.createNewFile())
				Log.d("File", "Created");
			else
				throw new IOException();
		}
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file)); 
		switch(type)
		{
		case IMAGE: 
			Bitmap image = (Bitmap) content;
			image.compress(CompressFormat.JPEG,50,bos);
			break;
		case FEED:	
			String feed = (String) content;
			bos.write(feed.getBytes());
			break;
		default:	
			break;
		}
		bos.flush();
		bos.close();
	}
	/*---------------------delete the cache content---------------------*/
	public void deleteCache(){
		String pathName = this.RootDirectory + File.separator + this.FileName;
		File file = new File(pathName);
		if(file.exists())
			file.delete();
	}
	
	
	public boolean isNotPresent(){
		String pathName = this.RootDirectory + File.separator + this.FileName;
		File file = new File(pathName);
		if(!file.exists())
			return true;
		return false;
	}
}
