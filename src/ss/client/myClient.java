package ss.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;


public class myClient {
 
    public enum RequestMethod{GET,POST};
    private int responseCode;
    private String message;
 
    private String response;
 
    public String getResponse() {
        return response;
    }
 
    public String getErrorMessage() {
        return message;
    }
 
    public int getResponseCode() {
        return responseCode;
    }
 
    public myClient()
    {
       
    }

    public void executeRequest(HttpUriRequest request) throws UnknownHostException,SocketTimeoutException,IOException
    {
//      HttpParams httpParameters = new BasicHttpParams();
//      int timeoutConnection = 5000;
//      HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//      int timeoutSocket = 10000;
//      HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
//    	HttpClient client = new DefaultHttpClient(httpParameters);
    	HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        try {
        	httpResponse = client.execute(request);
            
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null) {
 
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);
                // Closing the input stream will trigger connection release
                instream.close();
            }
 
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
    }
    
    private static String convertStreamToString(InputStream is) throws IOException  {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}