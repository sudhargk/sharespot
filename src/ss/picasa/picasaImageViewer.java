package ss.picasa;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ss.cache.CachePolicy.Cache_Options;
import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.ui.Browse;
import ss.ui.PicasaAuth;
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

public class picasaImageViewer extends Activity {

	private String albumID;
	List <picasaImageContent> imageList = new ArrayList<picasaImageContent>();
	GridView Images;
	Context c = this;
	Activity a = this;
	picasaImageAdap picAdap;
	private String albumName;
	Boolean resumeFlag;
	String longClickList[] = {"Delete","Details"};

	@Override
	public void onResume()
	{
		super.onResume();
		if(resumeFlag==true)
			picAdap.resumeAsyncTask();
	}
	
	@Override
	public void onBackPressed() {
		PicasaAuth.group.back();
		super.onBackPressed();
	}

	@Override
	public void onPause()
	{
		if(picAdap != null)
			picAdap.cancelAsyncTask();
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picasa_main);
		picAdap = null;
		resumeFlag=false;
		ImageButton srchbtn = (ImageButton) findViewById(R.id.btnSearchYT);
		srchbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent srchAct = new Intent(c,picasaSearch.class);
				srchAct.putExtra("start-index", 1);
				srchAct.putExtra("search-key", "");
				View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct3", srchAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				PicasaAuth.group.replace(view);
			}
		});
		albumID = this.getIntent().getStringExtra("albumid");
		albumName = this.getIntent().getStringExtra("title");
		TextView myAlbums = (TextView) findViewById(R.id.MyTxt);
		myAlbums.setText("Images - Album : " + albumName);
		Images = (GridView) findViewById(R.id.MyAlbum);
		c=Browse.TabContext;
		new getPhotos().execute();

		Images.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,int pos, long arg3) {
				Intent myI = new Intent(c, picasaImage.class);
				myI.putExtra("pos",pos);
				myI.putExtra("albumID", albumID);
				startActivity(myI);
			}
		});

		Images.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int longClickPos, long arg3) {
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
							alertDialog.setTitle("Delete Image " + imageList.get(longClickPos).photoTitle);
							alertDialog.setMessage("Are you sure ?");
							alertDialog.setButton("OK",new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									try {
										String url =(imageList.get(longClickPos).delURL);
										HttpClient httpcl = new DefaultHttpClient();
										HttpDelete httpdel = new HttpDelete(url);
										httpdel.addHeader("Authorization",  "GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN));
										httpdel.addHeader("GData-Version","2");
										httpdel.addHeader("If-Match","*");
										HttpResponse httpres = httpcl.execute(httpdel);

										int rescode = httpres.getStatusLine().getStatusCode();
										if(rescode == 200)
										{
											Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_LONG).show();
											imageList.remove(longClickPos);
											picAdap.deletedrawable(longClickPos);
											picAdap.notifyDataSetInvalidated();
											longClick.dismiss();
										}
										else
											Toast.makeText(getApplicationContext(), "Error Deleting Image", Toast.LENGTH_LONG).show();

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
						case 1:
							longClick.setTitle("Details");
							ListView details = (ListView) longClick.findViewById(R.id.LongClickList);
							ArrayList<String> det;
							det = loaddetails(longClickPos);
							details.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, det));
							details.setOnItemClickListener(null);
							break;
						}
					}

					private ArrayList<String> loaddetails(int pos) {
						String str;
						ArrayList<String> albumdet = new ArrayList<String>();
						str = "Title : " + imageList.get(pos).photoTitle;
						albumdet.add(str);
						str = "Size : " + imageList.get(pos).size;
						albumdet.add(str);
						str = "Height : " + imageList.get(pos).height;
						albumdet.add(str);
						str = "Width : " + imageList.get(pos).width;
						albumdet.add(str);
						str = "Summary : " + imageList.get(pos).summary;
						albumdet.add(str);
						return albumdet;
					}
				});
				longClick.show();
				return false;
			}
		});
	}

	class getPhotos extends AsyncTask<Void,String, String> {
		private ProgressDialog dialog;
		Boolean errFlag;
		protected void onPreExecute() {
			errFlag=false;
			dialog = new ProgressDialog(c); 
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			publishProgress("Retrieving Data");
			String imageURL = "http://picasaweb.google.com/data/feed/api/user/default/albumid/" + albumID;
			StringBuffer sb = new StringBuffer("");
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(imageURL);
				request.addHeader("Authorization","GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN));
				request.addHeader("GData-Version","2");
				HttpResponse res = client.execute(request);
				BufferedReader in = new BufferedReader(new 
						InputStreamReader(res.getEntity().getContent()));

				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
			}catch (UnknownHostException ea) {
				errFlag=true;
				publishProgress("No Internet Connection");
			} catch (ClientProtocolException e) {
				errFlag=true;
				publishProgress("Protocol Error");
			} catch (IOException e) {
				e.printStackTrace();	
			}
			return sb.toString();
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

		protected void onPostExecute(String feed) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				setAlbumAdapter(feed);
				resumeFlag=true;
			}
		}
		public void setAlbumAdapter(String feed){
			imageList=parse(feed);
			picAdap = new picasaImageAdap(c, R.layout.picasaimage, imageList, imageList.size(), Images, Cache_Options.ALLOW_CACHE);
			Images.setAdapter(picAdap);
		}
		private List<picasaImageContent> parse(String myFeed) {	
			String Feed;
			List <picasaImageContent> content = new ArrayList<picasaImageContent>();
			try {
				Feed = myFeed;
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = new ByteArrayInputStream(Feed.getBytes("UTF-8"));
				Document dom = db.parse(in);      
				Element docEle = dom.getDocumentElement();
				NodeList nl = docEle.getElementsByTagName("entry");
				int cnt = 0;
				while(cnt<nl.getLength()) {
					Element entry = (Element)nl.item(cnt);
					Element mgrp = (Element)entry.getElementsByTagName("media:group").item(0);
					Element mthmb = (Element)mgrp.getElementsByTagName("media:thumbnail").item(0);
					Element photoid =(Element)entry.getElementsByTagName("gphoto:id").item(0);
					Element phototitle = (Element) entry.getElementsByTagName("title").item(0);
					Element url = (Element) mgrp.getElementsByTagName("media:content").item(0);
					Element summary = (Element)entry.getElementsByTagName("summary").item(0);
					Element size = (Element)entry.getElementsByTagName("gphoto:size").item(0);
					Element height = (Element)entry.getElementsByTagName("gphoto:height").item(0);
					Element width = (Element)entry.getElementsByTagName("gphoto:width").item(0);
					Element delURL = (Element) entry.getElementsByTagName("link").item(4);

					String strUserID,strAlbumID,strThmb,strURL,strphotoid,strphototitle,strSummary,strSize,strHeight,strWidth,delhref;
					strUserID = "default";
					strAlbumID = albumID;
					strThmb = mthmb.getAttribute("url").toString();
					strURL = url.getAttribute("url").toString();
					strphotoid = photoid.getFirstChild().getNodeValue();
					strphototitle =phototitle.getFirstChild().getNodeValue();
					if(summary.getFirstChild()!=null)
						strSummary=summary.getFirstChild().getNodeValue();
					else
						strSummary="";
					strSize=size.getFirstChild().getNodeValue();
					strHeight=height.getFirstChild().getNodeValue();
					strWidth=width.getFirstChild().getNodeValue();
					delhref = delURL.getAttribute("href");

					picasaImageContent p = new picasaImageContent(strUserID,strAlbumID,strThmb, strphotoid, strphototitle, strURL,strSize,strHeight,strWidth,strSummary,delhref);
					content.add(p);
					cnt++;
				}
				((GlobalVariable) getApplication()).setImageList(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return content;
		}
	}
}
