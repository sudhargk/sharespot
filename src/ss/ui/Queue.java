package ss.ui;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ss.client.GlobalVariable;
import ss.client.uploadContent;
import ss.client.GlobalVariable.keys;
import ss.client.uploadContent.DownloadingStatus;
import ss.flickr.flickrExecute;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Queue extends Activity
{
	String DEV_KEY = "AI39si6vLx_IJpUI9be9NzwtBYms49XYZhXC6NTRq4wue-Cp--9keQ3E_uKeJ__bVBNc9oY-iBJOMzyvdXC57AMr3SxH2necDw";
	Context c= this;
	ListView myLV;
	groupAdapter adapter;
	private int SIMPLE_NOTFICATION_ID;
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.uploadbasket);
		c=getApplicationContext();
		myLV = (ListView) findViewById(R.id.uploadBasketList);
		adapter= new groupAdapter();
		myLV.setAdapter(adapter);
	}

	class groupAdapter extends BaseAdapter
	{
		public View getView(final int grpPos, View convertView,	ViewGroup parent)
		{
			uploadContent uc = ((GlobalVariable) getApplication()).getUploadContent(grpPos);
			if(convertView==null)
			{
				if(uc.downloadingStatus==DownloadingStatus.UPLOADING)
				{
					convertView = uc.CurrentView;
				}
				else
				{
					LayoutInflater inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.upld_group, null);
				}
			}
			TextView txtBucket = (TextView) convertView.findViewById(R.id.bcktName);
			txtBucket.setText("Bucket "+(grpPos+1));
			Gallery myLV = (Gallery) convertView.findViewById(R.id.upld_list);
			if(uc.fylPathList.size()>0)
				myLV.setSelection(2);
			Boolean isImage = uc.isPicasa||uc.isFlickr;
			childAdapter cAdap  = new childAdapter(uc,isImage);
			myLV.setAdapter(cAdap);
			cAdap.notifyDataSetChanged();

			TextView txtNumPhotos = (TextView) convertView.findViewById(R.id.numUploads);
			if(uc.isFlickr||uc.isPicasa)
				txtNumPhotos.setText(uc.fylPathList.size()+" Images");
			else
				txtNumPhotos.setText(uc.fylPathList.size()+" Videos");
			final ImageButton close = (ImageButton) convertView.findViewById(R.id.close);
			close.getBackground().setAlpha(0);
			close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((GlobalVariable) getApplication()).removeFromBasket(grpPos);
					notifyDataSetChanged();
				}
			});

			Drawable d = null;
			if(uc.isFlickr&&uc.isPicasa)
				d=c.getResources().getDrawable(R.drawable.picflickr);
			else if(uc.isFlickr)
				d=c.getResources().getDrawable(R.drawable.flickr);
			else if(uc.isPicasa)
				d=c.getResources().getDrawable(R.drawable.picasa);
			else if(uc.isYoutube)
				d=c.getResources().getDrawable(R.drawable.youtube32);
			
			txtNumPhotos.setCompoundDrawables(null, d, null, null);
			final ProgressBar pb =(ProgressBar) convertView.findViewById(R.id.prgrsBar);
			final Button upldButton = (Button) convertView.findViewById(R.id.upld);
			final TextView status= (TextView) convertView.findViewById(R.id.status);
			if(uc.downloadingStatus==DownloadingStatus.UPLOADING)
			{
				upldButton.setEnabled(false);
				upldButton.setText("Uploading..");
				pb.setVisibility(View.VISIBLE);
				close.setEnabled(false);
			}
			else if(uc.downloadingStatus==DownloadingStatus.UPLOADED)
			{
				upldButton.setEnabled(false);
				upldButton.setText("Uploaded");
				pb.setVisibility(View.GONE);
				status.setText("Finished Uploading");
				((GlobalVariable)getApplication()).uploadDone();
				close.setEnabled(true);
			}
			else
			{
				upldButton.setEnabled(true);
				upldButton.setText("Upload");
				pb.setVisibility(View.INVISIBLE);
				close.setEnabled(true);
			}
			upldButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					upldButton.setEnabled(false);
					upldButton.setText("Uploading..");
					pb.setVisibility(View.VISIBLE);
					new uploadTask().execute(grpPos);
					((GlobalVariable)getApplication()).setUploadRunning();
					close.setEnabled(false);
				}
			});
			return convertView;
		}
		@Override
		public int getCount() {

			return ((GlobalVariable) getApplication()).getBasketSize();
		}
		@Override
		public Object getItem(int position) {
			return ((GlobalVariable) getApplication()).getUploadContent(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	class childAdapter extends BaseAdapter
	{
		Boolean isImage;
		uploadContent uc;
		public childAdapter(uploadContent uc,Boolean isImage) {
			this.isImage=isImage;
			this.uc=uc;
		}
		public View getView(int childPos, View convertView,	ViewGroup parent)
		{
			if(convertView==null)
			{
				LayoutInflater inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.upld_gallery,null);
			}

			ImageView thmb=	(ImageView) convertView.findViewById(R.id.fylthmb);
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap curThumb;
			int filePos = uc.fylIndexPos.get(childPos);
			if(isImage){
				curThumb = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),filePos,MediaStore.Images.Thumbnails.MICRO_KIND, options);
			}else
				curThumb = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),filePos,MediaStore.Video.Thumbnails.MICRO_KIND, options);
			thmb.setImageBitmap(curThumb);

			return convertView;
		}
		@Override
		public int getCount() {
			return uc.fylIndexPos.size();
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	private class uploadTask extends AsyncTask<Integer, String, Void>
	{
		boolean flickrflag = true;
		boolean youtubeflag = true;
		boolean picasaflag = true;
		int pos;
		boolean errFlag;
		boolean prgrsFlag;
		uploadContent uc;
		TextView status;
		boolean statusFlag;
		ProgressBar pb;
		String response;
		protected void onPreExecute() {
			errFlag=false;
			prgrsFlag=false;
			statusFlag=false;
		}
		@Override
		protected Void doInBackground(Integer... args) {
			pos=args[0];
			uc =((GlobalVariable) getApplication()).getUploadContent(args[0]);
			int listPos = pos-myLV.getFirstVisiblePosition();
			View v = myLV.getChildAt(listPos);
			uc.CurrentView=v;
			pb=(ProgressBar) v.findViewById(R.id.prgrsBar);
			status = (TextView) v.findViewById(R.id.status);
			uc.downloadingStatus=DownloadingStatus.UPLOADING;
			List<String> files = uc.fylPathList;
			Integer cnt = 0;
			while(cnt<uc.fylPathList.size())
			{	
				if(uc.isFlickr||uc.isPicasa)
				{
					if(uc.isPicasa)
					{
						statusFlag=true;
						publishProgress("Uploading "+(cnt+1)+"/"+uc.fylPathList.size()+" in Picasa");
						picasaUpload(files.get(cnt).substring(files.get(cnt).lastIndexOf("/")+1)
								,files.get(cnt),getType(files.get(cnt)),uc.picAlbumID);
					}			
					if(uc.isFlickr){
						statusFlag=true;
						publishProgress("Uploading "+(cnt+1)+"/"+uc.fylPathList.size()+" in Flickr");					
						flickrUpload(files.get(cnt),files.get(cnt).substring(files.get(cnt).lastIndexOf("/")+1),uc.isFlickrPrivate);
					}	
				}
				else
				{
					statusFlag=true;
					publishProgress("Uploading "+(cnt+1)+"/"+uc.fylPathList.size()+" in Youtube");										
					youtubeUpload(files.get(cnt),files.get(cnt).substring(files.get(cnt).lastIndexOf("/")+1));
				}	
				cnt++;
			}
			statusFlag=true;
			publishProgress("Finished Uploading");
			return null;
		}
		private void youtubeUpload(String filePath,String fileName)
		{
			if (youtubeflag&&errFlag==false) {
				youtubeflag=false;
				String entry = "<?xml version=\"1.0\"?>"
					+ "<entry xmlns=\"http://www.w3.org/2005/Atom\""
					+ " xmlns:media=\"http://search.yahoo.com/mrss/\""
					+ " xmlns:yt=\"http://gdata.youtube.com/schemas/2007\">"
					+ "<media:group>"
					+ "<media:title type=\"plain\">"+fileName+"</media:title>"
					+ "<media:description type=\"plain\">"
					+ "Trying Upload"
					+ "</media:description>"
					+ "<media:category"
					+ " scheme=\"http://gdata.youtube.com/schemas/2007/categories.cat\">People"
					+ "</media:category>"
					+ "<media:keywords>newupload</media:keywords>"
					+ "</media:group>" + "</entry>";
				File file = new File(filePath);
				String boundary = "f93dcbA3";
				String endLine = "\r\n";

				StringBuilder sb = new StringBuilder();
				sb.append("--");
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Type: application/atom+xml; charset=UTF-8");
				sb.append(endLine);
				sb.append(endLine);
				sb.append(entry);
				sb.append(endLine);
				sb.append("--");
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Type: video/mp4");
				sb.append(endLine);
				sb.append("Content-Transfer-Encoding: binary");
				sb.append(endLine);
				sb.append(endLine);

				String bodyStart = sb.toString();

				sb = new StringBuilder();
				sb.append(endLine);
				sb.append("--");
				sb.append(boundary);
				sb.append("--");

				String bodyEnd = sb.toString();

				String AUTH_TOKEN=((GlobalVariable)getApplication()).getValue(keys.YOU_TOKEN);
				HttpURLConnection conn;
				try {
					FileInputStream fIn = new FileInputStream(file);
					conn = (HttpURLConnection) new URL(
					"http://uploads.gdata.youtube.com/feeds/api/users/default/uploads")
					.openConnection();

					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type",
							"multipart/related; boundary=\"" + boundary + "\"");
					conn
					.setRequestProperty("Host",
					"uploads.gdata.youtube.com");
					conn.setRequestProperty("Authorization",
							"GoogleLogin auth=" + AUTH_TOKEN);
					conn.setRequestProperty("GData-Version", "2");
					conn.setRequestProperty("X-GData-Key", "key=" + DEV_KEY);
					conn.setRequestProperty("Slug", fileName);
					conn.setRequestProperty("Content-Length",
							""
							+ (bodyStart.getBytes().length
									+ file.length() + bodyEnd
									.getBytes().length));
					conn.setRequestProperty("Connection", "close");

					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					try {
						conn.connect();

						try {
							OutputStream os = new BufferedOutputStream(conn
									.getOutputStream());
							os.write(bodyStart.getBytes());
							os.flush();
							//os.write(fileBytes);
							//Uploading file Contents in 2048 chunks...

							Integer max=(int) (Math.floor(file.length()/2048)+101);
							pb.setMax(max);
							for(Integer i=0;i<Math.floor(file.length()/2048);i++)
							{
								byte temp[]= new byte[2048];
								fIn.read(temp);
								os.write(temp);
								os.flush();
								prgrsFlag=true;
								publishProgress(i.toString());
							}
							byte temp[] =new byte[(int) (file.length() % 2048)];
							fIn.read(temp);
							os.write(temp);
							os.flush();
							prgrsFlag=true;
							publishProgress(""+(max-100));

							os.write(bodyEnd.getBytes());
							os.flush();

							try {
								response = "Success! "+ (conn.getInputStream().read());
							} catch (Exception e) {
								response = "Youtube Error! " + (conn.getErrorStream().read());
								errFlag=true;
								publishProgress(response);
							}
							Log.d("ID", response);
							prgrsFlag=true;
							publishProgress(""+max);

						} catch (FileNotFoundException e) {
							errFlag=true;
							publishProgress(e.getMessage());
						}
					}catch(OutOfMemoryError e)
					{
						errFlag=true;
						publishProgress("Error!! Out of Memory");	
					}
					catch (IOException e) {
						errFlag=true;
						publishProgress(e.getMessage());							}
				} catch (MalformedURLException e) {
					errFlag=true;
					publishProgress(e.getMessage());
				} catch (IOException e) {
					errFlag=true;
					publishProgress(e.getMessage());
				}
				youtubeflag=true;
			}
		}

		private void flickrUpload(String filePath,String fileName,Boolean isFlickrPrivate)
		{
			if (flickrflag&&errFlag==false) {
				flickrflag=false;
				String token=((GlobalVariable) getApplication()).getValue(keys.FLICKR_TOKEN);
				String signature;
				if(isFlickrPrivate)
					signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "is_public0";
				else
					signature = "ca7116990897272aapi_key6bdb55025361207fd970368b17e2c025auth_token" + token + "is_public1";	
				File file = new File(filePath);
				String boundary = "--7d44e178b0434";
				String endLine = "\r\n";
				StringBuilder sb = new StringBuilder();
				sb.append(endLine);
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Disposition: form-data; name=\"api_key\"");
				sb.append(endLine);
				sb.append(endLine);
				sb.append("6bdb55025361207fd970368b17e2c025");
				sb.append(endLine);
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Disposition: form-data; name=\"auth_token\"");
				sb.append(endLine);
				sb.append(endLine);
				sb.append(token);
				sb.append(endLine);
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Disposition: form-data; name=\"api_sig\"");
				sb.append(endLine);
				sb.append(endLine);
				sb.append(flickrExecute.md5(signature));
				sb.append(endLine);
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Disposition: form-data; name=\"is_public\"");
				sb.append(endLine);
				sb.append(endLine);
				if(isFlickrPrivate)
					sb.append(0);
				else
					sb.append(1);
				sb.append(endLine);
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Disposition: form-data; name=\"photo\"; filename=\""+fileName+"\"");
				sb.append(endLine);
				sb.append("Content-Type: image/jpg");
				sb.append(endLine);
				sb.append(endLine);
				String bodyStart = sb.toString();
				sb = new StringBuilder();
				sb.append(endLine);
				sb.append(boundary);
				sb.append("--");
				String bodyEnd = sb.toString();

				HttpURLConnection conn;
				try {	
					FileInputStream fIn = new FileInputStream(file);
					//byte fileBytes[] = new byte[(int) file.length()];
					//fIn.read(fileBytes);
					conn = (HttpURLConnection) new URL("http://api.flickr.com/services/upload/")
					.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=7d44e178b0434");
					conn.setRequestProperty("Host","api.flickr.com");
					long  len = bodyStart.length() + file.length() + bodyEnd.length();
					conn.setRequestProperty("Content-Length", "" + len); 
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.connect();
					OutputStream os = new BufferedOutputStream(conn.getOutputStream(),1024);

					os.write(bodyStart.getBytes());
					os.flush();
					//os.write(fileBytes);
					Integer max=(int) (Math.floor(file.length()/1024)+41);
					pb.setMax(max);
					for(Integer i=0;i<Math.floor(file.length()/1024);i++)
					{
						byte temp[]= new byte[1024];
						fIn.read(temp);
						os.write(temp);
						os.flush();
						prgrsFlag=true;
						publishProgress(i.toString());
					}

					byte temp[] =new byte[(int) (file.length() % 1024)];
					fIn.read(temp);
					os.write(temp);
					os.flush();								
					prgrsFlag=true;
					publishProgress(""+(max-41));
					os.write(bodyEnd.getBytes());
					os.flush();
					String response = "";
					try {
						int re = conn.getResponseCode();
						response = "" + re + "  ";
						BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						StringBuffer sb1 = new StringBuffer("");
						String line1 = "";
						String NL1 = System.getProperty("line.separator");
						while ((line1 = is.readLine()) != null) {
							sb1.append(line1 + NL1);
						}
						response = response + sb1.toString();
						Log.d("ID", response);
					} catch (Exception e) {
						response = "Flickr Error! " + (conn.getErrorStream().read());
						errFlag=true;
						publishProgress(response);
					}
					prgrsFlag=true;
					publishProgress(""+max);

				}catch(OutOfMemoryError e){
					errFlag=true;
					publishProgress("Error!! Out of Memory");	
				} 
				catch (FileNotFoundException e) {
					errFlag=true;
					publishProgress(e.getMessage());
				} catch (IOException e) {
					errFlag=true;
					publishProgress(e.getMessage());
				} catch (Throwable e) {
					errFlag=true;
					publishProgress(e.getMessage());
				}
				flickrflag=true;
			}
		}


		private void picasaUpload(String fileName,String filePath,String fileType,String AlbumID)
		{
			if (picasaflag&&errFlag==false) {
				picasaflag=false;
				String entry = "<entry xmlns='http://www.w3.org/2005/Atom' > "
					+ "<title>"+fileName+"</title> "
					+ "<summary>NewUpload</summary> "
					+ "<category scheme=\"http://schemas.google.com/g/2005#kind\" "
					+ "term=\"http://schemas.google.com/photos/2007#photo\"/> "
					+ "</entry>";
				File file = new File(filePath);
				String boundary = "END_OF_PART";
				String endLine = "\r\n";
				StringBuilder sb = new StringBuilder();
				sb.append("Media multipart posting");
				sb.append(endLine);
				sb.append("--");
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Type: application/atom+xml"); 
				sb.append(endLine);
				sb.append(endLine);
				sb.append(entry);
				sb.append(endLine);
				sb.append("--");
				sb.append(boundary);
				sb.append(endLine);
				sb.append("Content-Type: image/"+fileType);
				sb.append(endLine);
				sb.append(endLine);
				String bodyStart = sb.toString();
				sb = new StringBuilder();
				sb.append(endLine);
				sb.append("--");
				sb.append(boundary);
				sb.append("--");

				String bodyEnd = sb.toString();

				HttpURLConnection conn;
				try {	
					FileInputStream fIn = new FileInputStream(file);
					//byte fileBytes[] = new byte[(int) file.length()];
					//fIn.read(fileBytes);

					conn = (HttpURLConnection) new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+AlbumID)
					.openConnection();

					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "multipart/related; boundary=\"" + boundary + "\"");
					conn.setRequestProperty("Authorization", "GoogleLogin auth=" + ((GlobalVariable)getApplication()).getValue(keys.PIC_TOKEN));
					conn.setRequestProperty("GData-Version", "2");
					conn.setRequestProperty("MIME-version", "1.0");
					conn.setRequestProperty("Content-Length", "" + (bodyStart.getBytes().length
							+ file.length() + bodyEnd.getBytes().length)); 

					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.connect();
					OutputStream os = new BufferedOutputStream(conn.getOutputStream());
					os.write(bodyStart.getBytes());
					os.flush();
					//os.write(fileBytes);
					Integer max=(int) (Math.floor(file.length()/1024)+41);
					pb.setMax(max);
					for(Integer i=0;i<Math.floor(file.length()/1024);i++)
					{
						byte temp[]= new byte[1024];
						fIn.read(temp);
						os.write(temp);
						os.flush();
						prgrsFlag=true;
						publishProgress(i.toString());
					}
					byte temp[] =new byte[(int) (file.length() % 1024)];
					fIn.read(temp);
					os.write(temp);
					os.flush();
					prgrsFlag=true;
					publishProgress(""+(max-41));


					os.write(bodyEnd.getBytes());
					os.flush();

					try {
						BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						response = "Success! " + is.read();
					} catch (Exception e) {
						response = "Picasa Error! " + (conn.getErrorStream().read());
						errFlag=true;
						publishProgress(response);
					}
					prgrsFlag=true;
					publishProgress(""+max);

				} catch(OutOfMemoryError e){
					errFlag=true;
					publishProgress("Error!! Out of Memory");	
				}catch (FileNotFoundException e) {
					errFlag=true;
					publishProgress(e.getMessage());

				} catch (IOException e) {
					errFlag=true;
					publishProgress(e.getMessage());
				} catch (Exception e) {
					errFlag=true;
					publishProgress(e.getMessage());
				}
				picasaflag=true;
			}

		}
		protected void onProgressUpdate(String... args) {

			if(errFlag==true)
			{	
				this.cancel(true);   
				Toast.makeText(Upload.TabContext, args[0], Toast.LENGTH_LONG).show();
				errFlag=true;
			}
			else if(prgrsFlag==true){
				pb.setProgress(Integer.parseInt(args[0]));
				prgrsFlag=false;
			}
			else if(statusFlag==true){
				status.setText(args[0]);
				statusFlag=false;
			}
		}
		String getType(String filename)
		{
			if(filename.endsWith(".jpg") )
				return "jpeg";
			else if(filename.endsWith(".gif"))
				return"gif";
			else if(filename.endsWith(".png"))
				return"png";
			else if(filename.endsWith(".bmp"))
				return"bmp";
			return "";
		}

		protected void onPostExecute(final Void unused) {
			Log.d("Uploaded","..." );
			if(errFlag==false)
			{
				uploadContent uc =((GlobalVariable) getApplication()).getUploadContent(pos);
				uc.downloadingStatus=DownloadingStatus.UPLOADED;
				adapter.notifyDataSetChanged();
				ImageButton close = (ImageButton) uc.CurrentView.findViewById(R.id.close);
				Button upldButton = (Button) uc.CurrentView.findViewById(R.id.upld);
				upldButton.setEnabled(false);
				upldButton.setText("Uploaded");
				pb.setVisibility(View.GONE);
				close.setEnabled(true);

				NotificationManager nm =(NotificationManager)Upload.TabContext.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.icon,"Uploaded !!!", System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				Intent notificationIntent = new Intent(Upload.TabContext, Queue.class);
				PendingIntent contentIntent =  PendingIntent.getActivity(Upload.TabContext, 0, notificationIntent, 0); 
				notification.setLatestEventInfo(Upload.TabContext, "Uploaded !!!", "Bucket "+(pos+1)+" Uploaded....", contentIntent);
				nm.notify(SIMPLE_NOTFICATION_ID, notification);
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(300);
			}
			else
			{
				uploadContent uc =((GlobalVariable) getApplication()).getUploadContent(pos);
				uc.downloadingStatus=DownloadingStatus.NOT_UPLOADED;
				adapter.notifyDataSetChanged();
			}
		}


	}
}