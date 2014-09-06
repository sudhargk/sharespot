package ss.picasa;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.ui.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class picasaAddAlbum extends Activity {
	Context c = this;
	EditText edtAlbumName;
	EditText edtSummary;
	EditText edtLocation;
	Spinner spnrAccess;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_album);
		edtAlbumName=(EditText) findViewById(R.id.editAlbumName);
		edtSummary=(EditText) findViewById(R.id.editSummary);
		edtLocation=(EditText) findViewById(R.id.editLocation);
		spnrAccess = (Spinner) findViewById(R.id.spnrAccess);
	
		List<String> access= new ArrayList<String>();
		access.add("private");
		access.add("public");
		spnrAccess.setAdapter(new ArrayAdapter<String>(c,R.layout.comment, R.id.screen,access));

		Button btnSubmit =(Button) findViewById(R.id.submit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new addAlbum().execute();
			}
		});
	}

	private class addAlbum extends AsyncTask<Void, Void, Void>
	{
		private final ProgressDialog dialog = new ProgressDialog(c);
		boolean flag = true;
		int res;

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Creating Album");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (flag) {
				String album = "<entry xmlns='http://www.w3.org/2005/Atom' "
					+ "xmlns:media='http://search.yahoo.com/mrss/' "
					+ "xmlns:gphoto='http://schemas.google.com/photos/2007'> "
					+ "<title type='text'>" + edtAlbumName.getText()+ "</title> "
					+ "<summary type='text'>"+edtSummary.getText()+"</summary> "
					+ "<gphoto:location>"+edtLocation.getText()+"</gphoto:location> "
					+ "<gphoto:access>"+spnrAccess.getItemAtPosition(spnrAccess.getSelectedItemPosition())
					+"</gphoto:access> "
					+ "<media:group> "
					+ "<media:keywords>Trying</media:keywords> "
					+ "</media:group> "
					+ "<category scheme='http://schemas.google.com/g/2005#kind' "
					+ "term='http://schemas.google.com/photos/2007#album'></category> "
					+ "</entry>";

				String AUTH_TOKEN=((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN);

				HttpURLConnection conn;
				try {
					conn = (HttpURLConnection) new URL("https://picasaweb.google.com/data/feed/api/user/default").openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Authorization", "GoogleLogin auth=" + AUTH_TOKEN);
					conn.setRequestProperty ("Content-Type", "application/atom+xml");

					conn.setDoOutput(true);
					conn.connect();
					OutputStream os = new BufferedOutputStream(conn.getOutputStream());
					os.write(album.getBytes());
					os.flush();
					res = conn.getResponseCode();
					Log.d("ResCode",String.valueOf(res));
				} catch (IOException e) {
					Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG);
					res = 102;
				}
				flag = false;
			}
			flag = true;
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if (this.dialog.isShowing())
				this.dialog.dismiss();
			if(res == 201)
				Toast.makeText(c, "Album Created", Toast.LENGTH_LONG).show();
			setResult(res);
			finish();
		}
	}
}