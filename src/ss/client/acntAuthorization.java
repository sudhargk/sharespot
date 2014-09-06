package ss.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ss.client.GlobalVariable.keys;
import android.content.Context;

public class acntAuthorization {

	public enum AccountType{PICASA,YOUTUBE};
	static Context c;
	static String email;
	static String pass;

	public acntAuthorization(Context context) {
		acntAuthorization.c = context;
	}

	public  String Authorize(AccountType at) throws Exception {
		String acntType = "";
		switch(at) {
			case PICASA: 
				acntType="lh2"; 
				email=((GlobalVariable)c.getApplicationContext()).getValue(keys.PICASA_USERNAME);
				pass = ((GlobalVariable)c.getApplicationContext()).getValue(keys.PICASA_PASS);
				break;
			case YOUTUBE:
				acntType="youtube";
				email=((GlobalVariable)c.getApplicationContext()).getValue(keys.YOU_USERNAME);
				pass = ((GlobalVariable)c.getApplicationContext()).getValue(keys.YOU_PASS);
				break;
		}
		if(email.equals("null") || pass.equals("null"))
			return "null";
		String AUTH_TOKEN = "";
		//AUTHENTICATION PART (getting authentication token)
		try  {
			pass = SimpleCrypto.decrypt("1a05cb60670ebf7bee2da0e215d0e79b", pass);
			String authUrl = "https://www.google.com/accounts/ClientLogin"
				+ "?accountType=GOOGLE"
				+ "&Email=" + email
				+ "&Passwd=" + pass
				+ "&service=" + acntType
				+ "&source=ShareSpot";

			// create ssl context
			SSLContext sc = SSLContext.getInstance("TLS");

			// override ssl context with our own class, otherwise ssl will fail
			sc.init(null, new TrustManager[] { new MyTrustManager() },new SecureRandom());		

			// create url connection
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
			HttpsURLConnection con = (HttpsURLConnection) new URL(authUrl).openConnection();

			// set timeout and that we do output
			con.setReadTimeout(2000);

			//Timeout(1000);
			con.setDoOutput(true);
			con.connect();		

			// read response from url and accumulate body
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb= new StringBuilder();
			String line1;
			while ((line1 = br.readLine()) != null) {
				sb.append(line1);
			}
			AUTH_TOKEN=sb.toString();
			AUTH_TOKEN=AUTH_TOKEN.substring(AUTH_TOKEN.lastIndexOf("Auth=")+5);
		} catch (final UnknownHostException ea) {
			throw ea;
		}catch(SocketTimeoutException ea){
			throw ea;
		}
		catch(FileNotFoundException ea){
			throw ea;
		}
		catch(Exception e) {
			throw e;
		}
		return AUTH_TOKEN;
	}
}

class MyTrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) {
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}

class MyHostnameVerifier implements HostnameVerifier {
	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}