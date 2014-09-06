package ss.picasa;

public class picasaImageContent {
	String albumID;
	String photoTitle;
	String photoID;
	String thmb;
	String URL;
	String size;
	String height;
	String width;
	String summary;
	String delURL;
	String userID;
	
	public picasaImageContent(String userID, String albumID, String thumb, String fotoID, String fototitle, String imgURL, String size, String height, String width, String summary, String delurl) {
		this.albumID=albumID;
		this.userID=userID;
		this.thmb=thumb;
		this.photoTitle=fototitle;
		this.photoID=fotoID;
		this.URL=imgURL;
		this.size=size;
		this.height=height;
		this.width=width;
		this.summary=summary;
		this.delURL=delurl;

	}
}