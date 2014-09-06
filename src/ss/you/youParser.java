package ss.you;

import java.io.ByteArrayInputStream; 
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class youParser {
	public Integer Result;
	List<youtubeContent> feedList = null;
	String feed;
	youParser(String feed)
	{
		this.feed=feed;
		feedList=new ArrayList<youtubeContent>();
	}
	
	List<youtubeContent> parse()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(feed.getBytes("UTF-8"));
			Document dom = db.parse(in);      
			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getElementsByTagName("entry"); 
			Result=nl.getLength();
			int cnt = 0;
			if (nl != null && nl.getLength() > 0) {
        	 while(cnt<nl.getLength()) {
        		 Element entry = (Element)nl.item(cnt);
        		 Element title = (Element)entry.getElementsByTagName("title").item(0);
        		 Element mgrp = (Element)entry.getElementsByTagName("media:group").item(0);
        		 Element mthmb = (Element)entry.getElementsByTagName("media:thumbnail").item(0);
        		 Element len =(Element)mgrp.getElementsByTagName("yt:duration").item(0);
        		 Element rating = (Element)entry.getElementsByTagName("gd:rating").item(0);
        		 Element views = (Element) entry.getElementsByTagName("yt:statistics").item(0);
        		 Element url = (Element) mgrp.getElementsByTagName("media:content").item(0);
             	 Element vId = (Element) entry.getElementsByTagName("id").item(0);
				 Element pblshd = (Element)entry.getElementsByTagName("published").item(0);
				 Element updtd = (Element)entry.getElementsByTagName("updated").item(0);
				 Element delURL = (Element) entry.getElementsByTagName("link").item(1);

             	 //capturing title duration and thmburl
             	 String strPblshd,delhref,strUpdtd,strTitle,strDur,strThmb,strRating = "0",strUrl,strViews,strVID;
             	 if(title.getFirstChild()!=null)
             		 strTitle = title.getFirstChild().getNodeValue();
             	 else
             		 strTitle="";
             	 if(len!=null)
             		 strDur = len.getAttribute("seconds");     
             	 else
             		 strDur="";
             	 
             	 if(pblshd.getFirstChild()!=null)
             	 	strPblshd=pblshd.getFirstChild().getNodeValue();
             	 else
             		 strPblshd="";

             	 if(updtd.getFirstChild()!=null)
             		 strUpdtd=updtd.getFirstChild().getNodeValue();
             	 else
             		 strUpdtd="";
             	 
             	 if(mthmb!=null)
             		 strThmb = mthmb.getAttribute("url").toString();
             	 else
             		 strThmb="";
             	 if(delURL!=null) 
             		 delhref = delURL.getAttribute("href");
             	 else
             		 delhref="";
             	 if(rating!=null)
             		 strRating = rating.getAttribute("average");
             	 else
             		 strRating="0";
             	 
             	 if(views!=null)
             		strViews = views.getAttribute("viewCount").toString();
            	 else
            		 strViews="0";
             	 
             	if(url!=null)
             		strUrl = url.getAttribute("url").toString();
             	else
             		strUrl=null;
             	
             	if(vId.getFirstChild()!=null){
             	 strVID = vId.getFirstChild().getNodeValue();
             	 strVID = strVID.substring(strVID.lastIndexOf("/")+1);
             	}else
             		strVID="";
             	 youtubeContent tempContent = new youtubeContent(strTitle, strDur ,strRating, strThmb, strUrl, strViews, strVID,delhref,strPblshd,
             			 strUpdtd);
             	 feedList.add(tempContent);
             	 cnt++;
        	 }
         }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return feedList;
	}

}
