package ss.ui;

import ss.client.GlobalVariable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;

public class MainScrn extends Activity {

	/** Called when the activity is first created. */
	ImageButton btnbrws,btnupload,btnsettings,btnexit;
	Context c = this;
	Activity a = this;

	public void animatedown(ImageButton btn, int starttime)
	{
		final TranslateAnimation a2down=new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 800);
		a2down.setFillAfter(true);
		a2down.setDuration(150);
		a2down.setStartOffset(starttime);
		btn.startAnimation(a2down);
		btn.setVisibility(View.GONE);
	}

	public void animateup(final ImageButton btn, int starttime)
	{
		final TranslateAnimation a2top=new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, 800, Animation.RELATIVE_TO_SELF);
		a2top.setFillAfter(true);
		a2top.setDuration(150);
		a2top.setStartOffset(starttime);
		a2top.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				btn.setVisibility(View.VISIBLE);
				btn.setEnabled(true);
				btn.setFocusable(true);
				btn.setClickable(true);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		btn.startAnimation(a2top);
	}

	public void initbuttons()
	{
		btnbrws=(ImageButton)findViewById(R.id.btnbrowse);
		btnupload=(ImageButton)findViewById(R.id.btnupload);
		btnsettings=(ImageButton)findViewById(R.id.btnsettings);
		btnexit=(ImageButton)findViewById(R.id.btnexit);
		btnbrws.getBackground().setAlpha(0);
		btnupload.getBackground().setAlpha(0);
		btnsettings.getBackground().setAlpha(0);
		btnexit.getBackground().setAlpha(0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainscrn);
		initbuttons();

		final TranslateAnimation a2right=new TranslateAnimation(Animation.RELATIVE_TO_SELF, 800, Animation.RELATIVE_TO_SELF,0);
		a2right.setDuration(150);
		a2right.setFillAfter(true);

		btnbrws.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				btnbrws.startAnimation(a2right);
				animatedown(btnexit, 150);
				animatedown(btnsettings,300);
				animatedown(btnupload, 450);
				Intent go2brws = new Intent(c, BrwsScrn.class);
				startActivity(go2brws);
			}
		});

		btnupload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent gotoUpld = new Intent(c,Upload.class);	
				startActivity(gotoUpld);
			}
		});

		btnsettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent gotostng = new Intent(c,Settings.class);	
				startActivity(gotostng);
			}
		});

		btnexit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(((GlobalVariable)getApplication()).IsUpload())
				{
					AlertDialog alertDialog = new AlertDialog.Builder(c).create();
					alertDialog.setTitle("Upload is Running");
					alertDialog.setMessage("Are you sure you want to cancel and exit ?");
					alertDialog.setButton("OK",new DialogInterface.OnClickListener() {	

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							a.finish();
						}
					});

					alertDialog.setButton2("Cancel",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					alertDialog.setIcon(R.drawable.icon);
					alertDialog.show();
				}
				else
					a.finish();
			}
		});
	}
	
	

	@Override
	public void onBackPressed() {
		if(((GlobalVariable)getApplication()).IsUpload())
		{
			AlertDialog alertDialog = new AlertDialog.Builder(c).create();
			alertDialog.setTitle("Upload is Running");
			alertDialog.setMessage("Are you sure you want to cancel and exit ?");
			alertDialog.setButton("OK",new DialogInterface.OnClickListener() {	

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					a.finish();
				}
			});

			alertDialog.setButton2("Cancel",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			alertDialog.setIcon(R.drawable.icon);
			alertDialog.show();
		}
		else
			a.finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		animateup(btnbrws,150);
		animateup(btnexit,900);
		animateup(btnsettings,600);
		animateup(btnupload,300);
	}
}