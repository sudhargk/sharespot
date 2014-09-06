package ss.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ErrorDialog {
	
	public static void UnknownHostDialog(String text,Context c,final Activity a) {
		new AlertDialog.Builder(c)
		.setTitle("Error")
		.setMessage(text)
		.setNeutralButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				a.finish();
				
			}
		})
		.show();	
	}
}