package ss.picasa;

import java.io.ByteArrayInputStream; 
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ss.cache.CachePolicy.Cache_Options;
import ss.client.GlobalVariable;
import ss.client.myClient;
import ss.ui.Browse;
import ss.ui.PicasaAuth;
import ss.ui.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class picasaSearch extends Activity {
	List<picasaImageContent> feedList = null;
	Context c =Browse.TabContext;
	Activity a =this;
	GridView MyImgView;
	int start_index=1;
	String srchKey;
	ImageButton nxtBtn,prevBtn;
	EditText searchBox;
	Boolean pauseFlag=false;
	Boolean resumeFlag = false;
	TextView tvQuery,tvResult;
	picasaImageAdap picAdap;
	
	@Override
	public void onBackPressed() {
		PicasaAuth.group.back();
		super.onBackPressed();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picasa_search);  
		start_index=getIntent().getIntExtra("start-index", 1);
		srchKey=getIntent().getStringExtra("search-key");
		searchBox = (EditText) findViewById(R.id.searchKey);
		searchBox.setText(srchKey);
		tvQuery = (TextView) findViewById(R.id.txtQuery);
		tvResult = (TextView) findViewById(R.id.txtResult);
		MyImgView=(GridView) findViewById(R.id.searchList);
		MyImgView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				Intent myI = new Intent(c, picasaImage.class);
				myI.putExtra("pos",pos);
				myI.putExtra("albumID", picAdap.getItem(pos).albumID);
				startActivity(myI);
			}
		});
		Button searchBtn = (Button) findViewById(R.id.searchButton);
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				srchKey=searchBox.getText().toString();
				if(srchKey.equalsIgnoreCase("")!=true){
					((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.VISIBLE);
					start_index = 1;
					Intent moreAct = new Intent(c,picasaSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", searchBox.getText().toString());
					View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct4", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					PicasaAuth.group.replace(view);	
				}else{
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
				start_index=start_index+12;
				Intent moreAct = new Intent(c,picasaSearch.class);
				moreAct.putExtra("start-index", start_index);
				moreAct.putExtra("search-key", srchKey);
				View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct5", moreAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				PicasaAuth.group.replace(view);			
			}
		});
		prevBtn = (ImageButton) findViewById(R.id.btn_prev);
		if(start_index==1)
			prevBtn.setEnabled(false);
		prevBtn.getBackground().setAlpha(0);
		prevBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(start_index>12)
				{
					start_index=start_index-12;
					Intent moreAct = new Intent(c,picasaSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", srchKey);
					View view = PicasaAuth.group.getLocalActivityManager().startActivity("PicAct6", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					PicasaAuth.group.replace(view);
				}
			}
		});

		if(srchKey.equalsIgnoreCase(""))
			((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.GONE);
		else{
			((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.VISIBLE);
			new changePageTask().execute();
		}
	}
	@Override
	public void onPause()
	{
		if(pauseFlag==true && picAdap!=null)
			picAdap.cancelAsyncTask();
		super.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(resumeFlag==true)
			picAdap.resumeAsyncTask();
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
			String key=srchKey;
			key= key.replaceAll("\\s+", "+");
			String srchUrl="https://picasaweb.google.com/data/feed/api/all"
				+"?q="+key
				+"&start-index="+start_index
				+"&max-results=13"
				+"&v=2";
			try {
				HttpGet request= new HttpGet(srchUrl);
				myClient srchVid = new myClient();
				srchVid.executeRequest(request);
				String myFeed=srchVid.getResponse();
				feedList = parse(myFeed);
			} catch (Exception e) {
				errFlag=true;
				publishProgress("No Internet Connection"+e.getMessage());
			}
			return null;
		}
		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				if(errFlag==false)
				{
					int numResults=feedList.size();

					if(numResults>0)
					{
						if(numResults<13)
							nxtBtn.setEnabled(false);
						else{
							nxtBtn.setEnabled(true);
							feedList.remove(12);
							numResults=12;
						}
						tvResult.setText(start_index + "-" + (start_index+numResults-1));
						picAdap = new picasaImageAdap(c, R.layout.picasaimage, feedList, numResults, MyImgView, Cache_Options.NO_CACHE);
						MyImgView.setAdapter(picAdap);
						pauseFlag=true;
						resumeFlag=true;
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
					Element albID = (Element)entry.getElementsByTagName("gphoto:albumid").item(0);
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
					Element author =(Element)entry.getElementsByTagName("author").item(0);
					String strUserID,strAlbumID,strThmb,strURL,strphotoid,strphototitle,strSummary,strSize,strHeight,strWidth,delhref;
					strUserID = author.getElementsByTagName("email").item(0).getFirstChild().getNodeValue();
					strAlbumID = albID.getFirstChild().getNodeValue();
					Log.d("userID",strUserID);
					Log.d("alb ID",strAlbumID);
					strThmb = mthmb.getAttribute("url").toString();
					strURL = url.getAttribute("url").toString();
					strphotoid= photoid.getFirstChild().getNodeValue();
					if(phototitle.getFirstChild()!=null)
						strphototitle=phototitle.getFirstChild().getNodeValue();
					else
						strphototitle="";
					if(summary.getFirstChild()!=null)
						strSummary=summary.getFirstChild().getNodeValue();
					else
						strSummary="";
					strSize=size.getFirstChild().getNodeValue();
					strHeight=height.getFirstChild().getNodeValue();
					strWidth=width.getFirstChild().getNodeValue();
					delhref = delURL.getAttribute("href");

					picasaImageContent p = new picasaImageContent(strUserID, strAlbumID, strThmb, strphotoid, strphototitle, strURL,strSize,strHeight,strWidth,strSummary,delhref);
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