package ss.you;

import java.util.List; 

import org.apache.http.client.methods.HttpGet;

import ss.client.myClient;
import ss.ui.Browse;
import ss.ui.R;
import ss.ui.YoutubeAuth;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class YoutubeSearch extends Activity {
	List<youtubeContent> feedList = null;
	Context c =Browse.TabContext;
	Activity a =this;
	ListView MyVidView;
	int start_index=1;
	String srchKey;
	ImageButton nxtBtn,prevBtn;
	EditText searchBox;
	Boolean pauseFlag=false;
	TextView tvQuery,tvResult;
	youtubeAdap youAdap;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_search);  
		start_index=getIntent().getIntExtra("start-index", 1);
		srchKey=getIntent().getStringExtra("search-key");
		searchBox = (EditText) findViewById(R.id.searchKey);
		searchBox.setText(srchKey);
		tvQuery = (TextView) findViewById(R.id.txtQuery);
		tvResult = (TextView) findViewById(R.id.txtResult);
		MyVidView=(ListView) findViewById(R.id.searchList);
		MyVidView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(feedList.get(arg2).VidUrl)));
			}
		});
		Button searchBtn = (Button) findViewById(R.id.searchButton);
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				srchKey=searchBox.getText().toString();
				if(srchKey.equalsIgnoreCase("")!=true){
					((RelativeLayout) findViewById(R.id.you_srch_result)).setVisibility(View.VISIBLE);
					start_index=1;
					Intent moreAct = new Intent(c,YoutubeSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", searchBox.getText().toString());
					View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					YoutubeAuth.group.replace(view);	
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
				start_index=start_index+10;
				Intent moreAct = new Intent(c,YoutubeSearch.class);
				moreAct.putExtra("start-index", start_index);
				moreAct.putExtra("search-key", srchKey);
				View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", moreAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				YoutubeAuth.group.replace(view);			
			}
		});
		prevBtn = (ImageButton) findViewById(R.id.btn_prev);
		if(start_index==1)
			prevBtn.setEnabled(false);
		prevBtn.getBackground().setAlpha(0);
		prevBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(start_index>10)
				{
					start_index=start_index-10;
					Intent moreAct = new Intent(c,YoutubeSearch.class);
					moreAct.putExtra("start-index", start_index);
					moreAct.putExtra("search-key", srchKey);
					View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", moreAct
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
							.getDecorView();  
					YoutubeAuth.group.replace(view);
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
		if(pauseFlag==true && youAdap!=null)
			youAdap.cancelAsyncTask();
		super.onPause();
	}

	class changePageTask extends AsyncTask<Void,String,Void> {

		private ProgressDialog dialog;
		Boolean errFlag;
		youParser abc;
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
			String srchUrl="http://gdata.youtube.com/feeds/api/videos"
				+"?q="+key
				+"&start-index="+start_index
				+"&max-results=11"
				+"&v=2";
			try {
				HttpGet request= new HttpGet(srchUrl);
				myClient srchVid = new myClient();
				srchVid.executeRequest(request);
				String myFeed=srchVid.getResponse();
				abc = new youParser(myFeed);
				feedList = abc.parse();
			} catch (Exception e) {
				errFlag=true;
				publishProgress("No Internet Connection"+e.getMessage());
			}
			return null;
		}
		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				if(abc!=null)
				{
					if(errFlag==false)
					{
						int numResults=abc.Result;

						if(numResults>0)
						{
							if(numResults<11)
								nxtBtn.setEnabled(false);
							else{
								nxtBtn.setEnabled(true);
								feedList.remove(10);
								numResults=10;
							}
							tvResult.setText(start_index+"-"+(start_index+numResults-1));
							youAdap = new youtubeAdap(c, R.layout.youtubecontent, feedList,numResults,MyVidView);
							MyVidView.setAdapter(youAdap);
							pauseFlag=true;
						}
						else{
							nxtBtn.setEnabled(false);
							prevBtn.setEnabled(false);
							tvResult.setText("No Match Found");
						}
					}
					InputMethodManager inputManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(((Activity) c).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}
		protected void onProgressUpdate(String... args) {
			if(errFlag==false)
				dialog.setMessage(args[0]);
			else{
				this.cancel(true);   
				Toast.makeText(c, args[0], Toast.LENGTH_LONG).show();
				errFlag=false;
				a.finish();
			}
		}
	}
}