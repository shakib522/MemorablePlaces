package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplaces.databinding.ActivityMapsBinding;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    double latitude, longitude;
    String address;
    String mapMode="Normal";
    int mark=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("lat", 1);
        longitude = intent.getDoubleExtra("long", 1);
        address=intent.getStringExtra("address");
        mapMode=intent.getStringExtra("mapMode");
        mark=intent.getIntExtra("click",0);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);

        switch (mapMode) {
            case "Normal":
            case "Terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "Satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Hybrid":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "None":
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }

        if(mark==2){
            mMap.addMarker(new MarkerOptions().position(sydney).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        }else if(mark==3){
            mMap.addMarker(new MarkerOptions().position(sydney).title(address).icon(BitmapDescriptorFactory.fromResource(R.drawable.favourite)));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {


            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();


                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address> listAddress;
                String address = "";
                try {
                    listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (listAddress != null && listAddress.size() > 0) {
                        if (listAddress.get(0).getAddressLine(0) != null) {
                            address += listAddress.get(0).getAddressLine(0);
                        } else {
                            Date date = new Date();
                            int hours = date.getHours();
                            int second = date.getSeconds();
                            int minute = date.getMinutes();
                            if (hours > 12) {
                                hours -= 12;
                            }
                            address += "" + hours + ":" + minute + ":" + second + " ";
                            address += new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).format(new Date());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.fromResource(R.drawable.favourite)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                MapModel mapModel = new MapModel(latLng.latitude, latLng.longitude, address);
                MainActivity.mainArray.add(mapModel);
                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("sharedArray", Context.MODE_PRIVATE);
                try {
                    sharedPreferences.edit().putString("place", ObjectSerializer.serialize(MainActivity.mainArray)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent();
        setResult(1,intent);
        finish();
    }
}