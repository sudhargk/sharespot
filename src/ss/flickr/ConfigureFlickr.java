package ss.flickr;

import ss.backend.DataBaseHelper; 
import ss.client.GlobalVariable;
import ss.client.GlobalVariable.keys;
import ss.ui.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class ConfigureFlickr extends Activity {

	private String signature;
	private String request;
	private String response;
	private String frob;
	private String token;
	WebView wv;
	private Context c = this;

	@Override
	public void onBackPressed() {
		setResult(102);
		this.finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.webv);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		final Activity MyActivity = this;
		wv = (WebView) findViewById(R.id.webv);
		wv.clearHistory();
		c.deleteDatabase("webview.db");
		c.deleteDatabase("webviewCache.db");
		CookieManager.getInstance().removeAllCookie();
		wv.clearCache(true);
		try {
			signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025methodflickr.auth.getFrob";
			request = "http://api.flickr.com/services/rest/"
				+ "?method=flickr.auth.getFrob&"
				+ "api_key=6bdb55025361207fd970368b17e2c025"
				+ "&api_sig=" + flickrExecute.md5(signature);
			response = new flickrExecute(request).execute_get();	        
			frob = response.substring(response.lastIndexOf("<frob>")+6, response.lastIndexOf("</frob>"));

			signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025frob" + frob + "permsdelete";
			request = "http://api.flickr.com/services/auth/" +
			"?api_key=6bdb55025361207fd970368b17e2c025" +
			"&perms=delete" +
			"&frob=" + frob + 
			"&api_sig=" + flickrExecute.md5(signature);
			wv.getSettings().setJavaScriptEnabled(true);  
			wv.getSettings().setSupportZoom(true);
			wv.getSettings().setBuiltInZoomControls(true);

			wv.setWebViewClient(new Redirect());
			wv.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress)  
				{
					MyActivity.setTitle("Loading...");
					MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
					// Return the app name after finish loading
					if(progress == 100)
						MyActivity.setTitle(R.string.app_name);
				}
			});
			wv.loadUrl(request);

			Button Done = (Button) findViewById(R.id.okBtn);
			Done.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025frob" + frob + "methodflickr.auth.getToken";
					request = "http://api.flickr.com/services/rest/" +
					"?method=flickr.auth.getToken" +
					"&api_key=6bdb55025361207fd970368b17e2c025" +
					"&frob=" + frob + 
					"&api_sig=" + flickrExecute.md5(signature);
					try {
						response = new flickrExecute(request).execute_get();
						token = response.substring(response.lastIndexOf("<token>")+7, response.lastIndexOf("</token>"));
						String username = response.substring(response.lastIndexOf("username=\"")+10);
						username = username.substring(0,username.indexOf("\""));
						DataBaseHelper myDbHelper = new DataBaseHelper(c);
						((GlobalVariable) getApplication()).setValue(keys.FLICKR_USERNAME, username);
						((GlobalVariable) getApplication()).setValue(keys.FLICKR_TOKEN, token);
						myDbHelper.update("flickr");
						setResult(110);
						finish();
					} catch (Exception e) {
						e.printStackTrace();
						setResult(102);
						finish();
					}
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
			setResult(102);
			this.finish();
		}
	}

	private class Redirect extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			Log.d("URL",url);
			return true;
		}
	}

}