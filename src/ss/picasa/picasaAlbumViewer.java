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

public class picasaAlbumViewer extends Activity {

	List<picasaAlbumContent> albumList = new ArrayList<picasaAlbumContent>();
	GridView Albums;
	picasaAlbumAdap picAdap;
	Context c = this;
	Activity a = this;
	Boolean resumeFlag;
	Boolean isConfigured;
	String longClickList[] = { "Delete", "Details" };
	String AUTH_TOKEN;

	@Override
	public void onResume(){
		super.onResume();
		if(resumeFlag==true&&isConfigured==true)
			picAdap.resumeAsyncTask();
	}

	@Override
	public void onBackPressed() {
		PicasaAuth.group.back();
		super.onBackPressed();
	}

	@Override
	public void onPause(){
		if(isConfigured == true && picAdap != null)
			picAdap.cancelAsyncTask();
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resumeFlag=false;
		picAdap = null;
		setContentView(R.layout.picasa_main);
		Albums = (GridView) findViewById(R.id.MyAlbum);
		ImageButton srchbtn = (ImageButton) findViewById(R.id.btnSearchYT);
		srchbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent srchAct = new Intent(c,picasaSearch.class);
				srchAct.putExtra("start-index", 1);
				srchAct.putExtra("search-key", "");
				View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct1", srchAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();
				PicasaAuth.group.replace(view);
			}
		});
		if(((GlobalVariable)getApplication()).getValue(keys.PICASA_USERNAME).equals("null"))
		{
			Albums.setVisibility(View.GONE);
			TextView AcntNotConf = (TextView) findViewById(R.id.AcntNotConfigured);
			AcntNotConf.setVisibility(View.VISIBLE);
			isConfigured=false;
		}
		else
		{
			c = Browse.TabContext;
			isConfigured=true;

			TextView myAlbums = (TextView) findViewById(R.id.MyTxt);
			myAlbums.setText("My Albums");

			AUTH_TOKEN =((GlobalVariable) getApplication()).getValue(keys.PIC_TOKEN); 
			new getAlbums().execute();
			Albums.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
					Intent SImg = new Intent(c,picasaImageViewer.class);
					SImg.putExtra("albumid", albumList.get(position).AlbumID);
					SImg.putExtra("title", albumList.get(position).title);
					picAdap.cancelAsyncTask();
					View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct2", SImg
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					PicasaAuth.group.replace(view);
				}		
			});

			Albums.setOnItemLongClickListener(new OnItemLongClickListener() {

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
								alertDialog.setTitle("Delete Album " + albumList.get(longClickPos).title);
								alertDialog.setMessage("Are you sure ?");
								alertDialog.setButton("OK",new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										try {
											String url =(albumList.get(longClickPos).delURL);
											HttpClient httpcl = new DefaultHttpClient();
											HttpDelete httpdel = new HttpDelete(url);
											httpdel.addHeader("Authorization",  "GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN));
											httpdel.addHeader("GData-Version","2");
											httpdel.addHeader("If-Match","*");
											HttpResponse httpres = httpcl.execute(httpdel);

											int rescode = httpres.getStatusLine().getStatusCode();
											if(rescode == 200)
											{
												Toast.makeText(getApplicationContext(), "Album Deleted", Toast.LENGTH_LONG).show();
												albumList.remove(longClickPos);
												picAdap.deletedrawable(longClickPos);
												picAdap.notifyDataSetInvalidated();
												longClick.dismiss();
											}
											else
												Toast.makeText(getApplicationContext(), "Error Deleting Album", Toast.LENGTH_LONG).show();
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
							str = "Title : " + albumList.get(pos).title;
							albumdet.add(str);
							str = "No. of Photos : " + albumList.get(pos).numPhotos;
							albumdet.add(str);
							str = "Published On : " + albumList.get(pos).pblshd;
							albumdet.add(str);
							str = "Updated On : " + albumList.get(pos).updtd;
							albumdet.add(str);
							str = "Summary : " + albumList.get(pos).summary;
							albumdet.add(str);
							str = "Rights : " + albumList.get(pos).rights;
							albumdet.add(str);
							return albumdet;
						}
					});
					longClick.show();
					return false;
				}
			});
		}
	}

	class getAlbums extends AsyncTask<Void,String,String>
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
		@Override
		protected String doInBackground(Void... params) {
			publishProgress("Retrieving Data");
			String albumUrl = "http://picasaweb.google.com/data/feed/api/user/"+((GlobalVariable) getApplication()).getValue(keys.PICASA_USERNAME);
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(albumUrl);
				request.addHeader("Authorization","GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN));
				request.addHeader("GData-Version","2");
				HttpResponse res = client.execute(request);
				BufferedReader in = new BufferedReader(new 
						InputStreamReader(res.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				return sb.toString();
			}catch (UnknownHostException ea) {
				errFlag=true;
				publishProgress("No Internet Connection");
			} catch (ClientProtocolException e) {
				errFlag=true;
				publishProgress("Protocol Error");
			} catch (IOException e) {
				errFlag=true;
				publishProgress("I/O Error");	
			} catch(Exception e){
				errFlag=true;
				publishProgress("Error");	
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

		protected void onPostExecute(String feed) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				try {
					albumList = parse(feed);
					picAdap = new picasaAlbumAdap(c, R.layout.albumcontent, albumList,albumList.size(), Albums);
					Albums.setAdapter(picAdap);
					resumeFlag=true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private List<picasaAlbumContent> parse(String myFeed) throws Exception {

			String Feed;
			List <picasaAlbumContent> content= new ArrayList<picasaAlbumContent>();

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
					Element title = (Element)entry.getElementsByTagName("title").item(0);
					Element mgrp = (Element)entry.getElementsByTagName("media:group").item(0);
					Element mthmb = (Element)mgrp.getElementsByTagName("media:thumbnail").item(0);
					Element albumID =(Element)entry.getElementsByTagName("gphoto:id").item(0);
					Element delURL = (Element) entry.getElementsByTagName("link").item(3);
					Element numPhotos =(Element)entry.getElementsByTagName("gphoto:numphotos").item(0);
					Element pblshd = (Element)entry.getElementsByTagName("published").item(0);
					Element updtd = (Element)entry.getElementsByTagName("updated").item(0);
					Element summary = (Element)entry.getElementsByTagName("summary").item(0);
					Element rights = (Element)entry.getElementsByTagName("rights").item(0);

					String strTitle,strNumPhotos,strThmb,strAlbumID,delhref;
					String strPblshd,strUpdtd,strSummary,strRights;

					strTitle = title.getFirstChild().getNodeValue();        	                  
					strThmb = mthmb.getAttribute("url").toString();
					strAlbumID= albumID.getFirstChild().getNodeValue();
					strNumPhotos=numPhotos.getFirstChild().getNodeValue();

					delhref = delURL.getAttribute("href");

					strPblshd=pblshd.getFirstChild().getNodeValue();
					strUpdtd=updtd.getFirstChild().getNodeValue();

					if(summary.getFirstChild()!=null)
						strSummary=summary.getFirstChild().getNodeValue();
					else
						strSummary="";
					strRights=rights.getFirstChild().getNodeValue();
					strPblshd = strPblshd.substring(0,10);
					strUpdtd = strUpdtd.substring(0,10);
					picasaAlbumContent tempContent = new picasaAlbumContent(strTitle,strAlbumID ,strNumPhotos, strThmb, delhref,
							strPblshd,strUpdtd,strSummary,strRights);
					content.add(tempContent);
					cnt++;
				}
			} catch (Exception e) {
				throw e;
			}
			return content;
		}
	}
}