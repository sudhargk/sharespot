package ss.flickr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import ss.cache.CachePolicy;
import ss.cache.CachePolicy.Cache_Options;
import ss.cache.CachePolicy.strngs;
import ss.client.LoaderImageView;
import ss.ui.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

class flickrImageAdap extends ArrayAdapter<flickrImageContent> {

	Context context;
	List<flickrImageContent> MyList;
	int LayoutResourceID;
	Drawable[] d;
	ImageView thmb;
	GridView photoGV;
	DownloadImagesTask dtask;
	Boolean cancelFlag;
	Cache_Options cop;

	public flickrImageAdap(Context context, int LayoutResourceID,List<flickrImageContent> MyList,int count,GridView PhotoGrid,Cache_Options cop) {
		super(context, LayoutResourceID, MyList);
		this.MyList = MyList;
		this.LayoutResourceID = LayoutResourceID;
		this.context= context;
		this.d = new Drawable[count];
		this.photoGV=PhotoGrid;
		this.cop = cop;
		setdrawable();
		resumeAsyncTask();
	}

	public void cancelAsyncTask() {
		cancelFlag=true;
	}

	@SuppressWarnings("unchecked")
	public void resumeAsyncTask() {
		dtask = new DownloadImagesTask();
		cancelFlag=false;
		dtask.execute(MyList);
	}

	private void setdrawable() {
		for (@SuppressWarnings("unused") Drawable d1 : d) {
			d1=null;
		}
	}

	public void deletedrawable(int position) { 
		int i;
		for(i=position;i<(d.length-1);i++)
			d[i]=d[i+1];
		d[i]=null;
	}

	public View getView(int position, View convertView,	ViewGroup parent) {

		ViewHolder holder;
		if(convertView == null)
		{
			LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView=inflater.inflate(LayoutResourceID, null);
			holder = new ViewHolder();
			holder.lv = (LoaderImageView) convertView.findViewById(R.id.loaderImageView);
			holder.title = (TextView) convertView.findViewById(R.id.pictitle);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.title.setText(MyList.get(position).photoTitle);
		if(d[position]!=null)
			holder.lv.setDrawable(d[position]);
		else
			holder.lv.setDrawable(null);
		return convertView;
	}

	private static class ViewHolder
	{
		LoaderImageView lv;
		TextView title;
	}

	public class DownloadImagesTask extends AsyncTask<List<flickrImageContent>,Integer,Void> {
		protected Void doInBackground(List<flickrImageContent>... params) {
			Integer cnt=0;
			while(cnt<params[0].size())
			{
				try {
					if(d[cnt]==null){
						CachePolicy cp = new CachePolicy(context);
						String id =MyList.get(cnt).photoID;
						String URL= MyList.get(cnt).thmb;
						d[cnt]=cp.image_load(strngs.SITE_FLICKR, strngs.TYPE_IMAGE_THUMB, id, URL, Cache_Options.ALLOW_CACHE);
						publishProgress(cnt);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(cancelFlag==true)
					break;
				cnt++;
			}
			return null;
		}

		protected void onProgressUpdate(Integer... position) {
			if(photoGV.getFirstVisiblePosition()<=position[0])
				if(photoGV.getLastVisiblePosition()>=position[0])
				{
					LoaderImageView l=(LoaderImageView) photoGV.getChildAt(position[0]-photoGV.getFirstVisiblePosition()).findViewById(R.id.loaderImageView);
					l.setDrawable(d[position[0]]);
				}
		}
	}
}