package com.map;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreen extends FragmentActivity {

    Double lat,lon;
    GoogleMap googleMap;
    LatLng latLng;
    MarkerOptions markerOptions;
    ArrayList markerPoints;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    ActionBarDrawerToggle drawerToggle;
    LinearLayout location,distanceLayout;
    CircleImageView route_CircleImage;
    Button source_find_Button,desination_find_Button;
    TextView distanceTextView,durationTextView;
    int rooticon=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        toolbar = (Toolbar)findViewById(R.id.toolBar);
        recyclerView  = (RecyclerView) findViewById(R.id.recyclerView);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerMainActivity);
        source_find_Button = (Button) findViewById(R.id.source_find_Button);
        desination_find_Button= (Button) findViewById(R.id.dest_find_Button);
        location = (LinearLayout)findViewById(R.id.location);
        distanceLayout = (LinearLayout)findViewById(R.id.distanceLayout);
        route_CircleImage = (CircleImageView)findViewById(R.id.route_CircleImage);
        distanceTextView = (TextView)findViewById(R.id.DistanceTextView);
        durationTextView = (TextView)findViewById(R.id.DurationTextView);
        location.setVisibility(View.GONE);
        distanceLayout.setVisibility(View.GONE);
        route_CircleImage.setVisibility(View.VISIBLE);

        Intent gps = getIntent();
        lat = gps.getDoubleExtra("latitude",0.0);
        lon = gps.getDoubleExtra("longitude",0.0);

        route_CircleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rooticon==0){
                    location.setVisibility(View.VISIBLE);
                    rooticon=1;
                }else if (rooticon==1){
                    location.setVisibility(View.GONE);
                    rooticon=0;
                }
            }
        });

        source_find_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText Location_ET = (EditText) findViewById(R.id.source_location_EditText);
                String location = Location_ET.getText().toString();
                if (location != null && !location.equals("")) {
                    new GeocoderTask().execute(location);
                }
            }
        });

        markerPoints = new ArrayList();
        SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(markerPoints.size()>=2){
                    markerPoints.clear();
                    googleMap.clear();
                }
                markerPoints.add(point);
                MarkerOptions options = new MarkerOptions();
                options.position(point);
                if(markerPoints.size()==1){
                    googleMap.clear();
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    route_CircleImage.setVisibility(View.VISIBLE);
                }else if(markerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    distanceLayout.setVisibility(View.VISIBLE);
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }
                googleMap.addMarker(options);
            }
        });

        recyclerViewAdapter = new RecyclerViewAdapter(this,lat,lon);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupDrawerToggle();
    }

    private void setupDrawerToggle() {
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerToggle.syncState();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String waypoints = "";
        for(int i=2;i<markerPoints.size();i++){
            LatLng point = (LatLng) markerPoints.get(i);
            if(i==2)
            waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";
            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j <path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);
            }
            distanceTextView.setText(" Distance : "+distance);
            durationTextView.setText(" Duration : "+duration);
            googleMap.addPolyline(lineOptions);
        }
    }
    //-------------------source location search---------------------------//
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
        Button destButton;
        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
            googleMap.clear();
            destButton = (Button) findViewById(R.id.dest_find_Button);
            for(int i=0;i<addresses.size();i++){
                Address address = (Address) addresses.get(i);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerOptions.title(addressText);
                googleMap.addMarker(markerOptions);
                if(i==0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                destButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        distanceLayout.setVisibility(View.VISIBLE);
                        EditText Location_ET = (EditText) findViewById(R.id.dest_location_EditText);
                        String location = Location_ET.getText().toString();
                        if (location != null && !location.equals("")) {
                            new DestLocation(latLng).execute(location);
                        }
                    }
                });
            }
        }
    }

    //-----------------desination location search---------------------------//
    private class DestLocation extends AsyncTask<String, Void, List<Address>>{
        LatLng lat;
        public DestLocation(LatLng latLng) {
            this.lat=latLng;
        }

        @Override
        protected List<Address> doInBackground(String... desination) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(desination[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
            for(int i=0;i<addresses.size();i++){
                Address address = (Address) addresses.get(i);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markerOptions.title(addressText);
                googleMap.addMarker(markerOptions);
                if(i==0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                LatLng origin = lat;
                LatLng dest = latLng;
                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        }
    }

}
