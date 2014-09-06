package ss.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;

public class BrwsScrn extends Activity {
    /** Called when the activity is first created. */
	ImageButton btnpicasa,btnflickr,btnyoutube,btnback;
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
	
	public void animatemiddle(final ImageButton btn,int starttime)
	{
		final TranslateAnimation a2middle=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_SELF, 0);
		a2middle.setFillAfter(true);
		a2middle.setDuration(200);
		a2middle.setStartOffset(starttime);
		a2middle.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			btn.setVisibility(View.VISIBLE);
			btn.setEnabled(false);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		btn.startAnimation(a2middle);
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
        btnpicasa=(ImageButton)findViewById(R.id.btnpicasa);
        btnflickr=(ImageButton)findViewById(R.id.btnflickr);
        btnyoutube=(ImageButton)findViewById(R.id.btnyoutube);
        btnback=(ImageButton)findViewById(R.id.btnback);
        btnpicasa.getBackground().setAlpha(0);
        btnflickr.getBackground().setAlpha(0);
        btnyoutube.getBackground().setAlpha(0);
        btnback.getBackground().setAlpha(0);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.brwsscrn);        
        initbuttons();
        
        btnback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				animatedown(btnpicasa,200);
				animatedown(btnyoutube,400);
				animatedown(btnflickr,600);
				animatedown(btnback,600);
				a.finish();
			}
		});
        
        btnflickr.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Intent gotoBrws = new Intent(c,Browse.class);	
    			gotoBrws.putExtra("START_TAB", "FLICKR");
    			startActivity(gotoBrws);
    		}
    	});
    	btnpicasa.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Intent gotoBrws = new Intent(c,Browse.class);	
    			gotoBrws.putExtra("START_TAB", "PICASA");
    			startActivity(gotoBrws);
    		}
    	});
    	btnyoutube.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Intent gotoBrws = new Intent(c,Browse.class);	
    			gotoBrws.putExtra("START_TAB", "YOUTUBE");
    			startActivity(gotoBrws);
    		}
    	});
    }

	@Override
	protected void onStart() {
		super.onStart();
		animateup(btnpicasa,300);	
		animateup(btnyoutube,600);
		animateup(btnflickr,900);
		animateup(btnback,1200);
	}
}