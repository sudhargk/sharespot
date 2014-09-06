package ss.ui;


import ss.upload.imgUpload;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class imgSelect extends ActivityGroup
{
	Context c = this;
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		View view = getLocalActivityManager().startActivity("UploadAct", 
				new  Intent(this, imgUpload.class)  
		.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))  
		.getDecorView();  
		setContentView(view);  
	}
}