package com.masum.locationlibrary;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.masum.locationnote.MainActivity;
import com.masum.locationnote.MapsActivity;

import java.util.List;
import java.util.Locale;

public class LocationApplication extends Application {

    public static LocationInfo mLocationInfo = null;
    public static String mTotalAddress = null;
    public static String mStreet = null;
    public static String mCountry = null;
    public static LocationError mLocationError;
    @Override
    public void onCreate() {
        super.onCreate();
        mLocationError = new LocationError();
        initializeLocationLibrary();
        mLocationInfo = new LocationInfo(this);
        setCurrentLocationInfo();
        //mLocationInfo.lastLat = (float) 53.473105;
        //mLocationInfo.lastLong = (float) -2.297172;
        setCompleteAddress();
       // Log.i(MapsActivity.TAG, "##### Application called: ");
       // Log.d("TestApplication", "onCreate()");

        // output debug to LogCat, with tag LittleFluffyLocationLibrary
       // LocationLibrary.showDebugOutput(true);

    }

    private void initializeLocationLibrary(){
        try {
            // in most cases the following initialising code using defaults is probably sufficient:
            //
            // LocationLibrary.initialiseLibrary(getBaseContext(), "com.your.package.name");
            //
            // however for the purposes of the test app, we can request unrealistically frequent location broadcasts
            // every 1 minute, and force a location update if there hasn't been one for 2 minutes.
            // LocationLibrary.initialiseLibrary(getBaseContext(), 60 * 10000, 60 * 60 * 1000, "com.masum.locationnote");
            LocationLibrary.initialiseLibrary(getBaseContext(), "com.masum.locationnote");
        }
        catch (UnsupportedOperationException ex) {
            mLocationError.LocationLibraryInitError = true;
            Log.d("LocationApplication", "UnsupportedOperationException thrown - the device doesn't have any location providers");

        }
    }

    public void setCurrentLocationInfo(){
        LocationLibrary.forceLocationUpdate(this);
        if (mLocationInfo.anyLocationDataReceived()) {
            Log.i(MapsActivity.TAG, "setCurrentLocationInfo succeesed: "+mLocationInfo.lastLat);
        }
        else {
            mLocationError.LocationInfoError = true;
            Log.i(MapsActivity.TAG, "setCurrentLocationInfo failed. ");
        }
    }

    public void setCompleteAddress() {
        LocationLibrary.forceLocationUpdate(this);
        //String completeAddress = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(mLocationInfo.lastLat, mLocationInfo.lastLong, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                mStreet = strReturnedAddress.toString();
                mCountry = addresses.get(0).getCountryName();
                mTotalAddress = strReturnedAddress.toString()+" "+addresses.get(0).getCountryName();
                Log.w(MainActivity.TAG, "" + strReturnedAddress.toString());
            } else {
                Log.w(MainActivity.TAG, "No Address returned!");
            }
        } catch (Exception e) {
           // e.printStackTrace();
            mLocationError.AddressError = true;
            Log.e(MapsActivity.TAG, e.getMessage());
            Log.w(MainActivity.TAG, "Canont get Address!");
        }
    }

   public static class LocationError{
       public  static boolean LocationLibraryInitError = false;
       public static boolean LocationInfoError = false;
       public static boolean AddressError = false;
    }
}

