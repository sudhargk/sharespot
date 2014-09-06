package ss.flickr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
 
public class flickrExecute {

	String request;
	
	public flickrExecute(String req) {
		this.request = req;
	}
	
	public String execute_get() throws Exception
	{
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet httprequest = new HttpGet(request);
			HttpResponse httpresponse;
			httpresponse = client.execute(httprequest);
		
			BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity()
					.getContent()));
	    
			int responseCode = httpresponse.getStatusLine().getStatusCode();
			Log.d("ResCode",String.valueOf(responseCode));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String page = sb.toString();
			Log.d("RES",page);
			if(page.contains("fail"))
				throw new Exception();
			return page;
		} catch (Exception e) {
			throw e;
		} 
	}
	
	String execute_post() throws Exception
	{
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httprequest = new HttpPost(request);
			HttpResponse httpresponse;
			httpresponse = client.execute(httprequest);
					
			BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity()
					.getContent()));
	    
			int responseCode = httpresponse.getStatusLine().getStatusCode();
			Log.d("ResCode",String.valueOf(responseCode));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String page = sb.toString();
			if(page.contains("fail"))
				throw new Exception();
			return page;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static String md5(String text)
    {
        String md5Text="";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            md5Text = new BigInteger(1, digest.digest((text).getBytes())).toString(16);
            while(md5Text.length() < 32 ){
                md5Text = "0"+md5Text;
            }
        } catch (Exception e) {
            System.out.println("Error in call to MD5");
        }
        if (md5Text.length() == 31) {
            md5Text = "0" + md5Text;
        }
        return md5Text;
    }
}