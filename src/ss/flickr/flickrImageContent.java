package ss.flickr;

class flickrImageContent {
	String photoTitle;
	String photoID;
	String thmb;
	String photoURL;

	public flickrImageContent(String thumb, String fotoID, String fototitle,String URL) {
		this.thmb=thumb;
		this.photoTitle=fototitle;
		this.photoID=fotoID;
		this.photoURL = URL;
	}
}