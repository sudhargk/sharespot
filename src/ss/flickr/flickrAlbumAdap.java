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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

class flickrAlbumAdap extends ArrayAdapter<flickrAlbumContent> {
	Context context;
	List<flickrAlbumContent> MyList;
	int LayoutResourceID;
	Drawable[] d;
	Boolean cancelFlag;
	GridView albumgv;
	DownloadImagesTask dtask;

	public flickrAlbumAdap(Context context, int LayoutResourceID, List<flickrAlbumContent> MyList,int count, GridView Albums) {
		super(context, LayoutResourceID, MyList);
		this.MyList = MyList;
		this.LayoutResourceID = LayoutResourceID;
		this.context= context;
		this.d = new Drawable[count];
		this.albumgv=Albums;
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

	public View getView(int position, View convertView,	ViewGroup parent) {

		ViewHolder holder;

		if(convertView == null)
		{
			LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView=inflater.inflate(LayoutResourceID, null);

			holder = new ViewHolder();
			holder.thmb = (LoaderImageView) convertView.findViewById(R.id.loaderImageView);
			holder.title = (TextView) convertView.findViewById(R.id.pictitle);
			holder.numPhotos = (TextView) convertView.findViewById(R.id.picnumphotos);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.numPhotos.setText(MyList.get(position).numPhotos + " photos");
		holder.title.setText(MyList.get(position).title);
		if(d[position] != null)
			holder.thmb.setDrawable(d[position]);
		else
			holder.thmb.setDrawable(null);
		return convertView;
	}

	private static class ViewHolder	{
		LoaderImageView thmb;
		TextView title;
		TextView numPhotos;
	}

	class DownloadImagesTask extends AsyncTask<List<flickrAlbumContent>,Integer,Void> {

		@Override
		protected Void doInBackground(List<flickrAlbumContent>... params) {
			Integer cnt=0;
			while(cnt<params[0].size())
			{
				try {
					Log.d("Cnt", "" + cnt);
					if(d[cnt]==null)
					{
						Log.d("Inside", "DrawableNull");
						Log.d("Inside", MyList.get(cnt).AlbumPhotoURL);
						if(MyList.get(cnt).AlbumPhotoURL.equalsIgnoreCase("URL"))
							d[cnt] = getIcon();
						else
						{
							CachePolicy cp = new CachePolicy(context);
							String id = MyList.get(cnt).PrimaryID;
							String URL = MyList.get(cnt).AlbumPhotoURL;
							d[cnt] = cp.image_load(strngs.SITE_FLICKR, strngs.TYPE_ALBUM_THUMB, id, URL, Cache_Options.ALLOW_CACHE);
						}
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
			if(albumgv.getFirstVisiblePosition()<=position[0])
				if(albumgv.getLastVisiblePosition()>=position[0])
				{
					LoaderImageView l = (LoaderImageView) albumgv.getChildAt(position[0]-albumgv.getFirstVisiblePosition()).findViewById(R.id.loaderImageView);
					l.setDrawable(d[position[0]]);
				}
		}
	}

	private Drawable getIcon()
	{
		return context.getResources().getDrawable(R.drawable.icon);
	}

	public void deletedrawable(int longClickPos) {
		int i;
		for(i=longClickPos;i<(d.length-1);i++)
			d[i]=d[i+1];
		d[i]=null;		
	}
}