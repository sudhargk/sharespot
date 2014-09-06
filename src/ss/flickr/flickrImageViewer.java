package ss.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ss.cache.CachePolicy.Cache_Options;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class flickrImageViewer extends Activity {

	static List<flickrImageContent> imageList = new ArrayList<flickrImageContent>();
	GridView Images;
	private String AID;
	private String UnsortedFeed;
	private String albumName;
	Context c = this;
	Activity a = this;
	Boolean resumeFlag;
	private boolean Unsorted;
	flickrImageAdap adapter;
	String token;
	ArrayList<String> longClickList;
	String photosetID[];
	String selectedID = null;

	@Override
	public void onResume() {
		super.onResume();
		if(resumeFlag==true)
			adapter.resumeAsyncTask();
	}

	@Override
	public void onPause() {
		if(adapter != null)
			adapter.cancelAsyncTask();
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flickr_main);
		adapter = null;
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

		resumeFlag=false;
		token = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_TOKEN);

		AID = this.getIntent().getStringExtra("albumID");
		UnsortedFeed = this.getIntent().getStringExtra("feed");
		albumName = this.getIntent().getStringExtra("albumname");

		TextView myAlbums = (TextView) findViewById(R.id.MyTxt);
		myAlbums.setText("Images - Album : " + albumName);
		c = Browse.TabContext;

		Images = (GridView) findViewById(R.id.MyAlbum);
		if(UnsortedFeed.equals(""))
			Unsorted=false;
		else
			Unsorted=true;
		new getPhotos().execute();
		longClickList = new ArrayList<String>();

		Images.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent myI = new Intent(c, flickrImage.class);
				myI.putExtra("pos", pos);
				myI.putExtra("Search", false);
				startActivity(myI);
			}
		});

		Images.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int longClickPos, long arg3) {
				if(AID.equals(""))
				{
					longClickList.clear();
					longClickList.add("Delete");
					longClickList.add("Details");
					longClickList.add("Add To Set");
				}
				else
				{
					longClickList.clear();
					longClickList.add("Delete");
					longClickList.add("Details");
					longClickList.add("Set Photo As Primary");
					longClickList.add("Add To Set");
					longClickList.add("Delete From Set");
				}
				final Dialog longClick = new Dialog(c);
				longClick.setContentView(R.layout.longclick_list);
				longClick.setTitle("Options");
				ListView albumOptions = (ListView) longClick.findViewById(R.id.LongClickList);
				albumOptions.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, longClickList));
				albumOptions.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int Clickpos, long arg3) {
						if(longClickList.get(Clickpos).equals("Delete"))
						{
							final AlertDialog alertDialog = new AlertDialog.Builder(c).create();
							alertDialog.setTitle("Delete Image " + imageList.get(longClickPos).photoTitle);
							alertDialog.setMessage("Are you sure ?");
							alertDialog.setButton("OK",new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									try {
										String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.deletephoto_id" + imageList.get(longClickPos).photoID;
										String request = "http://api.flickr.com/services/rest/" +
										"?method=flickr.photos.delete" +
										"&api_key=6bdb55025361207fd970368b17e2c025" +
										"&photo_id=" + imageList.get(longClickPos).photoID +
										"&auth_token=" + token +
										"&api_sig=" + flickrExecute.md5(signature);
										String response = new flickrExecute(request).execute_post();
										if(!response.equalsIgnoreCase("error"))
										{
											Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_LONG).show();
											imageList.remove(longClickPos);
											adapter.deletedrawable(longClickPos);
											adapter.notifyDataSetInvalidated();
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
						}
						else if(longClickList.get(Clickpos).equals("Details"))
						{
							longClick.setTitle("Details");
							ListView details = (ListView) longClick.findViewById(R.id.LongClickList);
							try {
								ArrayList<String> det = new LoadInfo().execute(longClickPos).get();
								details.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, det));
								details.setOnItemClickListener(null);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
						else if(longClickList.get(Clickpos).equals("Set Photo As Primary"))
						{
							try {
								String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.setPrimaryPhotophoto_id" + imageList.get(longClickPos).photoID + "photoset_id" + AID;
								String request = "http://api.flickr.com/services/rest/" +
								"?method=flickr.photosets.setPrimaryPhoto" +
								"&api_key=6bdb55025361207fd970368b17e2c025" +
								"&photoset_id=" + AID +
								"&photo_id=" + imageList.get(longClickPos).photoID +
								"&auth_token=" + token +
								"&api_sig=" + flickrExecute.md5(signature);
								@SuppressWarnings("unused")
								String response = new flickrExecute(request).execute_get();
								Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();	
							}


						}
						else if(longClickList.get(Clickpos).equals("Add To Set"))
						{
							AlertDialog.Builder builder;
							final AlertDialog alertDialog;

							Context mContext = c;
							LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
							View layout = inflater.inflate(R.layout.setslist, null);

							ArrayList<String> sets_list = new ArrayList<String>();

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
										String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.addPhotophoto_id" + imageList.get(longClickPos).photoID + "photoset_id" + selectedID;
										String request = "http://api.flickr.com/services/rest/" +
										"?method=flickr.photosets.addPhoto" +
										"&api_key=6bdb55025361207fd970368b17e2c025" +
										"&photoset_id=" + selectedID +
										"&photo_id=" + imageList.get(longClickPos).photoID +
										"&auth_token=" + token +
										"&api_sig=" + flickrExecute.md5(signature);
										@SuppressWarnings("unused")
										String response;
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
											String request = "";
											if(title.equals(""))
											{
												request = "";	
											}
											else if(desc.equals(""))
											{
												String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.createprimary_photo_id" + imageList.get(longClickPos).photoID + "title" + title;
												title = title.replaceAll(" ", "%20");
												request = "http://api.flickr.com/services/rest/" +
												"?method=flickr.photosets.create" +
												"&api_key=6bdb55025361207fd970368b17e2c025" +
												"&title=" + title +
												"&primary_photo_id=" + imageList.get(longClickPos).photoID +
												"&auth_token=" + token +
												"&api_sig=" + flickrExecute.md5(signature);
											}
											else
											{
												String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "description" + desc + "methodflickr.photosets.createprimary_photo_id" + imageList.get(longClickPos).photoID + "title" + title;
												title = title.replaceAll(" ", "%20");
												desc = desc.replaceAll(" ", "%20");
												request = "http://api.flickr.com/services/rest/" +
												"?method=flickr.photosets.create" +
												"&api_key=6bdb55025361207fd970368b17e2c025" +
												"&title=" + title +
												"&description=" + desc + 
												"&primary_photo_id=" + imageList.get(longClickPos).photoID +
												"&auth_token=" + token +
												"&api_sig=" + flickrExecute.md5(signature);
											}
											@SuppressWarnings("unused")
											String response;
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
						else if(longClickList.get(Clickpos).equals("Delete From Set"))
						{
							AlertDialog.Builder builder;
							AlertDialog alertDialog;

							Context mContext = c;
							LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
							View layout = inflater.inflate(R.layout.setslist, null);
							Button plus = (Button) layout.findViewById(R.id.plus);
							plus.setVisibility(Button.INVISIBLE);

							ArrayList<String> sets_list = new ArrayList<String>();
							try {
								String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.getAllContextsphoto_id" + imageList.get(longClickPos).photoID;
								String request = "http://api.flickr.com/services/rest/" +
								"?method=flickr.photos.getAllContexts" +
								"&api_key=6bdb55025361207fd970368b17e2c025" +
								"&photo_id=" + imageList.get(longClickPos).photoID +
								"&auth_token=" + token +
								"&api_sig=" + flickrExecute.md5(signature);
								String response = new flickrExecute(request).execute_get();

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
										String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photosets.removePhotophoto_id" + imageList.get(longClickPos).photoID + "photoset_id" + selectedID;
										String request = "http://api.flickr.com/services/rest/" +
										"?method=flickr.photosets.removePhoto" +
										"&api_key=6bdb55025361207fd970368b17e2c025" +
										"&photoset_id=" + selectedID +
										"&photo_id=" + imageList.get(longClickPos).photoID +
										"&auth_token=" + token +
										"&api_sig=" + flickrExecute.md5(signature);
										@SuppressWarnings("unused")
										String response;
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

					}
				});
				longClick.show();
				return false;
			}
		});
	}

	class getPhotos extends AsyncTask<Void,String, Void> {

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
		protected Void doInBackground(Void... params) {
			publishProgress("Retrieving Data");
			String response;
			try {
				if(Unsorted==false)
				{
					String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "extrasurl_t,url_mmethodflickr.photosets.getPhotosphotoset_id" + AID;
					String request = "http://api.flickr.com/services/rest/" +
					"?method=flickr.photosets.getPhotos" +
					"&api_key=6bdb55025361207fd970368b17e2c025" +
					"&photoset_id=" + AID +
					"&extras=url_t,url_m" +
					"&auth_token=" + token +
					"&api_sig=" + flickrExecute.md5(signature);
					response = new flickrExecute(request).execute_get();
				}
				else
					response=UnsortedFeed;
				imageList = parse(response);
			} catch (Exception e) {
				e.printStackTrace();
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
				adapter = new flickrImageAdap(c, R.layout.picasaimage, imageList, imageList.size(), Images, Cache_Options.ALLOW_CACHE);
				Images.setAdapter(adapter);
				resumeFlag=true;
			}
		}

		private List<flickrImageContent> parse(String myFeed) {	

			String Feed;
			List <flickrImageContent> content = new ArrayList<flickrImageContent>();
			try {
				Feed = myFeed;
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = new ByteArrayInputStream(Feed.getBytes("UTF-8"));
				Document dom = db.parse(in);      
				Element docEle = dom.getDocumentElement();
				NodeList nl = docEle.getElementsByTagName("photo");
				int cnt = 0;
				while(cnt<nl.getLength()) {
					publishProgress("Loading Photos ("+(cnt+1)+"/"+nl.getLength()+")");
					Element photo = (Element)nl.item(cnt);

					String strThmb,strphotoid,strURL,strphototitle;
					strThmb = photo.getAttribute("url_t").toString();
					strURL = photo.getAttribute("url_m").toString();
					strphotoid = photo.getAttribute("id").toString();
					strphototitle = photo.getAttribute("title").toString();

					flickrImageContent p = new flickrImageContent(strThmb, strphotoid, strphototitle, strURL);
					content.add(p);
					cnt++;
				}
			} catch (Exception e) {
				errFlag=true;
				publishProgress("Parsing Error");
			}
			return content;
		}
	}

	private class LoadInfo extends AsyncTask<Integer, Void, ArrayList<String>> {
		private ProgressDialog dialog;
		ArrayList<String> info;

		@Override
		protected ArrayList<String> doInBackground(Integer... params) {
			int pos = params[0];
			try {
				String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "methodflickr.photos.getInfophoto_id" + imageList.get(pos).photoID;
				String request = "http://api.flickr.com/services/rest/" +
				"?method=flickr.photos.getInfo" +
				"&api_key=6bdb55025361207fd970368b17e2c025" +
				"&photo_id=" + imageList.get(pos).photoID + 
				"&auth_token=" + token +
				"&api_sig=" + flickrExecute.md5(signature);
				String response;
				response = new flickrExecute(request).execute_get();
				info = parseInfo(response);
				return info;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private ArrayList<String> parseInfo(String response) {
			ArrayList<String> ret = new ArrayList<String>();
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
				Document dom = db.parse(in);      
				Element docEle = dom.getDocumentElement();
				NodeList nl = docEle.getElementsByTagName("photo");
				Element entry = (Element)nl.item(0);
				Element owner = (Element) entry.getElementsByTagName("owner").item(0);
				Element title = (Element) entry.getElementsByTagName("title").item(0);
				Element description = (Element) entry.getElementsByTagName("description").item(0);
				Element visibility = (Element) entry.getElementsByTagName("visibility").item(0);
				Element dates = (Element) entry.getElementsByTagName("dates").item(0);

				String det1;
				String det = owner.getAttribute("username");
				det = "Owner : " + det;
				ret.add(det);
				det1 = title.getFirstChild().getNodeValue();
				det =  "Title : " + det1;
				ret.add(det);
				Element det2 = (Element) description.getFirstChild();
				if(det2 != null)
				{
					det1 = det2.getNodeValue();
					det = "Description : " + det1;
				}
				else
					det = "Description : ";
				ret.add(det);
				det = visibility.getAttribute("ispublic");
				if(det.equalsIgnoreCase("1"))
					det = "Visibility : Public";
				else
					det = "Visibility : Private";
				ret.add(det);
				det = dates.getAttribute("taken");
				det = det.substring(0,10);
				ret.add("Taken On : " + det);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ret;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(c); 
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
		}
	}
}