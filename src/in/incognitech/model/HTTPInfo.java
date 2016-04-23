package in.incognitech.model;

public class HTTPInfo {

	int HTTPStatus;
	String link;
	String fileLoc;
	String FName;
	
	String repLocation = "repository";
	public HTTPInfo(int conCode, String filename, String surl){
		HTTPStatus = conCode;
		FName = filename;
		link = surl;
		fileLoc = repLocation+"\\" + FName;
	}
	public String getLink(){
		return link;
	}

}
