package ss.cache;

import java.io.InputStream;
import java.net.URL;

import ss.cache.ShareSpotCache.cacheType;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class CachePolicy extends SQLiteOpenHelper{

	private static int diff=20;
	private static String DB_PATH = "/data/data/ss.ui/databases/";
	private static String DB_NAME = "ssdb";
	private static String TABLE_PICASA_CACHE = "picasa_cache";
	private static String TABLE_FLICKR_CACHE = "flickr_cache";
	private SQLiteDatabase myDataBase; 
	@SuppressWarnings("unused")
	private final Context myContext;
	public enum strngs{SITE_PICASA,SITE_FLICKR,TYPE_ALBUM_THUMB,TYPE_IMAGE_THUMB,TYPE_IMAGE_LARGE};
	public enum Cache_Options {ALLOW_CACHE,NO_CACHE};

	public void app_start_policy()
	{
		reinit_cache_table(TABLE_PICASA_CACHE,"album_thumb");
		reinit_cache_table(TABLE_PICASA_CACHE,"image_thumb");
		reinit_cache_table(TABLE_PICASA_CACHE,"image_large");
		reinit_cache_table(TABLE_FLICKR_CACHE,"album_thumb");
		reinit_cache_table(TABLE_FLICKR_CACHE,"image_thumb");
		reinit_cache_table(TABLE_FLICKR_CACHE,"image_large");
	}

	public void reinit_cache_table(String tblname,String type)
	{
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT MAX(cnt) from "+tblname+" WHERE type='"+type+"'", null);
		c.moveToFirst();
		int max=c.getInt(0);
		Log.d("reinit-->", c.getCount()+"--"+max);
		if(max>0){
			String Location;
			Log.d("maxcnt for "+tblname,""+max+"");	
			c = myDataBase.rawQuery("SELECT id from "+tblname +" WHERE cnt<="+ (max-diff) +" AND type='"+type+"'", null);
			if(c.getCount()>0)
			{	
				String id;
				c.moveToFirst();
				do{	
					id=c.getString(0);
					if(tblname.equalsIgnoreCase("picasa_cache")){
						if(type.equalsIgnoreCase("image_thumb")||type.equalsIgnoreCase("album_thumb"))
							Location=CacheLocation.PICASA_ICON;
						else
							Location=CacheLocation.PICASA_IMAGE;
					}else{
						if(type.equalsIgnoreCase("image_thumb")||type.equalsIgnoreCase("album_thumb"))
							Location=CacheLocation.FLICKR_ICON;
						else
							Location=CacheLocation.FLICKR_IMAGE;
					}
					ShareSpotCache<Bitmap> cache = new ShareSpotCache<Bitmap>(id+".ssw",Location);
					cache.deleteCache();
				}while(c.moveToNext());
			}
			myDataBase.execSQL("DELETE FROM "+ tblname +" WHERE cnt<="+ (max-diff) + " AND type='"+type+"'");
			myDataBase.execSQL("UPDATE "+ tblname +" SET cnt=cnt-"+ (max-diff) + " WHERE type='"+type+"'");	
			if(max>100)
				myDataBase.execSQL("UPDATE "+ tblname +" SET cnt=cnt-80 WHERE type='"+type+"'");	
		}
		myDataBase.close();
	}

	public Drawable image_load(strngs website,strngs Type,String id,String URL,Cache_Options cop) throws Exception
	{
		String tablename="",type="";
		String Location = null;
		Drawable d = null;
		switch(Type)
		{
		case TYPE_ALBUM_THUMB:	
			if(website==strngs.SITE_PICASA)
				Location=CacheLocation.PICASA_ICON;
			else if(website==strngs.SITE_FLICKR)
				Location=CacheLocation.FLICKR_ICON;
			type="album_thumb";
			break;
		case TYPE_IMAGE_THUMB:
			if(website==strngs.SITE_PICASA)
				Location=CacheLocation.PICASA_ICON;
			else  if(website==strngs.SITE_FLICKR)
				Location=CacheLocation.FLICKR_ICON;
			type="image_thumb";
			break;
		case TYPE_IMAGE_LARGE:	
			if(website==strngs.SITE_PICASA)
				Location=CacheLocation.PICASA_IMAGE;
			else  if(website==strngs.SITE_FLICKR)
				Location=CacheLocation.FLICKR_IMAGE;
			type="image_large";
			break;
		}
		switch(website)
		{
		case SITE_PICASA:		
			tablename="picasa_cache";
			break;
		case SITE_FLICKR:		
			tablename="flickr_cache";
			break;
		}
		Log.d("hello","b4 switch "+ tablename);

		ShareSpotCache<Bitmap> cache = new ShareSpotCache<Bitmap>(id+".ssw", Location);
		try{
			if(cache.isNotPresent()){
				InputStream is;
				is = (InputStream) new URL(URL).getContent();
				d = Drawable.createFromStream(is, "src name");
				if(cop== Cache_Options.ALLOW_CACHE){
					Bitmap bmp = ((BitmapDrawable)d).getBitmap();
					cache.saveCache(cacheType.IMAGE, bmp);
				}
			}else{
				Bitmap bmp = cache.retriveCache(cacheType.IMAGE);
				d=new BitmapDrawable(bmp);
			}
		}catch(Exception e){
			throw e;
		}
		if(cop==Cache_Options.ALLOW_CACHE){
			openDataBase();
			Cursor c = myDataBase.rawQuery("SELECT * FROM "+tablename+" WHERE id='"+id+"' AND type='"+type+"'", null);
			if(c.getCount()==0)//cache miss
			{
				c = myDataBase.rawQuery("SELECT MAX(cnt) from "+tablename+" WHERE type='"+type+"'", null);
				int cnt;
				c.moveToFirst();
				int max=c.getInt(0);
				if(max>0)
					cnt=max-(diff/2);
				else
					cnt=20;
				Log.d("hula", cnt+"");
				myDataBase.execSQL("INSERT INTO "+ tablename +" VALUES('"+ id + "','" + type + "',"+cnt+")");
			}
			else
			{
				if(cache.isNotPresent()){ //Directory check
					c = myDataBase.rawQuery("SELECT MAX(cnt) from "+tablename+" WHERE type='"+type+"'", null);
					int cnt;
					c.moveToFirst();
					int max=c.getInt(0);
					cnt=max-(diff/2);
					myDataBase.execSQL("UPDATE "+ tablename+ " SET cnt=" + cnt + " WHERE id='"+id+"' AND type='"+type+"'");
				}
				else
					myDataBase.execSQL("UPDATE "+ tablename +" SET cnt=cnt+1 WHERE id='"+ id+"' AND type='"+type+"'");
			}

			myDataBase.close();
		}
		return d;
	}

	public void openDataBase() {
		try{
			//Open the database
			String myPath = DB_PATH + DB_NAME;
			Log.d("myPath",myPath);
			myDataBase=SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		}catch(SQLException e){
			Log.d("SQLException", e.getMessage());
		}
	}

	public CachePolicy(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {

	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}
}
