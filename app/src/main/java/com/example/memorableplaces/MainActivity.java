package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    double latitude, longitude;
    CustomAdapter customAdapter;
    Button addButton;
    LocationListener locationListener;
    LocationManager locationManager;
    ListView listView;
    String mapMode="Normal";
    public static ArrayList<MapModel> mainArray = new ArrayList<>();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateLocationInfo(lastKnownLocation);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isInternetConnected()){
            showCustomDialog();
        }else if(!isLocationEnabled()){
            showCustomDialogForLocation();
        }
        SharedPreferences sharedPreferences = this.getSharedPreferences("sharedArray", MODE_PRIVATE);
        mainArray.clear();
        try {
            //noinspection unchecked
            mainArray = (ArrayList<MapModel>) ObjectSerializer.deserialize(sharedPreferences.getString("place", ObjectSerializer.serialize(new ArrayList<MapModel>())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        addButton = findViewById(R.id.addButtonId);
        listView = findViewById(R.id.listViewId);
        customAdapter = new CustomAdapter(this, mainArray);
        customAdapter.notifyDataSetChanged();
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("lat", mainArray.get(i).getLatitude());
                intent.putExtra("long", mainArray.get(i).getLongitude());
                intent.putExtra("address",mainArray.get(i).getAddress());
                intent.putExtra("mapMode",mapMode);
                startActivityForResult(intent, 1);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int itemToRemove = i;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Delete?");
                alertDialog.setMessage("Are you want to delete this item?");
                alertDialog.setCancelable(true);
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainArray.remove(itemToRemove);
                        try {
                            sharedPreferences.edit().putString("place", ObjectSerializer.serialize(mainArray)).apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        customAdapter.notifyDataSetChanged();
                    }
                });
                alertDialog.show();
                return true;
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateLocationInfo(lastKnownLocation);
            }
        }
        addButton.setOnClickListener(this);

    }

    private void showCustomDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("No internet");
        alertDialog.setMessage("Please check your internet connection");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        if(!isLocationEnabled()){
            showCustomDialogForLocation();
        }else if(!isInternetConnected()){
            showCustomDialog();
        }else if (latitude != 0 && longitude != 0) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("lat", latitude);
            intent.putExtra("long", longitude);
            intent.putExtra("address","your location");
            intent.putExtra("mapMode",mapMode);
            startActivityForResult(intent, 1);
        }else{
            Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            customAdapter.notifyDataSetChanged();
        }
    }

    private void updateLocationInfo(Location location) {
        if(location!=null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }else{
            Toast.makeText(getApplicationContext(), "Please Wait", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isInternetConnected(){
        ConnectivityManager connectivityManager= (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (wifiConn!=null&&wifiConn.isConnected())||(mobileConn!=null&&mobileConn.isConnected());
    }
    public boolean isLocationEnabled(){
        LocationManager lm= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enable=false;
        boolean network_enable=false;
        try {
            gps_enable=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enable=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }
        return gps_enable && network_enable;
    }
    public void showCustomDialogForLocation(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS Disable");
        alertDialog.setMessage("Please turn on your GPS");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout,menu);
        MenuItem menuItem=menu.findItem(R.id.spinner);
        Spinner spinner= (Spinner) menuItem.getActionView();
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.map_mode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mapMode=adapterView.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.userGuideId) {
            startActivity(new Intent(MainActivity.this,UserGuide.class));
        }
        return super.onOptionsItemSelected(item);
    }
}