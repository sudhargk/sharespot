package ss.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.ui.Browse;
import ss.ui.FlickrAuth;
import ss.ui.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class flickrAlbumViewer extends Activity {

	List <flickrAlbumContent> albumList = new ArrayList<flickrAlbumContent>();
	GridView Albums;
	Context c = this;
	Activity a = this;
	private String token;
	String value;
	Boolean resumeFlag;
	Boolean isConfigured;
	flickrAlbumAdap adapter;
	String longClickList[] = {"Delete"};

	@Override
	public void onResume()
	{
		super.onResume();
		if(resumeFlag==true&&isConfigured==false)
			adapter.resumeAsyncTask();
	}

	@Override
	public void onPause()
	{
		if(isConfigured==true && adapter != null)
			adapter.cancelAsyncTask();
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resumeFlag=false;
		adapter = null;
		setContentView(R.layout.flickr_main);
		Albums = (GridView) findViewById(R.id.MyAlbum);
		ImageButton srchbtn = (ImageButton) findViewById(R.id.btnSearchYT);
		srchbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent srchAct = new Intent(c,flickrSearch.class);
				srchAct.putExtra("start-index", 1);
				srchAct.putExtra("search-key", "");
				View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", srchAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				FlickrAuth.group.replace(view);
			}
		});
		if(((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
		{
			Albums.setVisibility(View.GONE);
			TextView AcntNotConf = (TextView) findViewById(R.id.AcntNotConfigured);
			AcntNotConf.setVisibility(View.VISIBLE);
			isConfigured=false;
		}
		else
		{
			token = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_TOKEN);
			c = Browse.TabContext;
			isConfigured=true;

			TextView myAlbums = (TextView) findViewById(R.id.MyTxt);
			myAlbums.setText("My Albums");
			new getAlbums().execute();

			Albums.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					if(albumList.get(position).title.equalsIgnoreCase("Unsorted"))
					{
						Intent img_show = new Intent(c, flickrImageViewer.class);
						img_show.putExtra("feed", value);
						img_show.putExtra("albumname", "Unsorted");
						img_show.putExtra("albumID", "");
						View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", img_show
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
								.getDecorView();  
						FlickrAuth.group.replace(view);
					}
					else
					{
						Intent img_show = new Intent(c, flickrImageViewer.class);
						img_show.putExtra("albumID", albumList.get(position).AlbumID);
						img_show.putExtra("albumname", albumList.get(position).title);
						img_show.putExtra("feed", "");
						View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", img_show
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
								.getDecorView();  
						FlickrAuth.group.replace(view);
					}
				}
			});

			Albums.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int longClickPos, long arg3) {
					if(albumList.get(longClickPos).AlbumPhotoURL.equalsIgnoreCase("URL"))
					{
						return false;
					}
					else
					{
						final Dialog longClick = new Dialog(c);
						longClick.setContentView(R.layout.longclick_list);
						longClick.setTitle("Options");
						ListView albumOptions = (ListView) longClick.findViewById(R.id.LongClickList);
						albumOptions.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, longClickList));
						albumOptions.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int Clickpos, long arg3) {
								switch(Clickpos)
								{
								case 0:
									final AlertDialog alertDialog = new AlertDialog.Builder(c).create();
									alertDialog.setTitle("Delete Album " + albumList.get(longClickPos).title);
									alertDialog.setMessage("Are you sure ?");
									alertDialog.setButton("OK",new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											try {
												String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.deletephotoset_id" + albumList.get(longClickPos).AlbumID;
												String request = "http://api.flickr.com/services/rest/" +
												"?method=flickr.photosets.delete" +
												"&api_key=6bdb55025361207fd970368b17e2c025" +
												"&photoset_id=" + albumList.get(longClickPos).AlbumID +
												"&auth_token=" + token +
												"&api_sig=" + flickrExecute.md5(signature);
												String response = new flickrExecute(request).execute_post();
												if(!response.equalsIgnoreCase("error"))
												{
													Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
													albumList.remove(longClickPos);
													adapter.deletedrawable(longClickPos);
													adapter.notifyDataSetInvalidated();
													longClick.dismiss();
												}
												else
													Toast.makeText(getApplicationContext(), "Error Deleting Photoset", Toast.LENGTH_LONG).show();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									});

									alertDialog.setButton2("Cancel",new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											alertDialog.dismiss();
										}
									});
									alertDialog.setIcon(R.drawable.icon);
									alertDialog.show();
									break;
								}
							}
						});
						longClick.show();
						return false;
					}
				}
			});
		}
	}

	private class getAlbums extends AsyncTask<Void,String, Void> {
		private ProgressDialog dialog;
		Boolean errFlag;

		protected void onPreExecute() {
			errFlag=false;
			dialog = new ProgressDialog(c); 
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
		}

		protected Void doInBackground(Void... params) {
			publishProgress("Retrieving Data");
			try {
				String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.getList";
				String request = "http://api.flickr.com/services/rest/" +
				"?method=flickr.photosets.getList" +
				"&api_key=6bdb55025361207fd970368b17e2c025" +
				"&auth_token=" + token +
				"&api_sig=" + flickrExecute.md5(signature);
				String response = new flickrExecute(request).execute_get();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
				Document dom = db.parse(in);      
				Element docEle = dom.getDocumentElement();
				NodeList nl = docEle.getElementsByTagName("photoset");
				int cnt = 0;
				while(cnt<nl.getLength()) {
					publishProgress("Loading Albums ("+(cnt+1)+"/"+nl.getLength()+")");
					Element entry = (Element)nl.item(cnt);
					Element title = (Element)entry.getElementsByTagName("title").item(0);

					String strTitle,strNumPhotos,strThmb,strAlbumID, strPriID;

					strTitle = title.getFirstChild().getNodeValue();        	                  
					strPriID = entry.getAttribute("primary");
					strThmb = parseForURL(strPriID);
					strAlbumID = entry.getAttribute("id");
					strNumPhotos = entry.getAttribute("photos");

					flickrAlbumContent tempContent = new flickrAlbumContent(strTitle, strAlbumID, strNumPhotos, strThmb, strPriID);
					albumList.add(tempContent);
					cnt++;
				}
				publishProgress("Loading Albums");
				String signature1 = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "extrasurl_t,url_mmethodflickr.photos.getNotInSet";
				String request1 = "http://api.flickr.com/services/rest/" +
				"?method=flickr.photos.getNotInSet" +
				"&api_key=6bdb55025361207fd970368b17e2c025" +
				"&extras=url_t,url_m" +
				"&auth_token=" + token +
				"&api_sig=" + flickrExecute.md5(signature1);
				String response1 = new flickrExecute(request1).execute_get();
				value = response1;

				String strTitle,strNumPhotos,strThmb,strAlbumID;
				strNumPhotos = response1.substring(response1.indexOf("total")+7);
				strNumPhotos = strNumPhotos.substring(0, strNumPhotos.indexOf("\""));
				if(!strNumPhotos.equalsIgnoreCase("0"))
				{
					strTitle = "Unsorted";      	                  
					strThmb = "URL";
					strAlbumID = "0";
					flickrAlbumContent tempContent = new flickrAlbumContent(strTitle, strAlbumID, strNumPhotos, strThmb, "0");
					albumList.add(tempContent);
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress(e.getMessage());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress(e.getMessage());
			} catch (SAXException e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress(e.getMessage());
			}
			return null;
		}

		protected void onProgressUpdate(String... args) {
			if(errFlag==false)
				dialog.setMessage(args[0]);
			else
			{	
				this.cancel(true);   
				Toast.makeText(c, args[0], Toast.LENGTH_LONG).show();
				errFlag=false;
				a.finish();
			}
		}

		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				adapter = new flickrAlbumAdap(c, R.layout.albumcontent, albumList,albumList.size(),Albums);
				resumeFlag=true;
				Albums.setAdapter(adapter);
			}
		}
	}

	private String parseForURL(String strThmb) throws Exception {
		token = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_TOKEN);
		String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.getSizesphoto_id" + strThmb;
		String request = "http://api.flickr.com/services/rest/" +
		"?method=flickr.photos.getSizes" +
		"&photo_id=" + strThmb +
		"&api_key=6bdb55025361207fd970368b17e2c025" +
		"&auth_token=" + token +
		"&api_sig=" + flickrExecute.md5(signature);				
		String response = new flickrExecute(request).execute_get();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
		Document dom = db.parse(in);      
		Element docEle = dom.getDocumentElement();
		NodeList nl = docEle.getElementsByTagName("size");
		Element entry = (Element)nl.item(1);
		String source = entry.getAttribute("source");
		return source;
	}
}