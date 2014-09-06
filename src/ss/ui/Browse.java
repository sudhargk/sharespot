package ss.ui;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Browse extends TabActivity {
	
	public static Context TabContext;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tab_layout);
		TabContext=this;
		Resources res = getResources(); // Resource object to get Drawables
		final TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab	

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, YoutubeAuth.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("youtubebrws").setIndicator("Youtube",
				res.getDrawable(R.drawable.ic_tab_youtube))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, PicasaAuth.class);
		spec = tabHost.newTabSpec("picasabrws").setIndicator("Picasa",
				res.getDrawable(R.drawable.ic_tab_picasa))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FlickrAuth.class);
		spec = tabHost.newTabSpec("flickrbrws").setIndicator("Flickr",
				res.getDrawable(R.drawable.ic_tab_flickr))
				.setContent(intent);
		tabHost.addTab(spec);

		String tab_index= getIntent().getStringExtra("START_TAB");
		if(tab_index.equalsIgnoreCase("youtube"))
			tabHost.setCurrentTab(0);
		else if(tab_index.equalsIgnoreCase("picasa"))
			tabHost.setCurrentTab(1);
		else if(tab_index.equalsIgnoreCase("FLICKR"))
			tabHost.setCurrentTab(2);
	}
}