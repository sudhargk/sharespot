package ss.ui;

import ss.upload.videosUpload;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class vidSelect extends ActivityGroup
{
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		View view = getLocalActivityManager().startActivity("UploadAct", 
				new  Intent(this,videosUpload.class)  
		.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))  
		.getDecorView();  
		setContentView(view);  
	}
}