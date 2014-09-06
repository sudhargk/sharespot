package ss.ui;

import java.util.ArrayList; 

import ss.flickr.flickrAlbumViewer;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FlickrAuth extends ActivityGroup {

	public static FlickrAuth group;
	private ArrayList<View> history;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.history = new ArrayList<View>();  
		group = this;
		View view = getLocalActivityManager().startActivity("FlickrAct", 
				new  Intent(this,flickrAlbumViewer.class)  
		.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))  
		.getDecorView();  
		replace(view);
	}

	public void replace(View v) {
		history.add(v); 
		setContentView(v);
	}

	public void back() {
		history.remove(history.size()-1);
		if(history.size() > 0) {  
			  
			setContentView(history.get(history.size()-1));  
		} else {  
			finish();  
		}  
	}  

	@Override  
	public void onBackPressed() {  
		FlickrAuth.group.back();  
		return;  
	}  
}
