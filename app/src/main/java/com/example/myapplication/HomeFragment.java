package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private LocalStorageHelper localStorageHelper;
    boolean alertShown = false; //flag to show alert when error is active
    final double EAST_LANSING_MIN_LAT = 42.681321; // boundaries for designated area
    final double EAST_LANSING_MAX_LAT = 42.783423;
    final double EAST_LANSING_MIN_LNG = -84.445573;
    final double EAST_LANSING_MAX_LNG = -84.516725;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()); //initalize the client
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        localStorageHelper = new LocalStorageHelper(getContext());

        Button button = view.findViewById(R.id.button); // button to go to next page
        button.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.navigation_dashboard);
        });


        checkLocation(); // updates location and checks permissions
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // checks to see if location is disabled or not
            new AlertDialog.Builder(getContext())
                    .setTitle("GPS Disabled")
                    .setMessage("Please enable GPS!")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void storeLocation(double latitude,double longitude) {
        localStorageHelper.saveData("lastLatitude", String.valueOf(latitude));
        localStorageHelper.saveData("lastLongitude", String.valueOf(longitude));
    }

    private void checkLocation() {
        //request for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        //configure location settings
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);  // Update error
        locationRequest.setFastestInterval(500);  // Fastest update every half-second
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //check device location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        locationCallback = new LocationCallback() { //defines the callback
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) { // returns when location is null
                    return;
                }
                for (Location location : locationResult.getLocations()) { //for loop to cycle through location updates
                    if (location != null) {//makes sure there is a location
                        double latitude = location.getLatitude();//get latitude
                        double longitude = location.getLongitude();//get longitude
                        storeLocation(latitude, longitude);
                        //Toast.makeText(getContext(), "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_LONG).show();
                        Log.d("LocationDebug", "Latitude: " + latitude + ", Longitude: " + longitude);
                        boolean longneg = longitude < 0; // checks to see if longitude is negative to make sure that were working with negative longitude for east lansing
                        boolean latCondition = latitude >= EAST_LANSING_MIN_LAT && latitude <= EAST_LANSING_MAX_LAT; // check lat bounds

                        // Use absolute values for longitude comparison
                        boolean lngConditionPart1 = Math.abs(longitude) >= Math.abs(EAST_LANSING_MIN_LNG);
                        boolean lngConditionPart2 = Math.abs(longitude) <= Math.abs(EAST_LANSING_MAX_LNG);
                        boolean lngCondition = lngConditionPart1 && lngConditionPart2; // check long bounds

                        // Debug logs
                        Log.d("Debug", "Lat Condition: " + latCondition);
                        Log.d("Debug", "Lng Condition: " + lngCondition);
                        Log.d("Debug", "Lng Condition Part 1: " + lngConditionPart1);
                        Log.d("Debug", "Lng Condition Part 2: " + lngConditionPart2);
                        Log.d("Debug", "Is " + longitude + " >= " + EAST_LANSING_MIN_LNG + " : " + lngConditionPart1);
                        Log.d("Debug", "Is " + longitude + " <= " + EAST_LANSING_MAX_LNG + " : " + lngConditionPart2);

                        double rlongitude = Math.round(longitude * 1000000d) / 1000000d; // round long for better calibration

                        Log.d("Debug", "EAST_LANSING_MIN_LNG: " + EAST_LANSING_MIN_LNG);
                        Log.d("Debug", "EAST_LANSING_MAX_LNG: " + EAST_LANSING_MAX_LNG);
                        Log.d("Debug", "Rounded Longitude: " + rlongitude);

                        if (latCondition && lngCondition) { // check to see if long and lat is in the bounds using their absolute value for easier comparison
                            if(longneg){ //checks to see if original long is negative then it fits in the bounds for east lansing
                                // Inside East Lansing
                                Log.d("LocationDebug", "Inside East Lansing");
                                alertShown = false; // says not to alert user
                                break;
                            }

                        } else if(!alertShown){
                            // The user is out of bound
                            Log.d("LocationDebug", "Outside East Lansing");

                            alertShown = true;  // Set the flag to true so the alert is not shown again
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Out of Bounds")
                                    .setMessage("ADVISORY\nYou are currently outside the allowed area. Get back to the Homeland")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                }
            }
        };
        //start location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, requireActivity().getMainLooper())
                .addOnFailureListener(e -> {
                    new AlertDialog.Builder(getContext())
                            //error if location was not fetched
                            .setTitle("Error!")
                            .setMessage("An error occurred while fetching location: " + e.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                checkLocation();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}



