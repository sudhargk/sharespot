package ss.client;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

public class uploadContent {

	public String picAlbumID;
	public String picAlbumName;
	public List<String> fylPathList=new ArrayList<String>();
	public List<Integer>fylIndexPos=new ArrayList<Integer>();
	public Boolean isPicasa;
	public Boolean isYoutube;
	public Boolean isFlickr;
	public Boolean isFlickrPrivate;
	public enum DownloadingStatus {NOT_UPLOADED,UPLOADED,UPLOADING};
	public DownloadingStatus downloadingStatus;
	public View CurrentView;
	public uploadContent(String picAlbumName, String picAlbumID,List<String> fylPathList,List<Integer> fylIndexPos,Boolean isPicasa,Boolean isFLickr, Boolean isFlickrPrivate) {
		this.picAlbumID = picAlbumID;
		this.picAlbumName = picAlbumName;
		this.fylPathList.addAll(fylPathList);
		this.fylIndexPos.addAll(fylIndexPos);
		this.isPicasa = isPicasa;
		this.isFlickr = isFLickr;
		this.isFlickrPrivate=isFlickrPrivate;
		this.isYoutube = false;
		this.downloadingStatus=DownloadingStatus.NOT_UPLOADED;
		this.CurrentView=null;
	}
	
	public uploadContent(List<String> fylPathList,List<Integer> fylIndexPos,Boolean isYoutube) {
		this.fylPathList.addAll(fylPathList);
		this.fylIndexPos.addAll(fylIndexPos);
		this.isFlickr = false;
		this.isPicasa = false;
		this.isYoutube = isYoutube;
		this.downloadingStatus=DownloadingStatus.NOT_UPLOADED;
		this.CurrentView=null;
		
	}
}