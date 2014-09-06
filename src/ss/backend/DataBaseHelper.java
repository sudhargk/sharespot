package ss.backend;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DataBaseHelper extends SQLiteOpenHelper {

	/*
	 * The Android's default system path of your application database.
	 */
	private static String DB_PATH = "/data/data/ss.ui/databases/";
	private static String DB_NAME = "ssdb";
	private static String TABLE_ACCOUNTS = "account_details";
	private static String APP_ACNT = "app_settings";
	
	private SQLiteDatabase myDataBase; 

	private final Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public void createDataBase() throws IOException{

		boolean dbExist = checkDataBase();

		if(dbExist)
		{
			//do nothing - database already exist
		}
		else
		{ 
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}catch(SQLiteException e){
			e.printStackTrace();
		}
		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */

	private void copyDataBase() throws IOException{
		try{
			//Open your local db as the input stream
			InputStream myInput = myContext.getAssets().open(DB_NAME);

			// Path to the just created empty db
			String outFileName = DB_PATH + DB_NAME;

			//Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);

			//transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer))>0){
				myOutput.write(buffer, 0, length);
			}

			//Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();
		}catch(IOException e){Log.d("IOException",e.getMessage());}
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

	public void retreive(String accountname){
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT username,password FROM " + TABLE_ACCOUNTS + " where Account_name = '" + accountname + "'", null);
		Log.d("count",""+c.getCount()+"");
		c.moveToFirst();
		String username=c.getString(0);
		String passwrd=c.getString(1);

		if(accountname.equals("picasa")){
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.PICASA_USERNAME, username);
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.PICASA_PASS, passwrd);
		}
		if(accountname.equals("youtube")){
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.YOU_USERNAME, username);
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.YOU_PASS, passwrd);
		}
		if(accountname.equals("flickr")){
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.FLICKR_USERNAME, username);
			((GlobalVariable)myContext.getApplicationContext()).setValue(keys.FLICKR_TOKEN, passwrd);
		}
		c.close();
		myDataBase.close();
	}

	public void update(String accountname) {
		openDataBase();
		String username=null;
		String password=null;

		if(accountname.equals("picasa")){
			username=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.PICASA_USERNAME);
			password=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.PICASA_PASS);
		}
		if(accountname.equals("youtube")){
			username=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.YOU_USERNAME);
			password=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.YOU_PASS);
		}
		if(accountname.equals("flickr")){
			username=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.FLICKR_USERNAME);
			password=((GlobalVariable)myContext.getApplicationContext()).getValue(keys.FLICKR_TOKEN);
		}
		myDataBase.execSQL("UPDATE "+ TABLE_ACCOUNTS + " set username='" + username + "', password='"+ password +"' where Account_name='" + accountname + "'");
		myDataBase.close();
	}
	
	public void updateSettings(String passwd) {
		boolean ispass = isPassSet();
		openDataBase();
		if(ispass)
			myDataBase.execSQL("UPDATE "+ APP_ACNT + " set password='" + passwd +"' where AppName='ShareSpot'");
		else
			myDataBase.execSQL("UPDATE "+ APP_ACNT + " set password='" + passwd +"', isPassword=1 where AppName='ShareSpot'");
		myDataBase.close();
	}
	
	public void UnregPass()
	{
		openDataBase();
		myDataBase.execSQL("UPDATE "+ APP_ACNT + " set password='', isPassword=0 where AppName='ShareSpot'");
		myDataBase.close();
	}
	
	public boolean isPassSet() {
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT isPassword FROM " + APP_ACNT + " where AppName = 'ShareSpot'", null);
		Log.d("count","" + c.getCount());
		c.moveToFirst();
		int pass = c.getInt(0);
		c.close();
		myDataBase.close();
		if(pass==0)
			return false;
		return true;
	}
	
	public String getPassword() {
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT password FROM " + APP_ACNT + " where AppName = 'ShareSpot'", null);
		c.moveToFirst();
		String pass = c.getString(0);
		c.close();
		myDataBase.close();
		return pass;
	}

	@Override
	public synchronized void close() {
		if(myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
