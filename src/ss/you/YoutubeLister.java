package ss.you;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import ss.client.GlobalVariable;
import ss.client.myClient;
import ss.client.GlobalVariable.keys;
import ss.ui.Browse;
import ss.ui.R;
import ss.ui.YoutubeAuth;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class YoutubeLister extends Activity {
	Context c= Browse.TabContext;
	String DEVELOPER_KEY ="AI39si6vLx_IJpUI9be9NzwtBYms49XYZhXC6NTRq4wue-Cp--9keQ3E_uKeJ__bVBNc9oY-iBJOMzyvdXC57AMr3SxH2necDw";
	Activity a=this;
	String AUTH_TOKEN=null;
	TextView tvResult;
	ListView MyVidView;
	ImageButton nxtBtn;
	ImageButton prevBtn;
	int start_index=1;
	int Choice;
	youtubeAdap youAdap;
    List<youtubeContent> feedList = null;
    Boolean pauseFlag=false;

    @Override
	public void onPause()
	{
		if(pauseFlag==true && youAdap!=null)
			youAdap.cancelAsyncTask();
		super.onPause();
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_lister);
        pauseFlag=false;
       	AUTH_TOKEN=((GlobalVariable)getApplication()).getValue(keys.YOU_TOKEN);
       	Choice=getIntent().getIntExtra("feedType", 0);
       	start_index=getIntent().getIntExtra("start-index", 1);
        tvResult = (TextView)findViewById(R.id.txtResult);
        MyVidView=(ListView) findViewById(R.id.listMyVid);
        MyVidView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(feedList.get(arg2).VidUrl)));
				}
        	});
        MyVidView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				final Dialog longClick = new Dialog(c);
				longClick.setContentView(R.layout.longclick_list);
				longClick.setTitle("Options");
				ListView albumOptions = (ListView) longClick.findViewById(R.id.LongClickList);
				if(Choice==0){
					String longClickList[]={"Details"};
					albumOptions.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, longClickList));
				}else{
					String longClickList[]={"Details"};
					albumOptions.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, longClickList));					
				}
					albumOptions.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, final int Clickpos, long arg3) {
					switch(Clickpos)
						{
						/*case 1:
								final AlertDialog alertDialog = new AlertDialog.Builder(c).create();
								alertDialog.setTitle("Delete Album " +feedList.get(Clickpos).title);
								alertDialog.setMessage("Are you sure ?");
								Toast.makeText(getApplicationContext(),feedList.get(Clickpos).delURL, Toast.LENGTH_LONG).show();
								alertDialog.setButton("OK",new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									try {
										String url = "http://gdata.youtube.com/feeds/api/users/default/uploads/" + feedList.get(Clickpos).videoID; 
										//String url =(feedList.get(Clickpos).delURL);
										HttpClient httpcl = new DefaultHttpClient();
										HttpDelete httpdel = new HttpDelete(url);
										httpdel.addHeader("Content-Type","application/atom+xml");
										httpdel.addHeader("Authorization",  "GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.YOU_TOKEN));
										httpdel.addHeader("GData-Version","2");
										httpdel.addHeader("X-GData-Key","key=" + DEVELOPER_KEY);
										//httpdel.addHeader("If-Match","*");
										HttpResponse httpres = httpcl.execute(httpdel);

										int rescode = httpres.getStatusLine().getStatusCode();
										if(rescode == 200)
										{
											Toast.makeText(getApplicationContext(), "Video Deleted", Toast.LENGTH_LONG).show();
											feedList.remove(Clickpos);
											youAdap.deletedrawable(Clickpos);
											youAdap.notifyDataSetChanged();
										}
										else
											Toast.makeText(getApplicationContext(), "Error Deleting Video"+rescode, Toast.LENGTH_LONG).show();

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
							break;*/
						case 0:
							longClick.setTitle("Details");
							ListView details = (ListView) longClick.findViewById(R.id.LongClickList);
							ArrayList<String> det;
							det = loaddetails(arg2);
							details.setAdapter(new ArrayAdapter<String>(c, R.layout.long_list, R.id.screen, det));
							details.setOnItemClickListener(null);
							break;
						}
					}

					private ArrayList<String> loaddetails(int pos) {
						String str;
						ArrayList<String> albumdet = new ArrayList<String>();
						str = "Title : " + feedList.get(pos).title;
						albumdet.add(str);
						str = "No. of Views : " + feedList.get(pos).noOfViews;
						albumdet.add(str);
						str = "Duraion : " + feedList.get(pos).duration+" seconds";
						albumdet.add(str);
						str = "Published On : " + feedList.get(pos).pblshd;
						albumdet.add(str);
						str = "Updated On : " + feedList.get(pos).uptd;
						albumdet.add(str);
					
						return albumdet;
					}
				});
				longClick.show();
				return false;
		
			}
		});
       	nxtBtn = (ImageButton) findViewById(R.id.btn_next);
       	nxtBtn.getBackground().setAlpha(0);
       	nxtBtn.setOnClickListener(new OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				start_index=start_index+10;
     				Intent moreAct = new Intent(c,YoutubeLister.class);
					moreAct.putExtra("feedType",Choice);
					moreAct.putExtra("start-index", start_index);
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
         				Intent moreAct = new Intent(c,YoutubeLister.class);
    					moreAct.putExtra("feedType",Choice);
    					moreAct.putExtra("start-index", start_index);
         				View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", moreAct
    							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
    							.getDecorView();  
    					YoutubeAuth.group.replace(view);
     				}
     			}
   		});	
        TextView tv = (TextView) findViewById(R.id.listTitle);
		//String userName=((GlobalVariable)c.getApplicationContext()).getValue(keys.YOU_USERNAME);
		switch(Choice)
    	{
    		case 0:	tv.setText("My Videos");
    				break;
    		case 1: tv.setText("Top Rated Videos");
		 		 	break;
    		case 2: tv.setText("Most Viewed Videos");
    				break;
    	}
       	new changePageTask().execute(Choice);
	}
	class changePageTask extends AsyncTask<Integer,String,Void> {
		private ProgressDialog dialog;
		Boolean errFlag;
		youParser abc;
		protected void onPreExecute() {
    		errFlag=false;
    		dialog = new ProgressDialog(Browse.TabContext);
			this.dialog.setCancelable(false);
			this.dialog.setIndeterminate(true);
			this.dialog.show();
    	}
		
		@Override
		protected Void doInBackground(Integer... Choice) {
			publishProgress("Retrieving Data");
			String URL=null;
			switch(Choice[0])
	    	{
	    		case 0:	URL="http://gdata.youtube.com/feeds/api/users/default/uploads";
	    				break;
	    		case 1: URL="http://gdata.youtube.com/feeds/api/standardfeeds/top_rated?max-results=11&start-index=" + start_index + "&time=today";
			 		 	break;
	    		case 2: URL="http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed?max-results=11&start-index=" + start_index + "time=today";
	    				break;
	    	}
			try{
				HttpGet request = new HttpGet(URL);
				if(Choice[0]==0)
	   	   			request.addHeader("Authorization",  "GoogleLogin auth=" + AUTH_TOKEN);
				request.addHeader("GData-Version","2");
				myClient userVid = new myClient();
	   	   		userVid.executeRequest(request);
	   	   		String myFeed=userVid.getResponse();
	   	   		abc = new youParser(myFeed);
		        feedList = abc.parse();
			}catch (SocketTimeoutException ea) {
				errFlag=true;
				publishProgress("No Internet Connection");
			}catch (UnknownHostException ea) {
				errFlag=true;
				publishProgress("No Internet Connection");
			}catch(InterruptedIOException e){
				errFlag=true;
				publishProgress("Socket Timeout");
			} catch (ClientProtocolException e) {
				errFlag=true;
				publishProgress("Protocol Error");
			}catch (Exception e) {
	    	 	errFlag=true;
	    	 	publishProgress(e.getMessage());
			}
			
			return null;
		}
		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
				if(abc!=null)
				{
					if(errFlag==false&&abc!=null)
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
							tvResult.setText("No Match Found");
							nxtBtn.setEnabled(false);
							prevBtn.setEnabled(false);
						}
					}
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
