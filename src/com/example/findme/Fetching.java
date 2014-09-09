package com.example.findme;

import java.io.IOException;
import java.util.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;



public class Fetching extends Activity{
	String Names[],Lats[],Lons[];
	String regUrl = "http://192.168.43.102/xampp/kavish/getUser.php";
	String Name[],Lat[],Lon[];
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setTheme(android.R.style.Theme_Dialog);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.fetching);
		new RequestTask().execute(regUrl);
		Resources res = getResources();
		Names = res.getStringArray(R.array.Names);
		Lats = res.getStringArray(R.array.Lats);
		Lons = res.getStringArray(R.array.Lans);
}
	
class RequestTask extends AsyncTask<String, Void, Document>{
	
		
	    @Override
	    protected Document doInBackground(String... uri) {
	    	 Document doc = null;
	            try {
	                doc = Jsoup.connect(regUrl).userAgent("Mozilla").get();
	            } catch (IOException e) {
	                e.printStackTrace();
	                return doc;
	            }
	            return doc;
	        }

	    @Override
	    protected void onPostExecute(Document doc) {
	        
	        
	        	if(doc!=null){
		        	Elements UserDetails = doc.select("a");
		        	try{
		        	for(int i=0; i<UserDetails.size()-1; i++){
		        		Name[i] = UserDetails.get(i).attr("abs:href");
		        		Lat[i] = UserDetails.get(i).attr("abs:alt");
		        		Lon[i] = UserDetails.get(i).attr("abs:src");
		        	}
		        	Toast.makeText(Fetching.this, "Name : " + Names[1] + "\n Lat : " + Lats[1] + "\n Lon : " + Lons[1], Toast.LENGTH_LONG).show();
		       
	        }catch(Exception e){
	        	Toast.makeText(Fetching.this, e.toString(), Toast.LENGTH_LONG).show();
	        }
	        	 }
	        	
	        
	        //Do anything with response..
	    }
	}

}
