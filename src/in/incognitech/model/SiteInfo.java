package in.incognitech.model;

import java.util.ArrayList;
import in.incognitech.model.HTTPInfo;

public class SiteInfo {

	public SiteInfo() {
		// TODO Auto-generated constructor stub
	}

	ArrayList<HTTPInfo> ds;
	public void addds(HTTPInfo data){
		ds.add(data);
	}
	public ArrayList<HTTPInfo> getSitesArray(){
		return ds;
	}

}
