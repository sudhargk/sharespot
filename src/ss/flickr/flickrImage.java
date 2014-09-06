package ss.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ss.cache.CachePolicy.strngs;
import ss.client.GlobalVariable;
import ss.client.LoaderImageView;
import ss.client.GlobalVariable.keys;
import ss.ui.Panel;
import ss.ui.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class flickrImage extends Activity implements ss.ui.Panel.OnPanelListener {	
	ArrayList<String> fotocmnt;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	EditText addCmntTxt;
	TextView title;
	LoaderImageView pic;
	ArrayAdapter<String> adapter;
	Button addCmntBtn,addToSet,deleteFromSet;
	int pos;
	String phototitle,photoid,photoURL;
	private String token;
	String signature,request,response;
	ListView myCmnt; 
	Activity a = this;
	Context c;

	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flickrimagecontent);
		c = this;
		token = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_TOKEN);

		pos = this.getIntent().getIntExtra("pos", 0);
		photoid = flickrImageViewer.imageList.get(pos).photoID;
		phototitle = flickrImageViewer.imageList.get(pos).photoTitle;
		photoURL = flickrImageViewer.imageList.get(pos).photoURL;

		title = (TextView) findViewById(R.id.phototitle);
		addCmntTxt = (EditText) findViewById(R.id.addCommentTxt);
		addCmntBtn = (Button) findViewById(R.id.addCommentBtn);
		addToSet = (Button) findViewById(R.id.addtoSet);
		pic = (LoaderImageView) findViewById(R.id.picimg);
		deleteFromSet = (Button) findViewById(R.id.deletefromset);
		myCmnt = (ListView) findViewById(R.id.ListComments);

		Panel panel;
		panel = (Panel) findViewById(R.id.topPanel);
		panel.setOnPanelListener(this);
		panel.setInterpolator(new ss.ui.ElasticInterpolator(ss.ui.ElasticInterpolator.Type.OUT,1.0f, 0.3f));

		new LoadComments().execute("");
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

		Boolean search = this.getIntent().getBooleanExtra("Search", false);
		LinearLayout commentLay = (LinearLayout) findViewById(R.id.CommentLayout);
		RelativeLayout setsConf = (RelativeLayout) findViewById(R.id.SetsConfigure);
		if(search)
			setsConf.setVisibility(View.GONE);
		else
			setsConf.setVisibility(View.VISIBLE);
		if(((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
			commentLay.setVisibility(View.GONE);
		else
		{
			commentLay.setVisibility(View.VISIBLE);
			/*
			 * Add To Set OnClickListner
			 */
			addToSet.setOnClickListener(new OnClickListener() {  

				String photosetID[];
				private String selectedID = null;

				/*
				 *  Add to Set Click Method 
				 */
				@Override
				public void onClick(View arg0) { 

					AlertDialog.Builder builder;
					final AlertDialog alertDialog;

					Context mContext = c;
					LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.setslist, null);

					ArrayList<String> sets_list = new ArrayList<String>();

					try {
						signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.getList";
						request = "http://api.flickr.com/services/rest/" +
						"?method=flickr.photosets.getList" +
						"&api_key=6bdb55025361207fd970368b17e2c025" +
						"&auth_token=" + token +
						"&api_sig=" + flickrExecute.md5(signature);
						response = new flickrExecute(request).execute_get();

						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
						Document dom = db.parse(in);      
						Element docEle = dom.getDocumentElement();
						NodeList nl = docEle.getElementsByTagName("photoset");
						photosetID = new String[nl.getLength()];
						int cnt = 0;
						while(cnt<nl.getLength()) {
							Element entry = (Element)nl.item(cnt);
							Element title = (Element)entry.getElementsByTagName("title").item(0);
							sets_list.add(title.getFirstChild().getNodeValue()); 
							photosetID[cnt] = entry.getAttribute("id");
							cnt++;
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_LONG).show();
					}

					Spinner setlist = (Spinner) layout.findViewById(R.id.spnrAlbums);
					ArrayAdapter<String> spnadap = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, sets_list);

					OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							selectedID = photosetID[arg0.getSelectedItemPosition()];
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// Do Nothing
						}

					};

					setlist.setOnItemSelectedListener(spinnerListener);
					setlist.setAdapter(spnadap);
					builder = new AlertDialog.Builder(mContext);

					/*
					 * Add To Set Positive Button Click
					 */
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 

						@Override
						public void onClick(DialogInterface dialog, int which) { 
							if(selectedID.equals(null))
							{
								Toast.makeText(getApplicationContext(), "Invalid Photoset", Toast.LENGTH_LONG).show();
							}
							else
							{
								signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.addPhotophoto_id" + photoid + "photoset_id" + selectedID;
								request = "http://api.flickr.com/services/rest/" +
								"?method=flickr.photosets.addPhoto" +
								"&api_key=6bdb55025361207fd970368b17e2c025" +
								"&photoset_id=" + selectedID +
								"&photo_id=" + photoid +
								"&auth_token=" + token +
								"&api_sig=" + flickrExecute.md5(signature);
								try {
									response = new flickrExecute(request).execute_post();
									Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_LONG).show();
								}
							}	
						}
					});

					/*
					 * Add to Set Cancel Button
					 */
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

					builder.setView(layout);
					alertDialog = builder.create();

					/*
					 * New Set Create OnClickListner
					 */
					Button NewSet = (Button) layout.findViewById(R.id.plus);
					NewSet.setOnClickListener(new OnClickListener() { 

						@Override
						public void onClick(View arg0) {
							final Dialog diagNewSet = new Dialog(c);
							diagNewSet.setContentView(R.layout.new_photoset);
							diagNewSet.setTitle("Create PhotoSet");
							Button btnOK = (Button) diagNewSet.findViewById(R.id.btnOK);
							Button btnCancel = (Button) diagNewSet.findViewById(R.id.btnCancel);

							btnOK.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									String title,desc = null;
									EditText edname = (EditText) diagNewSet.findViewById(R.id.Edname);
									EditText eddesc = (EditText) diagNewSet.findViewById(R.id.Eddesc);
									title = edname.getText().toString();
									desc = eddesc.getText().toString();
									title = title.trim();
									desc = desc.trim();
									if(title.equals(""))
									{
										request = "";
									}
									else if(desc.equals(""))
									{
										signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.createprimary_photo_id" + photoid + "title" + title;
										title = title.replaceAll(" ", "%20");
										request = "http://api.flickr.com/services/rest/" +
										"?method=flickr.photosets.create" +
										"&api_key=6bdb55025361207fd970368b17e2c025" +
										"&title=" + title +
										"&primary_photo_id=" + photoid +
										"&auth_token=" + token +
										"&api_sig=" + flickrExecute.md5(signature);
									}
									else
									{
										signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "description" + desc + "methodflickr.photosets.createprimary_photo_id" + photoid + "title" + title;
										title = title.replaceAll(" ", "%20");
										desc = desc.replaceAll(" ", "%20");
										request = "http://api.flickr.com/services/rest/" +
										"?method=flickr.photosets.create" +
										"&api_key=6bdb55025361207fd970368b17e2c025" +
										"&title=" + title +
										"&description=" + desc + 
										"&primary_photo_id=" + photoid +
										"&auth_token=" + token +
										"&api_sig=" + flickrExecute.md5(signature);
									}
									try {
										response = new flickrExecute(request).execute_post();
										Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
										diagNewSet.dismiss();
										alertDialog.dismiss();
									} catch (Exception e) {
										Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_LONG).show();
									}
								}
							});

							btnCancel.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									diagNewSet.dismiss();
								}
							});
							diagNewSet.show();
						}
					});
					alertDialog.show();
				}
			});

			/*
			 * Delete From Set OnClickListner
			 */
			deleteFromSet.setOnClickListener(new OnClickListener() {

				private String photosetID[];
				private String selectedID = "";

				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder;
					AlertDialog alertDialog;

					Context mContext = c;
					LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.setslist, null);
					Button plus = (Button) layout.findViewById(R.id.plus);
					plus.setVisibility(Button.INVISIBLE);

					ArrayList<String> sets_list = new ArrayList<String>();
					try{
						signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.getAllContextsphoto_id" + photoid;
						request = "http://api.flickr.com/services/rest/" +
						"?method=flickr.photos.getAllContexts" +
						"&api_key=6bdb55025361207fd970368b17e2c025" +
						"&photo_id=" + photoid +
						"&auth_token=" + token +
						"&api_sig=" + flickrExecute.md5(signature);
						response = new flickrExecute(request).execute_get();

						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
						Document dom = db.parse(in);      
						Element docEle = dom.getDocumentElement();
						NodeList nl = docEle.getElementsByTagName("set");
						photosetID = new String[nl.getLength()];
						int cnt = 0;
						while(cnt<nl.getLength()) {
							Element entry = (Element)nl.item(cnt);
							sets_list.add(entry.getAttribute("title")); 
							photosetID[cnt] = entry.getAttribute("id");
							cnt++;
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_LONG).show();
					}

					Spinner setlist = (Spinner) layout.findViewById(R.id.spnrAlbums);
					ArrayAdapter<String> spnadap = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, sets_list);

					OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							selectedID = photosetID[arg0.getSelectedItemPosition()];
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// Do Nothing
						}

					};

					setlist.setOnItemSelectedListener(spinnerListener);
					setlist.setAdapter(spnadap);
					builder = new AlertDialog.Builder(mContext);

					/*
					 * Positive Button Click
					 */

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(selectedID.equals(""))
							{
								Toast.makeText(getApplicationContext(), "Invalid Photoset", Toast.LENGTH_LONG).show();
							}
							else
							{
								signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.removePhotophoto_id" + photoid + "photoset_id" + selectedID;
								request = "http://api.flickr.com/services/rest/" +
								"?method=flickr.photosets.removePhoto" +
								"&api_key=6bdb55025361207fd970368b17e2c025" +
								"&photoset_id=" + selectedID +
								"&photo_id=" + photoid +
								"&auth_token=" + token +
								"&api_sig=" + flickrExecute.md5(signature);
								try {
									response = new flickrExecute(request).execute_post();
									Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_LONG).show();
								}
							}
						}     
					});

					/*
					 * Negative Button Click
					 */
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setView(layout);
					alertDialog = builder.create();
					alertDialog.show();
				}
			});

			/*
			 * Add a Comment
			 */
			addCmntBtn.setOnClickListener(new OnClickListener() {  

				@Override
				public void onClick(View v) {
					if(addCmntTxt.getText().toString().trim().equals(""))
					{
						Toast.makeText(getApplicationContext(), "Invalid Comment", Toast.LENGTH_LONG);
					}
					else
					{
						String cmnt = addCmntTxt.getText().toString();
						addCmntTxt.setText("");
						cmnt = cmnt.trim();
						signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "comment_text" + cmnt + "methodflickr.photos.comments.addCommentphoto_id" + photoid;
						cmnt = cmnt.replaceAll(" ", "%20");
						request = "http://api.flickr.com/services/rest/" +
						"?method=flickr.photos.comments.addComment" +
						"&api_key=6bdb55025361207fd970368b17e2c025" +
						"&photo_id=" + photoid +
						"&comment_text=" + cmnt +
						"&auth_token=" + token +
						"&api_sig=" + flickrExecute.md5(signature);
						try {
							response = new flickrExecute(request).execute_post();
							Toast.makeText(getApplicationContext(), "Comment Successfully Added", Toast.LENGTH_LONG).show();
							cmnt = cmnt.replaceAll("%20", " ");
							cmnt = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME) + " : " + cmnt;
							fotocmnt.add(cmnt);
							adapter.notifyDataSetChanged();
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
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
				Log.d("Pos", "" + pos);
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					int maxpos = flickrImageViewer.imageList.size();
					Log.d("Max", "" + maxpos);
					if(pos == (maxpos-1))
						{
						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);;
						}
					else
						pos = pos + 1;
					pic.invalidate();
					photoid = flickrImageViewer.imageList.get(pos).photoID;
					phototitle = flickrImageViewer.imageList.get(pos).photoTitle;
					photoURL = flickrImageViewer.imageList.get(pos).photoURL;
					setimage();
					new LoadComments().execute("");
				} 
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(pos==0)
						{
						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);;
						}
					else
						pos = pos - 1;
					pic.invalidate();
					photoid = flickrImageViewer.imageList.get(pos).photoID;
					phototitle = flickrImageViewer.imageList.get(pos).photoTitle;
					photoURL = flickrImageViewer.imageList.get(pos).photoURL;
					setimage();
					new LoadComments().execute("");
				}
				Log.d("Pos", "" + pos);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	/*
	 * Parse Comment Feeds 
	 */
	private ArrayList<String> parseComment(String response) throws SAXException, IOException, ParserConfigurationException {
		ArrayList<String> cmntlist = new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
		Document dom = db.parse(in);      
		Element docEle = dom.getDocumentElement();
		NodeList nl = docEle.getElementsByTagName("comment");
		int cnt = 0;
		while(cnt<nl.getLength()) {
			Element entry = (Element)nl.item(cnt);
			String strcomment,strauthor;
			strauthor = entry.getAttribute("authorname");
			strcomment = entry.getFirstChild().getNodeValue();
			strauthor = strauthor + " : " + strcomment;
			cmntlist.add(strauthor);
			cnt++;
		}
		return cmntlist;
	}

	private class LoadComments extends AsyncTask<String, Void, Void> {
		int cmntpos;

		@Override
		protected Void doInBackground(String... arg0) {
			try {
				if(((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
				{
					signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025methodflickr.photos.comments.getListphoto_id" + photoid;
					request = "http://api.flickr.com/services/rest/" +
					"?method=flickr.photos.comments.getList" +
					"&api_key=6bdb55025361207fd970368b17e2c025" +
					"&photo_id=" + photoid +
					"&api_sig=" + flickrExecute.md5(signature);
				}
				else
				{
					signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.comments.getListphoto_id" + photoid;
					request = "http://api.flickr.com/services/rest/" +
					"?method=flickr.photos.comments.getList" +
					"&api_key=6bdb55025361207fd970368b17e2c025" +
					"&photo_id=" + photoid +
					"&auth_token=" + token +
					"&api_sig=" + flickrExecute.md5(signature);
				}
				response = new flickrExecute(request).execute_get();		
				fotocmnt = parseComment(response);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			cmntpos = pos;
			adapter = new ArrayAdapter<String>(c, R.layout.comment, R.id.screen);
			adapter.add("Loading Comments .......");
			myCmnt.setAdapter(adapter);
		}

		@Override
		protected void onPostExecute(Void result) {
			if(cmntpos==pos)
			{
				if(fotocmnt==null)
				{
					adapter = null;
					myCmnt.setAdapter(null);
				}
				else
				{
					adapter = new ArrayAdapter<String>(c,R.layout.comment,R.id.screen,fotocmnt);
					myCmnt.setAdapter(adapter);
				}
			}
		}
	}

	private void setimage() {
		title.setText(phototitle);
		pic.setImageDrawable(photoURL, strngs.SITE_FLICKR, c, photoid);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
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