package com.masum.locationnote;

import android.content.Context;
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
import com.masum.database.NoteTable;
import com.masum.locationlibrary.LocationApplication;
import com.masum.utils.Utility;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private LatLng latLng = null;
    private android.support.v7.widget.Toolbar toolbar;


    public static final String TAG = "note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        boolean status = checkGpsNetworkStatus();
        if (!status) {

        }
        setUpMapIfNeeded();
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Map");
    }

    private boolean checkGpsNetworkStatus() {

        boolean status = isLocationEnabled(this);
        if (!status) {
            Utility.displayWarning(this, "Fatal error", "First enable network and GPS. Then retry again");
            // will close application if no GPS.
            finish();
        }
        return status;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
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
            LocationApplication locationApplication = (LocationApplication) getApplication();
            if (locationApplication.mLocationInfo != null) {
                bundle = new Bundle();
                bundle.putDouble(NoteTable.COLUMN_LATITUDE, locationApplication.mLocationInfo.lastLat);
                bundle.putDouble(NoteTable.COLUMN_LONGITUDE, locationApplication.mLocationInfo.lastLong);
                if (locationApplication.mTotalAddress == null) {
                    locationApplication.mTotalAddress = "debug address";
                }
                bundle.putString(NoteTable.COLUMN_ADDRESS, locationApplication.mTotalAddress);
            }
            Intent intent = new Intent(this, NoteEditor.class);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_list_activity) {
            startActivity(new Intent(this, NoteListActivity.class));
            return true;
        } else if (id == R.id.menu_current_location) {
            LocationApplication application = (LocationApplication) getApplication();
            application.setCurrentLocationInfo();
            application.setCompleteAddress();
            setCurretnLocation();
            return true;
        } else if (id == R.id.menu_camera) {
            Bundle bundle = new Bundle();
            // this start camera for still photo
            bundle.putInt(NoteTable.COLUMN_IMAGE, CameraActivity.MEDIA_TYPE_IMAGE);
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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
        LocationApplication locationApplication = (LocationApplication) getApplication();
        if (!locationApplication.mLocationError.LocationLibraryInitError && locationApplication.mLocationInfo != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(locationApplication.mLocationInfo.lastLat, locationApplication.mLocationInfo.lastLong))
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            if (!locationApplication.mLocationError.AddressError) {
                // show marker with addess info
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(locationApplication.mLocationInfo.lastLat, locationApplication.mLocationInfo.lastLong))
                        .title(locationApplication.mCountry)
                        .snippet(locationApplication.mStreet));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                marker.showInfoWindow();
            } else {
                //Toast.makeText(this, "Current address not found!", Toast.LENGTH_LONG ).show();
                // show only marker without address info
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(locationApplication.mLocationInfo.lastLat, locationApplication.mLocationInfo.lastLong))
                        .title(locationApplication.mCountry)
                        .snippet(locationApplication.mStreet));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                marker.showInfoWindow();
                Log.e(MapsActivity.TAG, "no address found in MapActivity");
            }
        } else {
            Toast.makeText(this, "Current location not found!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "no location found");
        }
    }
}
