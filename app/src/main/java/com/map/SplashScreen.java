package com.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class SplashScreen extends Activity{

    GPSTracker gps;
    double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        checkInternetConnection();
    }

    private void checkInternetConnection() {
        ConnectivityManager connectivityManager =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED){
            gpsCheck();
            //loadingScreen();
            Toast.makeText(getApplicationContext(), "Network is connected", Toast.LENGTH_SHORT).show();
        } else if (connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED){
            alertMessage();
        }
    }

    private void gpsCheck() {
        gps = new GPSTracker(SplashScreen.this);
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.e("in","latitude="+latitude+","+longitude);
            loadingScreen();
        }else{
            gps.showSettingsAlert();
        }
    }

    private void alertMessage() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Network error :");
        builder.setMessage("No network access")
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setNegativeButton("setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent =new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadingScreen() {
        Thread back = new Thread(){
            public void run(){
                try{
                    sleep(3 * 1000);
                    Intent intent = new Intent(SplashScreen.this,HomeScreen.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    startActivity(intent);
                    finish();
                }
                catch (Exception e){
                }
            }
        };
        back.start();
    }
}
