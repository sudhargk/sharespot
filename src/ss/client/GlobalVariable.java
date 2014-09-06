package ss.client;

import java.util.ArrayList; 
import java.util.List;

import ss.picasa.picasaImageContent;

import android.app.Application;

public class GlobalVariable extends Application {
	
	public enum keys{YOU_TOKEN,PIC_TOKEN,YOU_USERNAME,YOU_PASS,PICASA_USERNAME,PICASA_PASS,FLICKR_TOKEN,FLICKR_USERNAME};
	private String you_auth = null, pic_auth = null, flickr_auth = null;
	private List<uploadContent> uploadBasket = new ArrayList<uploadContent>(); 
	private String you_user = null, you_pass = null, picasa_user = null, picasa_pass = null;
	private String flickr_user = null;
	private List<picasaImageContent> picasaImageList=new ArrayList<picasaImageContent>();
	private boolean isUploadRunning = false;
	
	public void setValue(keys varname,String value)
	{
		switch(varname)
		{
			case YOU_TOKEN:			you_auth=value;
									break;
			case PIC_TOKEN:			pic_auth=value;
									break;
			case YOU_USERNAME:		you_user=value;
									break;
			case YOU_PASS:			you_pass=value;
		   							break;
			case PICASA_USERNAME:	picasa_user=value;
									break;
			case PICASA_PASS:		picasa_pass=value;
									break;
			case FLICKR_USERNAME:	flickr_user=value;
									break;
			case FLICKR_TOKEN:		flickr_auth=value;
									break;
		}
	}
	
	public String getValue(keys varname)
	{
		switch(varname)
		{
			case YOU_TOKEN:			return you_auth;
			case PIC_TOKEN:			return pic_auth;
			case YOU_USERNAME:		return you_user;
			case YOU_PASS:			return you_pass;
			case PICASA_USERNAME:	return picasa_user;
			case PICASA_PASS:		return picasa_pass;
			case FLICKR_USERNAME:	return flickr_user;
			case FLICKR_TOKEN:		return flickr_auth;
		}
		return null;
	}
	
	public void addToBasket(uploadContent uc)
	{
		uploadBasket.add(uc);
	}
	public void removeFromBasket(int pos)
	{
		uploadBasket.remove(pos);
	}
	public void removeFromBasket()
	{
		uploadBasket.clear();
	}

	public uploadContent getUploadContent(int pos)
	{
		return uploadBasket.get(pos);
	}
	public int getBasketSize()
	{
		return uploadBasket.size();
	}
	public void setImageList(List<picasaImageContent> temp)
	{
		picasaImageList.clear();
		picasaImageList.addAll(temp);
	}
	public picasaImageContent getImageList(int pos)
	{
		return picasaImageList.get(pos);
	}
	public int getImageListSize()
	{
		return picasaImageList.size();
	}
	public boolean IsUpload()
	{
		return isUploadRunning;
	}
	public void setUploadRunning()
	{
		isUploadRunning=true;
	}
	public void uploadDone()
	{
		isUploadRunning=false;
	}
}