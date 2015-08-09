package com.masum.locationnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.masum.locationlibrary.LocationApplication;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private   Marker marker;
    private android.support.v7.widget.Toolbar toolbar;
   // public LocationInfo locationInfo;
  //  private String currentAddress;
    public static final String TAG = "note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        boolean status = checkGpsNetworkStatus();
        if(!status){
            setUpMapIfNeeded();
        }
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Map");
    }

    private boolean checkGpsNetworkStatus(){

        boolean status = isLocationEnabled(this);
        if(!status){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Fatal error");
            alertDialog.setMessage("First enbale network and GPS. Then retry again");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }
      return status;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_new_note) {
            Bundle bundle = null;
            if( LocationApplication.mLocationInfo != null) {
                bundle = new Bundle();
                bundle.putFloat("latitude", LocationApplication.mLocationInfo.lastLat);
                bundle.putFloat("longitude", LocationApplication.mLocationInfo.lastLong);
                if(LocationApplication.mTotalAddress == null){
                    LocationApplication.mTotalAddress = "debug address";
                }
                bundle.putString("address", LocationApplication.mTotalAddress);
            }
            Intent intent = new Intent(this, NoteEditor.class);
            if(bundle != null){
                intent.putExtras(bundle);
            }
            startActivity(intent);
            return true;
        }

        if (id == R.id.menu_list_activity) {
            startActivity(new Intent(this, NoteListActivity.class));
            return true;
        }

        if (id == R.id.menu_current_location) {
            LocationApplication application = (LocationApplication) getApplication();
            application.setCurrentLocationInfo();
            application.setCompleteAddress();
            setCurretnLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setCurretnLocation();
            }
        }
    }

    private void setCurretnLocation() {

        if ( !LocationApplication.mLocationError.LocationLibraryInitError && LocationApplication.mLocationInfo != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(LocationApplication.mLocationInfo.lastLat, LocationApplication.mLocationInfo.lastLong))
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            if(!LocationApplication.mLocationError.AddressError){
                // show marker with addess info
                marker =  mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(LocationApplication.mLocationInfo.lastLat, LocationApplication.mLocationInfo.lastLong))
                        .title(LocationApplication.mCountry)
                        .snippet(LocationApplication.mStreet));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                marker.showInfoWindow();
            }else{
                //Toast.makeText(this, "Current address not found!", Toast.LENGTH_LONG ).show();
                // show only marker without address info
                marker =  mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(LocationApplication.mLocationInfo.lastLat, LocationApplication.mLocationInfo.lastLong))
                        .title(LocationApplication.mCountry)
                        .snippet(LocationApplication.mStreet));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                marker.showInfoWindow();
                Log.e(MapsActivity.TAG, "no address found in MapActivity");
            }
        }else{
            Toast.makeText(this, "Current location not found!", Toast.LENGTH_LONG ).show();
            Log.e(TAG, "no location found");
        }
    }
}
