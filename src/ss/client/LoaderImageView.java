package ss.client;

import java.io.IOException;
import java.net.MalformedURLException;

import ss.cache.CachePolicy;
import ss.cache.CachePolicy.Cache_Options;
import ss.cache.CachePolicy.strngs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class LoaderImageView extends LinearLayout{

	private static final int COMPLETE = 0;
	private static final int FAILED = 1;
	private static final int LOADING = 2;

	private Context mContext;
	private Drawable mDrawable;
	private ProgressBar mSpinner;
	private ImageView mImage;

	public LoaderImageView(final Context context, final AttributeSet attrSet) {
		super(context, attrSet);
		final String url = attrSet.getAttributeValue(null, "image");
		if(url != null){
			instantiate(context, url);
		} else {
			instantiate(context, null);
		}
	}

	public LoaderImageView(final Context context, final String imageUrl) {
		super(context);
		instantiate(context, imageUrl);		
	}

	private void instantiate(final Context context, final String imageUrl) {
		mContext = context;

		mImage = new ImageView(mContext);
		mImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mImage.setScaleType(ScaleType.FIT_CENTER);
		mSpinner = new ProgressBar(mContext);
		mSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER);

		mSpinner.setIndeterminate(true);

		addView(mSpinner);
		addView(mImage);

		if(imageUrl != null){
			setImageDrawableDefault(imageUrl);
		}
	}

	public void setDrawable(Drawable d) {
		if(d == null)
			imageLoadedHandler.sendEmptyMessage(LOADING);
		else
		{
			mDrawable = d;
			imageLoadedHandler.sendEmptyMessage(COMPLETE);
		}
	}

	private final Handler imageLoadedHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case COMPLETE:
				mImage.setImageDrawable(mDrawable);
				mImage.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			case LOADING:
				mImage.setVisibility(View.GONE);
				mSpinner.setVisibility(View.VISIBLE);
				break;
			case FAILED:
			default:
				Toast.makeText(getContext().getApplicationContext(), "Error Loading Image", Toast.LENGTH_LONG);
				break;
			}
			return true;
		}		
	});

	private static Drawable getDrawableFromUrl(final String url) throws IOException, MalformedURLException {
		return Drawable.createFromStream(((java.io.InputStream)new java.net.URL(url).getContent()), "name");
	}

	public void setImageDrawableDefault(final String uRL) {
		mDrawable = null;
		mSpinner.setVisibility(View.VISIBLE);
		mImage.setVisibility(View.GONE);
		new Thread(){
			public void run() {
				try {
					mDrawable = getDrawableFromUrl(uRL);
					imageLoadedHandler.sendEmptyMessage(COMPLETE);
				} catch (MalformedURLException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (IOException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				}
			};
		}.start();
	}
	
	public void setImageDrawable(final String uRL, final strngs site, final Context c, final String photoID) {
		mDrawable = null;
		mSpinner.setVisibility(View.VISIBLE);
		mImage.setVisibility(View.GONE);
		new Thread(){
			public void run() {
				try {
					CachePolicy cp = new CachePolicy(c);
					mDrawable = cp.image_load(site, strngs.TYPE_IMAGE_LARGE, photoID, uRL, Cache_Options.ALLOW_CACHE);
					imageLoadedHandler.sendEmptyMessage(COMPLETE);
				} catch (MalformedURLException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (IOException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
}
