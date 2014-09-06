package ss.flickr;

class flickrAlbumContent 
{
	String title;
	String AlbumID;
	String numPhotos;
	String AlbumPhotoURL;
	String PrimaryID;
	
	public flickrAlbumContent(String title,String AlbumID,String numPhotos,String thmbURL, String PriID) {
		this.title = title;
		this.AlbumID = AlbumID;
		this.numPhotos= numPhotos;
		this.AlbumPhotoURL=thmbURL;
		this.PrimaryID = PriID;
	}
}