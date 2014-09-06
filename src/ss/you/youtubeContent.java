package ss.you;

class youtubeContent 
{
	String title;
	String thumbURL;
	String duration;
	String rating;
	String VidUrl;
	String noOfViews;
	String videoID;
	String delURL;
	String pblshd;
	String uptd;
	
	public youtubeContent(String title,String duration,String rating, String thumbURL,String vidurl, String views, String vid,
					String delURL,String pblshd,String uptd) {
		this.title = title;
		this.thumbURL = thumbURL;
		this.duration = duration;
		this.rating = rating;
		this.VidUrl = vidurl;
		this.noOfViews = views;
		this.videoID = vid;
		this.delURL=delURL;
		this.pblshd=pblshd;
		this.uptd=uptd;
	}
}