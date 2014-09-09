package com.example.findme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class Broadcast_dialog extends Activity {
String Did,Lat,Lon;
TextView Disp;
String regUrl = "http://192.168.43.102/xampp/kavish/lat_lon_update.php";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.homescreen);
	
	Disp=(TextView)findViewById(R.id.tDid);
	Did=MainActivity.uid;	
	Lat = MainActivity.mLatLng.getText().toString();
	Lat = Lat.replaceAll("\\s+","");
	String[] parts = Lat.split(",");
	Lat = parts[0];
	Lon =parts[1];
	regUrl = regUrl + "?lat=" + Lat + "&lon=" + Lon + "&uid=" + Did;
	new RequestTask().execute(regUrl);
	}
	
	class RequestTask extends AsyncTask<String, String, String>{
		
	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(regUrl));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        finish();
	        //Do anything with response..
	    }
	}

}
