package ss.picasa;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ss.cache.CachePolicy.strngs;
import ss.client.GlobalVariable;
import ss.client.LoaderImageView;
import ss.client.GlobalVariable.keys;
import ss.ui.Panel;
import ss.ui.R;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class picasaImage extends Activity implements ss.ui.Panel.OnPanelListener {	
	Context c = this;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	String fotocmnt[];
	EditText addCmntTxt;
	Button addCmntBtn;
	String albumID;
	String fotoID,userID;
	String URL;
	String title;
	int pos;
	TextView tv;
	LoaderImageView Iv;
	ListView myCmnt;
	ArrayAdapter<String> cmntadap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imagecontent);

		pos = this.getIntent().getIntExtra("pos",0);
		albumID = this.getIntent().getStringExtra("albumID");
		userID =((GlobalVariable) getApplication()).getImageList(pos).userID;
		fotoID = ((GlobalVariable) getApplication()).getImageList(pos).photoID;
		URL = ((GlobalVariable) getApplication()).getImageList(pos).URL;
		title =((GlobalVariable) getApplication()).getImageList(pos).photoTitle;

		tv = (TextView) findViewById(R.id.phototitle);
		Iv = (LoaderImageView) findViewById(R.id.picimg);

		Panel panel;
		panel = (Panel) findViewById(R.id.topPanel);
		panel.setOnPanelListener(this);
		panel.setInterpolator(new ss.ui.ElasticInterpolator(ss.ui.ElasticInterpolator.Type.OUT,1.0f, 0.3f));

		myCmnt = (ListView) findViewById(R.id.ListComments);
		new Loadcomments().execute("");
		setimage();

		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};

		LinearLayout commentLay = (LinearLayout) findViewById(R.id.CommentLayout);
		if(((GlobalVariable)getApplication()).getValue(keys.PICASA_USERNAME).equals("null"))
		{
			commentLay.setVisibility(View.GONE);
		}
		else
		{
			commentLay.setVisibility(View.VISIBLE);
			addCmntTxt = (EditText) findViewById(R.id.addCommentTxt);
			addCmntBtn = (Button) findViewById(R.id.addCommentBtn);
			addCmntBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(addCmntTxt.getText().equals(""))
					{
						Toast.makeText(getApplicationContext(), "Invalid Comment", Toast.LENGTH_LONG);
					}
					else
					{
						String cmnt = addCmntTxt.getText().toString();
						String CmntXML = "<entry xmlns='http://www.w3.org/2005/Atom'>" +
						"<content>" + cmnt + "</content>" +
						"<category scheme=\"http://schemas.google.com/g/2005#kind\" " +
						"term=\"http://schemas.google.com/photos/2007#comment\"/>" +
						"</entry>";

						HttpURLConnection conn;
						try {
							conn = (HttpURLConnection) new URL("https://picasaweb.google.com/data/feed/api/user/" + userID + "/albumid/" + albumID + "/photoid/" + fotoID).openConnection();
							conn.setRequestMethod("POST");
							conn.setRequestProperty("Authorization", "GoogleLogin auth=" + ((GlobalVariable) getApplication()).getValue(keys.PIC_TOKEN));
							conn.setRequestProperty ("Content-Type", "application/atom+xml");

							conn.setDoOutput(true);
							conn.connect();
							OutputStream os = new BufferedOutputStream(conn.getOutputStream());
							os.write(CmntXML.getBytes());
							os.flush();

							int re = conn.getResponseCode();
							String response = "" + re + "  ";
							BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
							StringBuffer sb1 = new StringBuffer("");
							String line1 = "";
							String NL1 = System.getProperty("line.separator");
							while ((line1 = is.readLine()) != null) {
								sb1.append(line1 + NL1);
							}
							response = response + sb1.toString();
							if(re==201)
							{
								addCmntTxt.setText("");
								new Loadcomments().execute("");
								cmntadap.notifyDataSetChanged();
								Toast.makeText(getApplicationContext(), "Comment Added", Toast.LENGTH_LONG).show();
							}
							else
								Toast.makeText(getApplicationContext(), "Error : " + conn.getResponseMessage() , Toast.LENGTH_LONG).show();

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					int maxpos = ((GlobalVariable) getApplication()).getImageListSize();
					if(pos == maxpos - 1)
					{
						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);
					}
					else
					{
						pos = pos + 1;
						Iv.invalidate();
					}
					userID = ((GlobalVariable) getApplication()).getImageList(pos).userID;
					albumID = ((GlobalVariable) getApplication()).getImageList(pos).albumID;
					fotoID = ((GlobalVariable) getApplication()).getImageList(pos).photoID;
					URL = ((GlobalVariable) getApplication()).getImageList(pos).URL;
					title = ((GlobalVariable) getApplication()).getImageList(pos).photoTitle;
					setimage();
					new Loadcomments().execute("");
				} 
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(pos==0)
					{
						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);;
					}
					else
					{
						pos = pos - 1;
						Iv.invalidate();
					}
					userID = ((GlobalVariable) getApplication()).getImageList(pos).userID;
					albumID = ((GlobalVariable) getApplication()).getImageList(pos).albumID;
					fotoID = ((GlobalVariable) getApplication()).getImageList(pos).photoID;
					URL = ((GlobalVariable) getApplication()).getImageList(pos).URL;
					title = ((GlobalVariable) getApplication()).getImageList(pos).photoTitle;
					setimage();
					new Loadcomments().execute("");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	class Loadcomments extends AsyncTask<String, Void, Void> {		
		int cmntpos;

		@Override
		protected Void doInBackground(String... arg0) {
			try {	 
				String comment = "https://picasaweb.google.com/data/feed/api/user/" + userID + "/albumid/" + albumID + "/photoid/" + fotoID + "?kind=comment";
				Log.d("Com",comment);
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(comment);
				if(!((GlobalVariable)getApplication()).getValue(keys.PICASA_USERNAME).equals("null"))
				{
					request.addHeader("Authorization","GoogleLogin auth=" + ((GlobalVariable) getApplication()).getValue(keys.PIC_TOKEN));
					request.addHeader("GData-Version","2");
				}
				HttpResponse res = client.execute(request);
				BufferedReader input = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = input.readLine()) != null) {
					sb.append(line + NL);
				}
				input.close();
				String CommentFeed = sb.toString();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = new ByteArrayInputStream(CommentFeed.getBytes("UTF-8"));
				Document dom = db.parse(in);      
				Element docEle = dom.getDocumentElement();
				NodeList nl = docEle.getElementsByTagName("entry");
				int cnt = 0;
				if(nl.getLength()==0)
					fotocmnt = null;
				else
					fotocmnt = new String[nl.getLength()];
				while(cnt<nl.getLength()) {
					Element entry = (Element)nl.item(cnt);
					Element cmnt = (Element)entry.getElementsByTagName("content").item(0);
					Element author = (Element) entry.getElementsByTagName("author").item(0);
					Element authname = (Element) author.getElementsByTagName("name").item(0);

					String strcomment,strauthor;
					strauthor = authname.getFirstChild().getNodeValue();
					strcomment = cmnt.getFirstChild().getNodeValue();
					strauthor = strauthor + " : " + strcomment;
					fotocmnt[cnt]= strauthor;
					cnt++;
				}	 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			cmntpos = pos;
			cmntadap = new ArrayAdapter<String>(c, R.layout.comment, R.id.screen);
			cmntadap.add("Loading Comments .......");
			myCmnt.setAdapter(cmntadap);
		}

		@Override
		protected void onPostExecute(Void result) {
			if(cmntpos==pos)
			{
				if(fotocmnt==null)
				{
					cmntadap = null;
					myCmnt.setAdapter(null);
				}
				else
				{
					cmntadap = new ArrayAdapter<String>(c, R.layout.comment, R.id.screen, fotocmnt);
					myCmnt.setAdapter(cmntadap);
				}
			}
		}			
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

	private void setimage() {
		tv.setText(title);
		Iv.setImageDrawable(URL, strngs.SITE_PICASA, c, fotoID);
	}

	public void onPanelClosed(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("Test", "Panel [" + panelName + "] closed");
	}
	public void onPanelOpened(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("Test", "Panel [" + panelName + "] opened");
	}
}