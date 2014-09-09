package com.example.findme;

import android.provider.Settings.Secure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity implements OnClickListener {
	Button sbmt;
	EditText etName, etEmail; 
	TextView name;
	TextView email;
	Button btnsubmit;
	SharedPreferences checkforreg;
	TelephonyManager    telephonyManager; 

	

	public static final String MyPREFERENCES = "MyPrefs";
	public static final String Name = "nameKey";
	String uid;
	public static final String Email = "emailKey";
	String regUrl = "http://192.168.43.102/xampp/kavish/xyz.php";
			@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		                                             
         
		telephonyManager  =  ( TelephonyManager )getSystemService( Context.TELEPHONY_SERVICE );
		uid = telephonyManager.getDeviceId().toString();
		
		sbmt = (Button) findViewById(R.id.btnSubmit);
		etName = (EditText) findViewById(R.id.editname);
		etEmail = (EditText) findViewById(R.id.editemail);
		sbmt.setOnClickListener(this);
		checkforreg = getSharedPreferences("Myprefs",
				Context.MODE_PRIVATE);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case R.id.btnSubmit:
				
				regUrl = regUrl + "?name=" + etName.getText() + "&email=" + etEmail.getText() + "&uid=" + uid;
				//new Httpsync().execute(regUrl);
				Toast.makeText(this,uid , Toast.LENGTH_LONG).show();
				String x = "kavish";				
				Editor editor = checkforreg.edit();
				editor.putString(Name, uid);
				editor.commit();
				new RequestTask().execute(regUrl);
				Intent i = new Intent(this,MainActivity.class);
				startActivity(i);
				finish();		
			
			
		}
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
	        
	        //Do anything with response..
	    }
	}
		
	}



