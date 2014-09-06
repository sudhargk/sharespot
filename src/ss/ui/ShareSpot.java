package ss.ui;

import java.io.IOException;
import java.net.UnknownHostException;

import ss.backend.DataBaseHelper;
import ss.cache.CachePolicy;
import ss.cache.ShareSpotDirectory;
import ss.cache.ShareSpotDirectory.Site;
import ss.client.GlobalVariable;
import ss.client.acntAuthorization;
import ss.client.GlobalVariable.keys;
import ss.client.acntAuthorization.AccountType;
import ss.flickr.flickrExecute;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShareSpot extends Activity {

	DataBaseHelper myDbHelper;
	acntAuthorization authorizer;
	Context c = this;
	Activity a = this;
	Boolean isPasswordSet;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		myDbHelper = new DataBaseHelper(c);
		try {
			myDbHelper.createDataBase();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		isPasswordSet = myDbHelper.isPassSet();
		if(isPasswordSet)
			checkpassword();
		else
			new startUp().execute();
		ShareSpotDirectory.createRootDirectory(c);
		ShareSpotDirectory.createSiteDirectory(Site.PICASA);
		ShareSpotDirectory.createSiteDirectory(Site.FLICKR);
	}

	void checkpassword() {
		Button btnOK,btnCancel;
		final Dialog d1;
		d1 = new Dialog(c);
		d1.setTitle("Enter Password");
		d1.setContentView(R.layout.popupmenu);
		d1.setCancelable(false);
		d1.show();
		btnOK = (Button)d1.findViewById(R.id.btnOk);
		btnCancel = (Button)d1.findViewById(R.id.btnCancel);
		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EditText pass = (EditText)d1.findViewById(R.id.password);
				String passwd = flickrExecute.md5(pass.getText().toString());
				myDbHelper = new DataBaseHelper(c);
				if(passwd.toString().equals(myDbHelper.getPassword()))
				{
					d1.dismiss();
					new startUp().execute();
				}
				else
				{
					Toast.makeText(getApplicationContext(), 
							"Invalid Password", 
							Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				d1.dismiss();
				a.finish();
			}
		});	
	}

	private class startUp extends AsyncTask<Void,String, Void>
	{
		private ProgressDialog dialog;
		Boolean errFlag;

		protected void onPreExecute() {
			errFlag=false;
			dialog = new ProgressDialog(c); 
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
		}

		protected Void doInBackground(Void... args) {
			publishProgress("Retrieving Database");
			myDbHelper = new DataBaseHelper(c);
			try {
				myDbHelper.retreive("picasa");
				myDbHelper.retreive("youtube");
				myDbHelper.retreive("flickr");
			} catch(SQLException sqle){
				errFlag=true;
				publishProgress("Error Retrieving Database");
			}
			
			CachePolicy cp = new CachePolicy(c);
			cp.app_start_policy();

			publishProgress("Authenticating Accounts");
			authorizer= new acntAuthorization(c);
			try {
				((GlobalVariable) getApplication()).setValue(keys.YOU_TOKEN,authorizer.Authorize(AccountType.YOUTUBE));
				((GlobalVariable) getApplication()).setValue(keys.PIC_TOKEN,authorizer.Authorize(AccountType.PICASA));
			} catch (UnknownHostException e) {
				errFlag=true;
				publishProgress("No Internet Connection");
			} catch (Exception e) {
				errFlag=true;
				publishProgress("Exception Encountered");
			}
			return null;
		}

		protected void onProgressUpdate(String... args) {
			if(errFlag==false)
				dialog.setMessage(args[0]);
			else
			{	
				if (this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
				this.cancel(true);
				ErrorDialog.UnknownHostDialog(args[0],c,a);
				errFlag=false;
			}
		}

		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				Intent gotoMain = new Intent(c, MainScrn.class);
				startActivity(gotoMain);
				a.finish();
			}
		}
	}
}