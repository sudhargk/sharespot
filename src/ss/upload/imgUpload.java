package ss.upload;

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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ss.client.GlobalVariable;
import ss.client.uploadContent;
import ss.client.GlobalVariable.keys;
import ss.picasa.picasaAddAlbum;
import ss.picasa.picasaAlbumContent;
import ss.ui.R;
import ss.ui.Upload;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class imgUpload extends Activity {

	Cursor cc;
	Context c;
	ListView myLV;
	List<Boolean> CheckedList = new ArrayList<Boolean>();
	List<String> fylPathList = new ArrayList<String>();
	List<Integer>fylIndexPos = new ArrayList<Integer>();
	Activity a = this;
	List<picasaAlbumContent> picAlbumList;
	Spinner picAlbums;
	List<String> AlbumTitle = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.fyl_layout);

		c = Upload.TabContext;

		cc = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,null);  
		startManagingCursor(cc);

		String[] columns = new String[] {cc.getColumnName(1)};
		int[] names = new int[] {R.id.fylthmb};
		ListAdapter lAdapter = new ImageCursorAdapter(this, R.layout.fyl_content, cc, columns, names);

		myLV = (ListView) findViewById(R.id.fylList); 
		myLV.setAdapter(lAdapter);

		for(int pos=0;pos<cc.getCount();pos++)
			CheckedList.add(pos,false);


		myLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
				cc.moveToPosition(pos);
				CheckBox cb =((CheckBox)view.findViewById(R.id.fylCB));
				cb.toggle();
				CheckedList.set(pos, cb.isChecked());
			}
		});


		Button addToQueue = (Button) findViewById(R.id.addToQ);
		if(cc.getCount()>0)
			addToQueue.setVisibility(View.VISIBLE);
		else
			addToQueue.setVisibility(View.GONE);

		addToQueue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				fylIndexPos.clear();
				fylPathList.clear();
				for(int pos=0;pos<cc.getCount();pos++)
				{
					if(CheckedList.get(pos).booleanValue())
					{
						cc.moveToPosition(pos);
						fylPathList.add(cc.getString(1));
						fylIndexPos.add(Integer.parseInt(cc.getString(0)));
					}
				}
				if(fylIndexPos.size()==0)
					Toast.makeText(c,"No Items Selected",Toast.LENGTH_SHORT).show();
				else
				{
					if(((GlobalVariable)getApplication()).getValue(keys.PICASA_USERNAME).equals("null")&&((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
						Toast.makeText(c,"No Image Sharing Accounts Configured.",Toast.LENGTH_SHORT).show();
					else
					{
						final Dialog img_diag = new Dialog(Upload.TabContext);

						img_diag.setContentView(R.layout.img_upld_dialog);
						picAlbums=(Spinner) img_diag.findViewById(R.id.spnrAlbums);
						picAlbums.setEnabled(false);
						final CheckBox isPicasa = (CheckBox) img_diag.findViewById(R.id.cbImgpicasa);
						final CheckBox isFlickr = (CheckBox) img_diag.findViewById(R.id.cbImgflickr);
						final CheckBox isFlickrPrivate = (CheckBox) img_diag.findViewById(R.id.cbflickrPrivate);
						RelativeLayout PicasaLayout= (RelativeLayout) img_diag.findViewById(R.id.picasa);
						RelativeLayout FlickrLayout= (RelativeLayout) img_diag.findViewById(R.id.flickr);

						Button btn_OK,btn_Cancel;
						final Button btn_Add;
						btn_Add = (Button) img_diag.findViewById(R.id.plus);

						if(((GlobalVariable)getApplication()).getValue(keys.PICASA_USERNAME).equals("null"))
							PicasaLayout.setVisibility(View.GONE);
						else
							PicasaLayout.setVisibility(View.VISIBLE);

						if(((GlobalVariable)getApplication()).getValue(keys.FLICKR_USERNAME).equals("null"))
							FlickrLayout.setVisibility(View.GONE);
						else
							FlickrLayout.setVisibility(View.VISIBLE);					

						isPicasa.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton arg0, boolean checked) {
								if(checked==true)
								{
									new getAlbums().execute();
									picAlbums.setEnabled(true);
									btn_Add.setEnabled(true);
								}
								else if(checked==false)
								{
									picAlbumList.clear();
									picAlbums.setEnabled(false);
									btn_Add.setEnabled(false);
								}
							}
						});

						isFlickr.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton arg0, boolean checked) {
								isFlickrPrivate.setEnabled(checked);
							}
						});

						img_diag.setTitle("Add To Queue ( "+ fylIndexPos.size()+" Item )");

						btn_Add.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent i = new Intent(c, picasaAddAlbum.class);
								startActivity(i);
								img_diag.dismiss();
							}
						});

						btn_OK = (Button) img_diag.findViewById(R.id.btnOk);
						btn_Cancel = (Button) img_diag.findViewById(R.id.btnCancel);

						btn_OK.setOnClickListener(new OnClickListener(){
							public void onClick(View arg0) {
								if(isFlickr.isChecked() || isPicasa.isChecked())
								{
									uploadContent uc;
									if(isPicasa.isChecked())
									{
										uc = new uploadContent(picAlbums.getSelectedItem().toString(),
												picAlbumList.get(picAlbums.getSelectedItemPosition()).AlbumID ,fylPathList,fylIndexPos,
												isPicasa.isChecked(),isFlickr.isChecked(),isFlickrPrivate.isChecked());
										((GlobalVariable)getApplication()).addToBasket(uc);
									}
									else if(isFlickr.isChecked())
									{
										uc = new uploadContent(null,null,fylPathList,fylIndexPos,isPicasa.isChecked(),isFlickr.isChecked(),isFlickrPrivate.isChecked());
										((GlobalVariable)getApplication()).addToBasket(uc);
									}
									img_diag.dismiss();
									fylPathList.clear();
									fylIndexPos.clear();
									Toast.makeText(Upload.TabContext, "Added to Queue", Toast.LENGTH_SHORT).show();
								}

								else
									Toast.makeText(Upload.TabContext, "Uploading Site Not Selected", Toast.LENGTH_SHORT).show();
							}
						});


						btn_Cancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								img_diag.dismiss();
								fylPathList.clear();
								fylIndexPos.clear();
							}
						});

						img_diag.show();
					}
				}
			}
		});
	}

	private class getAlbums extends AsyncTask<Void,String, Void>
	{
		private ProgressDialog dialog;
		Boolean errFlag;
		protected void onPreExecute() {
			errFlag=false;
			AlbumTitle=new ArrayList<String>();
			picAlbumList=new ArrayList<picasaAlbumContent>();
			dialog = new ProgressDialog(c); 
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
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
				picAlbumList= parse(sb.toString());
			}catch (UnknownHostException ea) {
				errFlag=true;
				publishProgress("No Internet Connection");
			} catch (ClientProtocolException e) {
				errFlag=true;
				publishProgress("Protocol Error");
			} catch (IOException e) {
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
				finish();
				Toast.makeText(c, args[0], Toast.LENGTH_LONG).show();
				errFlag=false;
			}
		}

		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				for(int pos=0;pos<picAlbumList.size();pos++)
					AlbumTitle.add(picAlbumList.get(pos).title);
				picAlbums.setAdapter(new ArrayAdapter<String>(c,R.layout.comment,R.id.screen,AlbumTitle));
			}
		}

		private List<picasaAlbumContent> parse(String myFeed) {
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
					Element albumID =(Element)entry.getElementsByTagName("gphoto:id").item(0);
					String strTitle = title.getFirstChild().getNodeValue();   
					String strAlbumId =albumID.getFirstChild().getNodeValue();
					content.add(new picasaAlbumContent(strTitle,strAlbumId));
					cnt++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return content;
		}
	}

	public class ImageCursorAdapter extends SimpleCursorAdapter {
		private Cursor myCursor;
		private Context context;

		public ImageCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, 0, c, from, to);
			this.myCursor = c;
			this.context = context;
		}

		public View getView(int pos, View inView, ViewGroup parent) {
			if (inView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				inView = inflater.inflate(R.layout.fyl_content, null);
			}
			myCursor.moveToPosition(pos);
			String fylPath = myCursor.getString(1);
			TextView tvName = (TextView)inView.findViewById(R.id.fylName);
			tvName.setText(fylPath.substring(fylPath.lastIndexOf("/")+1));
			String fylSize = myCursor.getString(2);
			TextView tvSize = (TextView)inView.findViewById(R.id.fylSize);
			tvSize.setText(fylSize+" bytes");
			CheckBox selectedCB = (CheckBox) inView.findViewById(R.id.fylCB);
			if(CheckedList.get(pos).booleanValue()==true)
				selectedCB.setChecked(true);
			else
				selectedCB.setChecked(false);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			ImageView imgThmb = (ImageView) inView.findViewById(R.id.fylthmb);
			Bitmap curThumb = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), Integer.parseInt(cc.getString(0)), MediaStore.Images.Thumbnails.MICRO_KIND, options);
			imgThmb.setImageBitmap(curThumb);
			return inView;
		}
	}
}
