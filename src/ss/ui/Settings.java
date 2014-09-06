package ss.ui;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ss.backend.DataBaseHelper;
import ss.client.GlobalVariable;
import ss.client.SimpleCrypto;
import ss.client.acntAuthorization;
import ss.client.GlobalVariable.keys;
import ss.client.acntAuthorization.AccountType;
import ss.flickr.ConfigureFlickr;
import ss.flickr.flickrExecute;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Settings extends Activity {
	public enum SettingOptions {CONFIGURE_ACCOUNT,OTHER_SETTING};
	Context c = this;
	Activity a = this;
	EditText username;
	EditText password;
	List<String> otherSetting;
	List<String> otherSettingSummary;
	List<String> mediaSite;
	List<String> mediaSiteSummary;
	customListAdapter acntAdap;
	ListView LVconf;
	DataBaseHelper myDbHelper;
	CheckBox cb;
	private Boolean isPasswordSet;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		myDbHelper = new DataBaseHelper(c);
		isPasswordSet = myDbHelper.isPassSet();
		if(isPasswordSet)
			checkpassword();
		setContentView(R.layout.settings);
		ListView LVotherSettings = (ListView) findViewById(R.id.othrSettings);
		LVconf = (ListView) findViewById(R.id.confgAccount);
		init_list_configureAccount();
		acntAdap = new customListAdapter(SettingOptions.CONFIGURE_ACCOUNT, mediaSite, mediaSiteSummary);
		LVconf.setAdapter(acntAdap);

		LVconf.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, final View parentView, final int pos, long arg3) {
				cb = (CheckBox) parentView.findViewById(R.id.settingCB);
				cb.toggle();
				if(cb.isChecked()==false)
				{
					AlertDialog ad = new AlertDialog.Builder(c).create();
					ad.setTitle("Unconfigure Account : " + getname(pos));
					ad.setMessage("Are You Sure ?");
					ad.setButton("Yes", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							DataBaseHelper myDbHelper = new DataBaseHelper(c);
							switch(pos){
							case 0: 
								((GlobalVariable) getApplication()).setValue(keys.PICASA_USERNAME,"null" );
								((GlobalVariable) getApplication()).setValue(keys.PICASA_PASS, "null");
								myDbHelper.update("picasa");
								break;
							case 1: 
								((GlobalVariable) getApplication()).setValue(keys.YOU_USERNAME,"null" );
								((GlobalVariable) getApplication()).setValue(keys.YOU_PASS, "null");
								myDbHelper.update("youtube");
								break;
							case 2: 
								((GlobalVariable) getApplication()).setValue(keys.FLICKR_USERNAME,"null" );
								myDbHelper.update("flickr");
							}
							init_list_configureAccount();
							acntAdap.notifyDataSetChanged();
							Toast.makeText(getApplicationContext(), "Account Unconfigured", Toast.LENGTH_LONG).show();
						}
					});
					ad.setButton2("No", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							cb.toggle();
						}
					});
					ad.show();
				}
				else if(cb.isChecked()==true)
				{
					if(pos == 2)
					{
						if(((GlobalVariable) getApplication()).getValue(keys.FLICKR_TOKEN).equals("null"))
						{
							Intent myi = new Intent(c, ConfigureFlickr.class);
							startActivityForResult(myi, 101);
						}
						else
						{
							Intent myi = new Intent(c, ConfigureFlickr.class);
							startActivityForResult(myi, 101);
						}
					}
					else
					{
						final Dialog dnew = new Dialog(c);
						dnew.setContentView(R.layout.user_pass);
						Button btnOk = (Button) dnew.findViewById(R.id.btnOK);
						Button btnCancel = (Button) dnew.findViewById(R.id.btnCancel);
						username = (EditText) dnew.findViewById(R.id.edtemail);
						password = (EditText) dnew.findViewById(R.id.edtpassword);
						username.setText("");
						password.setText("");
						if(pos==0)
							dnew.setTitle("Configue Picasa");
						else if(pos==1)
							dnew.setTitle("Configue Youtube");


						btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								DataBaseHelper myDbHelper = new DataBaseHelper(c);
								String uname=username.getText().toString();

								if(uname.equals(""))
									uname="null";

								String pass=password.getText().toString();
								if(pass.equals(""))
									pass="null";

								switch(pos)
								{
								case 0:
									try {
										pass = SimpleCrypto.encrypt("1a05cb60670ebf7bee2da0e215d0e79b", pass);
										((GlobalVariable) getApplication()).setValue(keys.PICASA_USERNAME, uname );
										((GlobalVariable) getApplication()).setValue(keys.PICASA_PASS, pass);
										acntAuthorization abc = new acntAuthorization(c);
										((GlobalVariable) getApplication()).setValue(keys.PIC_TOKEN, abc.Authorize(AccountType.PICASA));
										myDbHelper.update("picasa");
										String User =((GlobalVariable)c.getApplicationContext()).getValue(keys.PICASA_USERNAME);
										if(User.equals("null")){
											cb.toggle();
											Toast.makeText(c, "Account Initials Invalid", Toast.LENGTH_LONG).show();
										}
										else
										{
											Toast.makeText(c, "Account Configured", Toast.LENGTH_LONG).show();
										}
									} catch (UnknownHostException e) {
										Toast.makeText(c, "Couldnt Connect Picasa Server", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (FileNotFoundException e) {
										Toast.makeText(c, "Invalid Picasa Username and Password", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (SocketTimeoutException e) {
										Toast.makeText(c, "Socket Timeout", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (Exception e) {
										e.printStackTrace();
										cb.toggle();
									}
									myDbHelper.retreive("picasa");
									break;
								case 1:
									try {
										pass = SimpleCrypto.encrypt("1a05cb60670ebf7bee2da0e215d0e79b", pass);
										((GlobalVariable) getApplication()).setValue(keys.YOU_USERNAME, uname);
										((GlobalVariable) getApplication()).setValue(keys.YOU_PASS, pass);
										acntAuthorization abc = new acntAuthorization(c);
										((GlobalVariable) getApplication()).setValue(keys.YOU_TOKEN,abc.Authorize(AccountType.YOUTUBE));
										myDbHelper.update("youtube");

										String User =((GlobalVariable)c.getApplicationContext()).getValue(keys.YOU_USERNAME);
										if(User.equals("null")) {
											cb.toggle();
											Toast.makeText(c, "Account Cleared", Toast.LENGTH_LONG).show();
										}
										else{
											Toast.makeText(c, "Account Configured", Toast.LENGTH_LONG).show();
										}
									} catch (UnknownHostException e) {
										Toast.makeText(c, "Couldnt Connect Youtube Server", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (FileNotFoundException e) {
										Toast.makeText(c, "Invalid Youtube Username and Password", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (SocketTimeoutException e) {
										Toast.makeText(c, "Socket Timeout", Toast.LENGTH_LONG).show();
										cb.toggle();
									} catch (Exception e) {
										e.printStackTrace();
										cb.toggle();
									}
									myDbHelper.retreive("youtube");
									break;
								}
								init_list_configureAccount();
								acntAdap.notifyDataSetChanged();
								dnew.dismiss();
							}
						});

						btnCancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dnew.dismiss();
								cb.toggle();
							}
						});
						dnew.show();
					}
				}
			}

			private String getname(int pos) {
				switch(pos)
				{
				case 0: return "Picasa";
				case 1: return "Youtube";
				case 2: return "Flickr";
				}
				return null;
			}
		});

		init_list_otherSetting();
		LVotherSettings.setAdapter(new customListAdapter(SettingOptions.OTHER_SETTING, otherSetting, otherSettingSummary));

		LVotherSettings.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				if(pos==0)
				{
					final CheckBox cb = (CheckBox) v.findViewById(R.id.settingCB);
					if(cb.isChecked()==false)
					{
						final Dialog dnew = new Dialog(c);
						dnew.setContentView(R.layout.setpassword);

						final EditText passwd = (EditText) dnew.findViewById(R.id.passwd);
						final EditText repasswd = (EditText) dnew.findViewById(R.id.repasswd);

						Button btnOk = (Button) dnew.findViewById(R.id.btnOk);
						Button btnCancel = (Button) dnew.findViewById(R.id.btnCancel);

						dnew.setTitle("Configure ShareSpot");
						btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								if(passwd.getText().toString().equals(""))
									Toast.makeText(c, "Enter a Password", Toast.LENGTH_LONG).show();
								else if(passwd.getText().toString().equals(repasswd.getText().toString()))
								{
									DataBaseHelper myDbHelper = new DataBaseHelper(c);
									String passwrd = flickrExecute.md5(passwd.getText().toString());
									myDbHelper.updateSettings(passwrd);
									dnew.dismiss();
									cb.toggle();
									Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
								}
								else
									Toast.makeText(c, "Passwords Dont Match", Toast.LENGTH_LONG).show();
							}});

						btnCancel.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								dnew.dismiss();
							}
						});
						dnew.setCancelable(false);
						dnew.show();
					}
					else if(cb.isChecked()==true)
					{
						AlertDialog dnew = new AlertDialog.Builder(c).create();
						dnew.setTitle("Turn App Password Off");
						dnew.setMessage("Are you sure ?");
						dnew.setButton("Yes", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								myDbHelper.UnregPass();
								arg0.dismiss();
								cb.toggle();
								Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
							}
						});
						dnew.setButton2("No", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						dnew.setCancelable(false);
						dnew.show();
					}
				}
			}
		});
	}	

	private void checkpassword() {
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
				myDbHelper = new DataBaseHelper(c);
				String passwd = flickrExecute.md5(pass.getText().toString());
				if(passwd.equals(myDbHelper.getPassword()))
				{
					d1.dismiss();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
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

	private void init_list_otherSetting(){
		otherSetting = new ArrayList<String>();
		otherSetting.add("SetPassword");
		otherSettingSummary = new ArrayList<String>();
		otherSettingSummary.add("Turn on/off Application Password");
	}

	private void init_list_configureAccount(){
		mediaSite = new ArrayList<String>();
		mediaSite.add("Configure Picasa");
		mediaSite.add("Configure Youtube");
		mediaSite.add("Configure Flickr");

		mediaSiteSummary = new ArrayList<String>();
		String picUser =((GlobalVariable)c.getApplicationContext()).getValue(keys.PICASA_USERNAME);
		if(picUser.equals("null"))
			mediaSiteSummary.add("Account Not Configured");
		else 
			mediaSiteSummary.add("Configured to "+picUser);

		String youUser =((GlobalVariable)c.getApplicationContext()).getValue(keys.YOU_USERNAME);
		if(youUser.equals("null"))
			mediaSiteSummary.add("Accoutnt Not Configured");
		else
			mediaSiteSummary.add("Configured "+youUser);

		String flickrUser =((GlobalVariable)c.getApplicationContext()).getValue(keys.FLICKR_USERNAME);
		if(flickrUser.equals("null"))
			mediaSiteSummary.add("Accoutnt Not Configured");
		else
			mediaSiteSummary.add("Configured to "+flickrUser);
	}

	class customListAdapter extends BaseAdapter { 
		List <String> title;
		List <String> summary;
		SettingOptions opt;
		int size;

		public customListAdapter(SettingOptions opt,List<String> title,List<String> summary){
			this.title=title;
			this.summary=summary;
			this.opt=opt;
		}

		@Override
		public int getCount() {
			return title.size();
		}

		@Override
		public String getItem(int pos) {
			return title.get(pos);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			if(convertView == null)
			{
				LayoutInflater inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=inflater.inflate(R.layout.settings_content, null);
			}
			TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
			TextView tvSummary = (TextView) convertView.findViewById(R.id.summary);

			ImageView iv = (ImageView) convertView.findViewById(R.id.settingsIV);
			CheckBox cb = (CheckBox) convertView.findViewById(R.id.settingCB);
			if(opt==SettingOptions.CONFIGURE_ACCOUNT)
			{
				tvTitle.setText(mediaSite.get(position));
				tvSummary.setText(mediaSiteSummary.get(position));
				int ResID = 0;
				switch(position){
				case 0: ResID = R.drawable.picasa;
				if(((GlobalVariable) getApplication()).getValue(keys.PICASA_USERNAME).equals("null"))
					cb.setChecked(false);
				else
					cb.setChecked(true);
				break;
				case 1: ResID = R.drawable.youtube32;
				if(((GlobalVariable) getApplication()).getValue(keys.YOU_USERNAME).equals("null"))
					cb.setChecked(false);
				else
					cb.setChecked(true);
				break;
				case 2: ResID = R.drawable.flickr;
				if(((GlobalVariable) getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
					cb.setChecked(false);
				else
					cb.setChecked(true);
				}
				iv.setImageResource(ResID);
			}
			if(opt==SettingOptions.OTHER_SETTING)
			{
				tvTitle.setText(otherSetting.get(0));
				tvSummary.setText(otherSettingSummary.get(0));
				if(myDbHelper.isPassSet())
					cb.setChecked(true);
				else
					cb.setChecked(false);
				iv.setImageResource(R.drawable.modifypass);
			}
			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 102)
		{
			Toast.makeText(getApplicationContext(), "Account Not Configured", Toast.LENGTH_LONG).show();
			cb.toggle();
		}
		else if(resultCode == 110)
		{
			Log.d("Flickr",((GlobalVariable)c.getApplicationContext()).getValue(keys.FLICKR_USERNAME).toString());
			Log.d("Flickr",((GlobalVariable)c.getApplicationContext()).getValue(keys.FLICKR_TOKEN).toString());
			init_list_configureAccount();
			acntAdap.notifyDataSetChanged();
		}
	}
}