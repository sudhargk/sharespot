package ss.upload;

import java.util.ArrayList; 
import java.util.List;

import ss.client.GlobalVariable;
import ss.client.uploadContent;
import ss.client.GlobalVariable.keys;
import ss.ui.R;
import ss.ui.Upload;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class videosUpload extends Activity {

	public String AUTH_TOKEN = "";
	TextView tv;
	Cursor cc;
	List<Boolean> CheckedList= new ArrayList<Boolean>();
	String DEV_KEY = "AI39si6vLx_IJpUI9be9NzwtBYms49XYZhXC6NTRq4wue-Cp--9keQ3E_uKeJ__bVBNc9oY-iBJOMzyvdXC57AMr3SxH2necDw";
	ArrayList<String> fylList = new ArrayList<String>();
	List<String> fylPathList= new ArrayList<String>();
	List<Integer>fylIndexPos = new ArrayList<Integer>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fyl_layout);

		cc = this.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,null);  
		startManagingCursor(cc);

		for(int pos=0;pos<cc.getCount();pos++)
			CheckedList.add(pos,false);

		String[] columns = new String[] {cc.getColumnName(1)};
		int[] names = new int[] {R.id.fylthmb};
		ListAdapter lAdapter = new ImageCursorAdapter(this, R.layout.fyl_content, cc, columns, names);

		Button addToQueue = (Button) findViewById(R.id.addToQ);
		if(cc.getCount()>0)
			addToQueue.setVisibility(View.VISIBLE);
		else
			addToQueue.setVisibility(View.GONE);
		addToQueue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				for(int pos=0;pos<cc.getCount();pos++)
				{
					if(CheckedList.get(pos).booleanValue())
					{
						cc.moveToPosition(pos);
						fylPathList.add(cc.getString(1));
						fylIndexPos.add(Integer.parseInt(cc.getString(0)));
					}
				}
				if(fylIndexPos.size()==0)
					Toast.makeText(Upload.TabContext,"No Items Selected",Toast.LENGTH_SHORT).show();
				else
				{
					if(((GlobalVariable)getApplication()).getValue(keys.YOU_USERNAME).equals("null"))
						Toast.makeText(Upload.TabContext, "No Video Sharing Account Configured", Toast.LENGTH_SHORT).show();
					else{
						final Dialog img_diag = new Dialog(Upload.TabContext);
						img_diag.setContentView(R.layout.vid_upld_dialog);
						final CheckBox isYoutube = (CheckBox) img_diag.findViewById(R.id.cbVidYou);
						Button btn_OK,btn_Cancel;
						img_diag.setTitle("Add To Queue ( "+ fylIndexPos.size()+" Item )");

						btn_OK = (Button) img_diag.findViewById(R.id.btnOk);
						btn_Cancel = (Button) img_diag.findViewById(R.id.btnCancel);

						btn_OK.setOnClickListener(new OnClickListener(){
							public void onClick(View arg0) {
								if(isYoutube.isChecked())
								{
									uploadContent uc = new uploadContent(fylPathList,fylIndexPos,true);
									((GlobalVariable)getApplication()).addToBasket(uc);
									fylPathList.clear();
									fylIndexPos.clear();
									Toast.makeText(Upload.TabContext, "Added to Queue", Toast.LENGTH_SHORT).show();
									img_diag.dismiss();
								}

								else
									Toast.makeText(Upload.TabContext, "Uploading Site Not Selected", Toast.LENGTH_SHORT).show();
							}
						});
						btn_Cancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								img_diag.dismiss();
								fylPathList.clear();
								fylIndexPos.clear();
							}
						});

						img_diag.show();
					}
				}
			}});
		ListView myLV = (ListView) findViewById(R.id.fylList); 
		myLV.setAdapter(lAdapter);
		myLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long arg3) {
				cc.moveToPosition(pos);
				CheckBox cb =((CheckBox)view.findViewById(R.id.fylCB));
				cb.toggle();
				CheckedList.set(pos, cb.isChecked());
			}
		});

	}

	public class ImageCursorAdapter extends SimpleCursorAdapter {
		private Cursor myCursor;
		private Context context;

		public ImageCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, 0, c, from, to);
			this.myCursor = c;
			this.context = context;
		}

		public View getView(int pos, View inView, ViewGroup parent) {
			View v = inView;
			// Associate the xml file for each row with the view
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.fyl_content, null);
			}
			myCursor.moveToPosition(pos);
			String fylPath=myCursor.getString(1);
			TextView tvName= (TextView)v.findViewById(R.id.fylName);
			tvName.setText(fylPath.substring(fylPath.lastIndexOf("/")+1));

			String fylSize= myCursor.getString(3);
			TextView tvSize= (TextView)v.findViewById(R.id.fylSize);
			tvSize.setText(fylSize+" bytes");

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), Integer.parseInt(cc.getString(0)), MediaStore.Video.Thumbnails.MICRO_KIND, options);
			ImageView imgThmb = (ImageView) v.findViewById(R.id.fylthmb);
			imgThmb.setImageBitmap(curThumb);
			return v;
		}
	}

}
