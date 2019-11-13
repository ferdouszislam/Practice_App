package com.example.practiceapp;

/** Unused Class**/

import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


public class AccurateLocationAsync extends AsyncTask <Location,String,String>{

    MainActivity mainActivity;
    FusedLocationProviderClient locationProviderClient;
    /*LocationRequest locationRequest;
    LocationCallback locationCallback;*/

    OnSuccessListener<Location> getLastLocationListener = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            if(location!=null){
                AccurateLocationAsync.accuracy = location.getAccuracy();
                AccurateLocationAsync.latlang = location.getLatitude()+","+location.getLongitude();

                publishProgress("calibarating location... (accuracy at "+AccurateLocationAsync.accuracy+")");

                Log.d("BUGG",
                        "AccurateLocationAsync onSuccess: accuracy at-" +AccurateLocationAsync.accuracy);

            }
        }
    };

    public static double accuracy;
    public static String latlang;

    public AccurateLocationAsync(MainActivity mainActivity/**, FusedLocationProviderClient locationProviderClient,
                                 LocationRequest locationRequest, LocationCallback locationCallback**/)
    {
        this.mainActivity = mainActivity;
        this.locationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        /*this.locationRequest = locationRequest;
        this.locationCallback = locationCallback;*/
    }

    @Override
    protected void onPreExecute() {
        mainActivity.locationbutton.setEnabled(false);
    }

    @Override
    protected String doInBackground(Location... locations) {

        AccurateLocationAsync.accuracy = locations[0].getAccuracy();
        AccurateLocationAsync.latlang = locations[0].getLatitude()+","+locations[0].getLongitude();

        while (AccurateLocationAsync.accuracy>50) {

            Log.d("whileloop", "doInBackground(AccurateLocationAsync): " + AccurateLocationAsync.accuracy);

            locationProviderClient.getLastLocation().
                    addOnSuccessListener(mainActivity, getLastLocationListener);
        }

        return "Location: "+AccurateLocationAsync.latlang+"("+(int)accuracy+")";
    }

    @Override
    protected void onProgressUpdate(String... progress){
        mainActivity.locationtext.setText(progress[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        mainActivity.locationtext.setText(s);
        mainActivity.locationbutton.setEnabled(true);

        MainActivity.locationClient.removeLocationUpdates(MainActivity.locationCallback);
        MainActivity.locationRequested = false;

    }
}
