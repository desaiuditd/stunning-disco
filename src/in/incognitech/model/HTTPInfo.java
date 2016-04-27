package in.incognitech.model;

import java.io.File;

public class HTTPInfo {

	int HTTPStatus;
	String link;
	String title;
	int outLinksCount;
	int imageCount;
	String fileLoc;
	String FName;
	
	String repLocation = "./repository";
	public HTTPInfo(int conCode, String filename, String surl, String title, int outLinksCount, int imageCount){
		HTTPStatus = conCode;
		FName = filename;
		link = surl;
		this.title = title;
		this.outLinksCount = outLinksCount;
		this.imageCount = imageCount;

		if(!filename.equals(null)) {
			File file = new File(repLocation+"/" + FName);
			if ( file.exists() ) {
				fileLoc = repLocation+"/" + FName;
			} else {
				fileLoc = filename;
			}
		} else {
			fileLoc = "- NA since no filename available. -";
		}
	}
	public String getLink(){
		return link;
	}

	public String getTitle() {
		return title;
	}
	
	public int getHTTPStatus() {
		return HTTPStatus;
	}
	
	public String getFileLoc() {
		return fileLoc;
	}
	
	public String getFName() {
		return FName;
	}

	public int getOutLinksCount() {
		return outLinksCount;
	}

	public int getImageCount() {
		return imageCount;
	}

}
