package ss.ui;

import java.util.ArrayList;

import ss.you.YoutubeMain;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

 public class YoutubeAuth extends ActivityGroup
    {
	 
	 public static YoutubeAuth group;
	 private ArrayList<View> history;
    	public void onCreate(Bundle icicle) {
    	    super.onCreate(icicle);
    	    group = this;
    	    history=new ArrayList<View>();
    	    View view = getLocalActivityManager().startActivity("YouAct", 
					 	new  Intent(this,YoutubeMain.class)  
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
    		 YoutubeAuth.group.back();  
    		 return;  
    	 }  
    }