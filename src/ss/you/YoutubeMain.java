package ss.you;

import java.net.UnknownHostException;     
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;


import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.client.myClient;
import ss.ui.R;
import ss.ui.YoutubeAuth;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class YoutubeMain extends Activity {

	Context c= this;
	ExpandableListView myELV;
	private ExpandableListAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_main);  
		myELV = (ExpandableListView) findViewById(R.id.EList);
		adapter = new myExpandableListAdapter(this);
		myELV.setAdapter(adapter);
		myELV.expandGroup(1);
		ImageButton srchbtn = (ImageButton) findViewById(R.id.btnSearchYT);
		srchbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent srchAct = new Intent(c,YoutubeSearch.class);
				srchAct.putExtra("start-index", 1);
				srchAct.putExtra("search-key", "");
				View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", srchAct
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
						.getDecorView();  
				YoutubeAuth.group.replace(view);

			}
		});

		myELV.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int arg0) {
				for(int i=0;i<adapter.getGroupCount();i++)
				{
					if(myELV.isGroupExpanded(i)==true && i!=arg0)
					{
						myELV.collapseGroup(i);
					}
				}

			}
		});

		myELV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

			}

		});
	}

	class myExpandableListAdapter extends BaseExpandableListAdapter {
		List<youtubeContent> feedList = null;
		List<youtubeContent> myVidfeedList = null;
		List<youtubeContent> topRatedfeedList = null;
		List<youtubeContent> mostViewedfeedList = null;
		List<String>category;
		Context context;
		private String AUTH_TOKEN = null;

		public myExpandableListAdapter(Context c) {
			AUTH_TOKEN =((GlobalVariable)getApplication()).getValue(keys.YOU_TOKEN);
			category = new ArrayList<String>();
			category.add("My Videos");
			category.add("Top Rated Videos");
			category.add("Most Viewed Videos");
			context=c;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return 1;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;

		}

		@Override
		public View getChildView(final int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=inflater.inflate(R.layout.you_child_list,parent,false);
			}
			String URL = null;

			switch(groupPosition)
			{
			case 0:
				if(((GlobalVariable)getApplication()).getValue(keys.YOU_USERNAME).equals("null"))
				{
					URL="notconfigured";
					feedList=null;
					myVidfeedList=null;
				}
				else
				{	
					URL="http://gdata.youtube.com/feeds/api/users/default/uploads"
						+ "?max-results=3";	
					feedList=myVidfeedList;
				}
				break; 
			case 1: URL="http://gdata.youtube.com/feeds/api/standardfeeds/top_rated"
				+ "?max-results=3&time=today";	
			feedList=topRatedfeedList;
			break;
			case 2:	URL="http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed"
				+ "?max-results=3&time=today";	
			feedList=mostViewedfeedList;
			break;
			}	
			Button btnMore = (Button) convertView.findViewById(R.id.btnMore);
			if(URL.equals("notconfigured"))
			{
				ListView lv =(ListView) convertView.findViewById(R.id.listVid);
				String s[] = {"Account Not Configured"};
				lv.setAdapter(new ArrayAdapter<String>(c ,R.layout.comment, R.id.screen, s));	
				btnMore.setVisibility(View.GONE);
			}
			else
			{
				try {
					if(feedList==null)
					{
						HttpGet request = new HttpGet(URL);
						if(groupPosition==0)
							request.addHeader("Authorization",  "GoogleLogin auth=" + AUTH_TOKEN);
						request.addHeader("GData-Version","2");
						myClient userVid = new myClient();
						userVid.executeRequest(request);
						String myFeed=userVid.getResponse();
						feedList=new youParser(myFeed).parse();
					}
					switch(groupPosition)
					{
					case 0:	myVidfeedList=feedList;
					break;
					case 1: topRatedfeedList=feedList;
					break;
					case 2:	mostViewedfeedList=feedList;
					break;
					}
					ListView lv =(ListView) convertView.findViewById(R.id.listVid);
					lv.setAdapter(new youtubeAdap(context, R.layout.youtubecontent, feedList,3,lv));
					lv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(feedList.get(arg2).VidUrl)));

						}
					});

					btnMore.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent moreAct = new Intent(context,YoutubeLister.class);
							moreAct.putExtra("feedType",groupPosition);
							moreAct.putExtra("start-index", 1);
							View view = YoutubeAuth.group.getLocalActivityManager().startActivity("YouAct", moreAct
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
									.getDecorView();  
							YoutubeAuth.group.replace(view);
						}
					});
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return category.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return category.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=inflater.inflate(R.layout.youtube_group,parent,false);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.listName);
			tv.setText(category.get(groupPosition));
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
	}
}
