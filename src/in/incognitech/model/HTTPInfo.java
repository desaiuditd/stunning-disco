package in.incognitech.model;

public class HTTPInfo {

	int HTTPStatus;
	String link;
	String fileLoc;
	String FName;
	
	String repLocation = "\\repository";
	public HTTPInfo(int conCode, String filename, String surl){
		HTTPStatus = conCode;
		FName = filename;
		link = surl;
		if(!filename.equals("")||!filename.equals(null))
			fileLoc = repLocation+"\\" + FName;
	}
	public String getLink(){
		return link;
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
	
	
}
