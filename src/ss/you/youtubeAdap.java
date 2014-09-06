package ss.you;

import java.io.IOException; 
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ss.ui.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

class youtubeAdap extends ArrayAdapter<youtubeContent>
{
	Context context;
	List<youtubeContent> MyList;
	int LayoutResourceID;
	ListView thmbLV;
	Drawable[] d;
	DownloadImagesTask dtask;
	Boolean cancelFlag;
	
	private void setdrawable() {
		for (@SuppressWarnings("unused") Drawable d1 : d) {
			d1=null;
		}
	}
	public void deletedrawable(int position)
	{ 
		int i;
		for(i=position;i<d.length;i++)
			d[i]=d[i+1];
		d[i]=null;
	}
	public youtubeAdap(Context context, int LayoutResourceID, List<youtubeContent> MyList,int count,ListView thmbLV) {
		super(context, LayoutResourceID, MyList);
		this.thmbLV=thmbLV;
		this.MyList = MyList;
		this.LayoutResourceID = LayoutResourceID;
		this.context= context;
		d = new Drawable[count];
		setdrawable();
		resumeAsyncTask();
	}
	public void cancelAsyncTask()
	{
		cancelFlag=true;
	}
	
	@SuppressWarnings("unchecked")
	public void resumeAsyncTask()
	{
		dtask= new DownloadImagesTask();
		cancelFlag=false;
		dtask.execute(MyList);
	}
	public View getView(int position, View convertView,	ViewGroup parent)
	{
		ViewHolder holder;
		
		if(convertView==null)
		{
			LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(LayoutResourceID, null);
			
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title1);
			holder.duration = (TextView) convertView.findViewById(R.id.length1);
			holder.rt = (RatingBar) convertView.findViewById(R.id.rate);
			holder.thmb = (ImageView) convertView.findViewById(R.id.thmb1);
			holder.view = (TextView) convertView.findViewById(R.id.viewed);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.title.setText(MyList.get(position).title);
		
		holder.view.setText(MyList.get(position).noOfViews + "views");
			
		Integer sec =Integer.parseInt(MyList.get(position).duration);
		Integer min = (Integer)sec/60;
		sec = sec%60;
		String second;
		if(sec<10)
			second="0"+sec.toString();
		else
			second=sec.toString();
		holder.duration.setText(min+ ":" + second);
		holder.rt.setRating(Float.parseFloat(MyList.get(position).rating));
		//holder.thmb.setTag(new posURL(position,MyList.get(position).thumbURL));
		if(d[position]!=null)
			holder.thmb.setImageDrawable(d[position]);
		else
			holder.thmb.setImageDrawable(null);
		//new DownloadImagesTask().execute(holder.thmb);
		return convertView;
	}
	
	static class ViewHolder
	{
		TextView title;
		TextView duration;
		RatingBar rt;
		TextView view;
		ImageView thmb;
	}
	public class DownloadImagesTask extends AsyncTask<List<youtubeContent>,Integer,Void> {
		protected Void doInBackground(List<youtubeContent>... params) {
			Integer cnt=0;
			InputStream is;
			while(cnt<params[0].size())
			{
				try {
					if(d[cnt]==null){
					is = (InputStream) new URL(MyList.get(cnt).thumbURL).getContent();
					d[cnt] = Drawable.createFromStream(is, "src name");
					publishProgress(cnt);}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(cancelFlag==true)
					break;
				cnt++;
			}
			return null;
		}
		
		protected void onProgressUpdate(Integer... position) {
			if(thmbLV.getFirstVisiblePosition()<=position[0])
				if(thmbLV.getLastVisiblePosition()>=position[0])
				{
					ImageView l=(ImageView) thmbLV.getChildAt(position[0]-thmbLV.getFirstVisiblePosition()).findViewById(R.id.thmb1);
					l.setImageDrawable(d[position[0]]);
				}
			}
		}
//	public class DownloadImagesTask extends AsyncTask<ImageView, Void,Drawable> {
//
//		ImageView imageView = null;
//			   
//		@Override
//		protected Drawable doInBackground(ImageView... imageViews) {
//			this.imageView = imageViews[0];
//			return download_Image((posURL)imageView.getTag());
//		}
//
//		@Override
//		protected void onPostExecute(Drawable result) {
//		    imageView.setImageDrawable(result);
//		}
//
//
//		private Drawable download_Image(posURL obj) {
//			if(d[obj.pos]==null)
//			{
//				InputStream is;
//				try {
//					is = (InputStream) new URL(MyList.get(obj.pos).thumbURL).getContent();
//					d[obj.pos] = Drawable.createFromStream(is, "src name");
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			return d[obj.pos];
//		}
//
//	}
}