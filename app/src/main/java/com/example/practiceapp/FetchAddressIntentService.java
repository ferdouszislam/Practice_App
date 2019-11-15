package com.example.practiceapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver receiver;

    public FetchAddressIntentService() {
        super(FetchAddressIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }
        String errorMessage = "";

        // Get the location and receiver passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        //get the receiver from MainActivity
        receiver = intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;

        try {

            Log.d(MainActivity.LOCATION_TAG, "onHandleIntent: latlong - "+location.getLatitude()+" "+location.getLongitude());

            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // get just a single address.
                    1);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";
            Log.e(MainActivity.LOCATION_TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid lat_long used";
            Log.e(MainActivity.LOCATION_TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found";
                Log.e(MainActivity.LOCATION_TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the UI thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            Log.d(MainActivity.LOCATION_TAG, "address found");

            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(", ", addressFragments));
        }

    }

    private void deliverResultToReceiver(int resultCode, String message) {

        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);

        receiver.send(resultCode, bundle);

    }
}
