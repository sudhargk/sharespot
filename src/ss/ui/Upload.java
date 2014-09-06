package ss.ui;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Upload extends TabActivity {

	public static Context TabContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_layout);
		TabContext = this;

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, imgSelect.class);

		// Initialise a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("img").setIndicator("Images",
				res.getDrawable(R.drawable.ic_tab_image))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, vidSelect.class);
		spec = tabHost.newTabSpec("vids").setIndicator("Videos",
				res.getDrawable(R.drawable.ic_tab_video))
				.setContent(intent);
		tabHost.addTab(spec);
		intent = new Intent().setClass(this, Queue.class);
		spec = tabHost.newTabSpec("allmed").setIndicator("Go",
				res.getDrawable(R.drawable.ic_tab_go))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
}