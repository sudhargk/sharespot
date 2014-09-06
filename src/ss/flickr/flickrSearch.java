package ss.flickr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ss.cache.CachePolicy.Cache_Options;
import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.ui.Browse;
import ss.ui.FlickrAuth;
import ss.ui.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class flickrSearch extends Activity {
	List<flickrImageContent> feedList = null;
	Context c = Browse.TabContext;
	Activity a = this;
	GridView MyImgView;
	int start_index=1;
	String srchKey;
	ImageButton nxtBtn,prevBtn;
	EditText searchBox;
	Boolean pauseFlag = false;
	Boolean resumeFlag= false;
	TextView tvQuery,tvResult;
	String token;
	flickrImageAdap flickrAdap;

	public void onCreate(Bundle savedInstanceState) {
		resumeFlag = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picasa_search);

		token = ((GlobalVariable)getApplication()).getValue(keys.FLICKR_TOKEN);
		start_index = getIntent().getIntExtra("start-index", 1);
		srchKey = getIntent().getStringExtra("search-key");

		searchBox = (EditText) findViewById(R.id.searchKey);
		searchBox.setText(srchKey);

		tvQuery = (TextView) findViewById(R.id.txtQuery);
		tvResult = (TextView) findViewById(R.id.txtResult);
		MyImgView =(GridView) findViewById(R.id.searchList);

		MyImgView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,	int pos, long arg3) {
				Intent myI = new Intent(c, flickrImage.class);
				myI.putExtra("pos",pos);
				myI.putExtra("Search", true);
				startActivity(myI);
			}
		});

		Button searchBtn = (Button) findViewById(R.id.searchButton);
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				srchKey = searchBox.getText().toString();
				if(srchKey.trim().equalsIgnoreCase("")!=true){
					((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.VISIBLE);
					start_index = 1;
					Intent moreAct = new Intent(c, flickrSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", searchBox.getText().toString());
					View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					FlickrAuth.group.replace(view);	
				}
				else
				{
					Toast.makeText(c, "Search Key is not entered", Toast.LENGTH_SHORT).show();
					((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.GONE);
				}
			}
		});

		nxtBtn = (ImageButton) findViewById(R.id.btn_next);
		nxtBtn.getBackground().setAlpha(0);
		nxtBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_index=start_index + 1;
				Intent moreAct = new Intent(c, flickrSearch.class);
				moreAct.putExtra("start-index", start_index);
				moreAct.putExtra("search-key", srchKey);
				View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", moreAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				FlickrAuth.group.replace(view);			
			}
		});

		prevBtn = (ImageButton) findViewById(R.id.btn_prev);
		if(start_index==1)
			prevBtn.setEnabled(false);
		prevBtn.getBackground().setAlpha(0);
		prevBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(start_index > 1)
				{
					start_index=start_index - 1;
					Intent moreAct = new Intent(c, flickrSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", srchKey);
					View view = FlickrAuth.group.getLocalActivityManager().startActivity("FlickrAct", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					FlickrAuth.group.replace(view);
				}
			}
		});

		if(srchKey.trim().equalsIgnoreCase(""))
			((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.GONE);
		else{
			((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.VISIBLE);
			new changePageTask().execute();
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		if(resumeFlag==true)
			flickrAdap.resumeAsyncTask();
	}

	@Override
	public void onPause()
	{
		if(pauseFlag == true && flickrAdap != null)
			flickrAdap.cancelAsyncTask();
		super.onPause();
	}

	class changePageTask extends AsyncTask<Void,String,Void> {
		private ProgressDialog dialog;
		Boolean errFlag;

		@Override
		protected void onPreExecute() {
			errFlag=false;
			dialog = new ProgressDialog(Browse.TabContext);
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
			tvQuery.setText("Search Results of \""+srchKey+"\"");
		}

		protected Void doInBackground(Void... arg0) {
			publishProgress("Retrieving Data");
			String key = srchKey;
			try {
				String signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025extrasurl_t,url_mmethodflickr.photos.searchpage" + start_index + "per_page13text" + key;
				key = key.replaceAll("\\s+", "%20");
				String request = "http://api.flickr.com/services/rest/" +
				"?method=flickr.photos.search" +
				"&api_key=6bdb55025361207fd970368b17e2c025" +
				"&text=" + key +
				"&per_page=13" +
				"&page=" + start_index +
				"&extras=url_t,url_m" +
				"&api_sig=" + flickrExecute.md5(signature);
				String response = new flickrExecute(request).execute_get();
				feedList = parse(response);
				flickrImageViewer.imageList = feedList;
			} catch (Exception e) {
				e.printStackTrace();
				errFlag=true;
				publishProgress("Unsuccessful");
			}
			return null;
		}

		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				if(errFlag==false)
				{
					int numResults = feedList.size();

					if(numResults>0)
					{
						if(numResults<13)
							nxtBtn.setEnabled(false);
						else
						{
							nxtBtn.setEnabled(true);
							feedList.remove(12);
							numResults=12;
						}
						tvResult.setText(((start_index-1)*12+1) + "-" + (((start_index-1)*12+1) + numResults-1));
						flickrAdap = new flickrImageAdap(c, R.layout.picasaimage, feedList, numResults, MyImgView, Cache_Options.NO_CACHE);
						MyImgView.setAdapter(flickrAdap);
						pauseFlag = true;
						resumeFlag = true;
					}
					else
						tvResult.setText("No Match Found");
				}
				InputMethodManager inputManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(((Activity) c).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
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

}