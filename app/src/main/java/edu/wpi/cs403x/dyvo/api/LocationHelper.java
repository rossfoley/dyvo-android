package edu.wpi.cs403x.dyvo.api;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by cdhan_000 on 4/26/2015.
 */
public class LocationHelper {

    public LocationManager locationManager;
    private static LocationHelper instance;
    public static LocationHelper getInstance(){
        if (instance == null){
            instance = new LocationHelper();
        }
        return instance;
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1; // 5 seconds

    private LatLng latlng;
    private Location loc;
    private boolean initialized = false;

    private LocationHelper(){
        latlng = new LatLng(0, 0);

    }

    public void initialize(final Context ctx){
        if (!initialized) {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            initialized = true;
        }
    }

    public float getDistanceTo(LatLng latLng){
        float[] answer = new float[]{0};
        Location current = getLocation();
        Location.distanceBetween(current.getLatitude(), current.getLongitude(), latLng.latitude, latLng.longitude, answer);
        return answer[0];
    }

    public String getDistanceAsText(float meters){
        String unit = "meters";

        if (meters > 1000){
            meters /= 1000f;
            unit = "kilometers";
        }

        if (meters == 1){
            unit = unit.substring(0, unit.length()); //chop off one character, IF there is only one thing
        }

        String display = String.format("%.0f %s", meters, unit);
        return display;
    }

    public String getDistanceToAsText(LatLng latLng){
        return getDistanceAsText(getDistanceTo(latLng));
    }

    public Location getLocation() {
        Location location = null;
        double latitude;
        double longitude;
        try {

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
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
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
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
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

}
