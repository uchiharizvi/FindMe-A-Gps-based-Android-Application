package com.example.findme;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	public static TextView mLatLng, LatLng;
	private TextView mAddress;
	private TextView mLastUp;

	private LocationManager mLocationMgr;
	private Handler mHandler;

	private Location mLastLocation;
	private boolean mGeocoderAvailable;
	private static final int UPDATE_LASTLATLNG = 4;
	private static final int LAST_UP = 3;
	private static final int UPDATE_LATLNG = 2;
	private static final int UPDATE_ADDRESS = 1;

	private static final int SECONDS_TO_UP = 10000;
	private static final int METERS_TO_UP = 10;
	private static final int MINUTES_TO_STALE = 1000 * 60 * 2;
	private GoogleMap mMap;
	private static LatLng loc2,loc3;
	SharedPreferences sharedpreferences;
	String regUrl = "http://192.168.43.102/xampp/kavish/lat_lon_update.php";
	TelephonyManager    telephonyManager;
	public static String uid;
	String[] Names,Lats,Lons;
	String name1,name2,name3;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map1))
				.getMap();
		Resources res = getResources();
		Names = res.getStringArray(R.array.Names);
		Lats = res.getStringArray(R.array.Lats);
		Lons = res.getStringArray(R.array.Lans);
		name1=Names[0];
		name2=Names[1];
		name3=Names[2];
		
		loc2 = new LatLng(Double.parseDouble(Lats[1]), Double.parseDouble(Lons[1]));
		loc3 = new LatLng(Double.parseDouble(Lats[2]), Double.parseDouble(Lons[2]));
		telephonyManager  =  ( TelephonyManager )getSystemService( Context.TELEPHONY_SERVICE );
		uid = telephonyManager.getDeviceId().toString();
		regUrl = regUrl + "?uid=" + uid;
		Toast.makeText(this,"Welcome"+" "+name1+"\n user id :"+uid, Toast.LENGTH_LONG).show();
		
		mMap.setMyLocationEnabled(true);
		mLatLng = (TextView) findViewById(R.id.latlng);
		mLastUp = (TextView) findViewById(R.id.lastup);
		mAddress = (TextView) findViewById(R.id.address);
		LatLng = (TextView) findViewById(R.id.label_latlng);
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_ADDRESS:
					mAddress.setText((String) msg.obj);
					break;
				case UPDATE_LATLNG:
					mLatLng.setText((String) msg.obj);
					break;
				case LAST_UP:
					mLastUp.setText((String) msg.obj);
					break;
				}
				
			}
		};

		mGeocoderAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent();
		mLocationMgr = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		sharedpreferences = getSharedPreferences("Myprefs",
				Context.MODE_PRIVATE);
		if (!sharedpreferences.contains("nameKey")) {
			Intent i = new Intent(this, Register.class);
			startActivity(i);
			finish();
		} else {
			
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		setup();
	}

	public void onStart() {
		super.onStart();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!gpsEnabled) {
			new EnableGpsDialogFragment().show(getSupportFragmentManager(),
					"enableGpsDialog");
		}
	}

	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationMgr.removeUpdates(listener);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void setup() {
		Location newLocation = null;
		mLocationMgr.removeUpdates(listener);
		mLatLng.setText(R.string.unknown);
		newLocation = requestUpdatesFromProvider(LocationManager.GPS_PROVIDER,
				R.string.no_gps_support);

		// If gps location doesn't work, try network location
		if (newLocation == null) {
			newLocation = requestUpdatesFromProvider(
					LocationManager.NETWORK_PROVIDER,
					R.string.no_network_support);
		}

		if (newLocation != null) {
			updateUILocation(getBestLocation(newLocation, mLastLocation));
		}
	}

	/**
	 * This code is based on this code:
	 * http://developer.android.com/guide/topics
	 * /location/obtaining-user-location.html
	 * 
	 * @param newLocation
	 * @param currentBestLocation
	 * @return
	 */
	protected Location getBestLocation(Location newLocation,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			return newLocation;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isNewerThanStale = timeDelta > MINUTES_TO_STALE;
		boolean isOlderThanStale = timeDelta < -MINUTES_TO_STALE;
		boolean isNewer = timeDelta > 0;

		if (isNewerThanStale) {
			return newLocation;
		} else if (isOlderThanStale) {
			return currentBestLocation;
		}

		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return newLocation;
		} else if (isNewer && !isLessAccurate) {
			return newLocation;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return newLocation;
		}
		return currentBestLocation;
	}

	private Location requestUpdatesFromProvider(final String provider,
			final int errorResId) {
		Location location = null;
		if (mLocationMgr.isProviderEnabled(provider)) {
			mLocationMgr.requestLocationUpdates(provider, SECONDS_TO_UP,
					METERS_TO_UP, listener);
			location = mLocationMgr.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}

	private void doReverseGeocoding(Location location) {
		(new ReverseGeocode(this)).execute(new Location[] { location });
	}

	private void updateUILocation(Location location) {
		Message.obtain(mHandler, UPDATE_LATLNG,
				location.getLatitude() + ", " + location.getLongitude())
				.sendToTarget();
		if (mLastLocation != null) {
			Message.obtain(
					mHandler,
					UPDATE_LASTLATLNG,
					mLastLocation.getLatitude() + ", "
							+ mLastLocation.getLongitude()).sendToTarget();
		}
		mLastLocation = location;
		Date now = new Date();
		Message.obtain(mHandler, LAST_UP, now.toString()).sendToTarget();

		if (mGeocoderAvailable)
			doReverseGeocoding(location);
	}

	private final LocationListener listener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			updateUILocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	private class ReverseGeocode extends AsyncTask<Location, Void, Void> {
		Context mContext;

		public ReverseGeocode(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

			Location loc = params[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				// Update address field with the exception.
				Message.obtain(mHandler, UPDATE_ADDRESS, e.toString())
						.sendToTarget();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
				// Update address field on UI.
				Message.obtain(mHandler, UPDATE_ADDRESS, addressText)
						.sendToTarget();
			}
			return null;
		}
	}

	/**
	 * Dialog to prompt users to enable GPS on the device.
	 */

	public class EnableGpsDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.enable_gps)
					.setMessage(R.string.enable_gps_dialog)
					.setPositiveButton(R.string.enable_gps,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									enableLocationSettings();
								}
							}).create();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_sethybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;

		case R.id.menu_setnormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.menu_showtraffic:
			mMap.setTrafficEnabled(true);
			break;
		

		

		case R.id.menu_showcurrentlocation:
			Location myLocation = mMap.getMyLocation();
			LatLng myLatLng = new LatLng(myLocation.getLatitude(),
					myLocation.getLongitude());

			CameraPosition myPosition = new CameraPosition.Builder()
					.target(myLatLng).zoom(17).bearing(90).tilt(30).build();
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(myPosition));
			break;

		case R.id.findPlaces:
			Intent l = new Intent(MainActivity.this,Home.class);
			startActivity(l);
			finish();
		case R.id.findppl:
			Intent k = new Intent(MainActivity.this,Fetching.class);
			startActivity(k);
			
			Marker two=mMap.addMarker(new MarkerOptions()
	        .position(loc2)
	         .snippet("Latitude is"+Lats[1]+"\n"+"Longitude is"+Lons[1])
	        .title(name2)
	        .flat(true));
			Marker three=mMap.addMarker(new MarkerOptions()
	        .position(loc3)
	        .snippet("Latitude is"+Lats[2]+"\n"+"Longitude is"+Lons[2]+"\n"+"Adress :")
	        .title(name3)
	        .flat(true));
			break;
		case R.id.menu_broadcast:
				Intent j = new Intent(MainActivity.this,Broadcast_dialog.class);
				startActivity(j);
			break;
		case R.id.exit:
			finish();
			break;
		}
		return true;
		
	}
	
	
}
