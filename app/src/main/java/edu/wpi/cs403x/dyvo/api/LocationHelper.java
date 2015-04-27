package edu.wpi.cs403x.dyvo.api;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by cdhan_000 on 4/26/2015.
 */
public class LocationHelper {


    private static LocationHelper instance;
    public static LocationHelper getInstance(){
        if (instance == null){
            instance = new LocationHelper();
        }
        return instance;
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1; // 5 seconds

    private LatLng latlng;
    private Location loc;

    private LocationHelper(){
        latlng = new LatLng(0, 0);

    }

    public void initialize(final Context ctx){
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        criteria.setAccuracy(1);

        String bestProvider = locationManager.getBestProvider(criteria, true);

        locationManager.requestLocationUpdates(
                bestProvider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener(){
                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            latlng = new LatLng(location.getLatitude(), location.getLongitude());
                            loc = location;
                        }
                    }
                });


        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            latlng = new LatLng(location.getLatitude(), location.getLongitude());
            loc = location;
        }
    }

    public LatLng getCurrentLatLong(){
        return latlng;
    }

    public double getCurrentLat(){
        return latlng.latitude;
    }

    public double getCurrentLong(){
        return latlng.longitude;
    }

    public Location getCurrentLocation(){
        return loc;
    }

}
