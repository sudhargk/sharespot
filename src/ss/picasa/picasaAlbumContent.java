package ss.picasa;

public class picasaAlbumContent 
{
	
	public String title;
	public String AlbumID;
	String numPhotos;
	String thmbURL;
	String delURL;
	String pblshd;
	String updtd;
	String summary;
	String rights;
	
	public picasaAlbumContent(String title,String AlbumID,String numPhotos,String thmbURL,String del
							,String pblshd,String updtd,String summary,String rights) {
		this.title = title;
		this.AlbumID = AlbumID;
		this.numPhotos= numPhotos;
		this.thmbURL=thmbURL;
		this.delURL = del;
		this.pblshd=pblshd;
		this.updtd=updtd;
		this.summary=summary;
		this.rights=rights;
	}
	public picasaAlbumContent(String title,String AlbumID){
		this.title=title;
		this.AlbumID=AlbumID;
	}
}