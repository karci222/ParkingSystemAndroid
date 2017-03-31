package org.marso.karol.client2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ListData> regions = new ArrayList<ListData>();
    private ArrayList<Locations> locationses = new ArrayList<Locations>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);




        String data1 = null;
        String stringUrl = "https://api.backendless.com/v1/data/SPACES";//url to download from. Downloads all the data
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);//manages connections
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();//gathers information about network

        //if device is being connected and network works
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                data1 = new DownloadWebpageTask().execute(stringUrl).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        //parses data downloaded from server
        try {
            JSONObject obj = new JSONObject(data1);
            JSONArray jArr = obj.getJSONArray("data");
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jObj = jArr.getJSONObject(i);
                int Space_num = jObj.getInt("SPACE_NUM");
                int Region_num = jObj.getInt("REGION_NUM");
                int Free = jObj.getInt("FREE");
                Log.d("DATA: ", "REGION:  " + Region_num + " SPACE NUMBER: " + Space_num + " Free: " + Free);
                checkRegions(regions, Region_num,Free, Space_num);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data2 = null;
        stringUrl = "https://api.backendless.com/v1/data/Region_locations";//url to download from. Downloads all the data


        //if device is being connected and network works
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                data2 = new DownloadWebpageTask().execute(stringUrl).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        //parses data downloaded from server
        try {
            JSONObject obj = new JSONObject(data2);
            JSONArray jArr = obj.getJSONArray("data");
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jObj = jArr.getJSONObject(i);
                int Region_num = jObj.getInt("Region_num");
                double xLoc = jObj.getDouble("xLoc");
                double yLoc = jObj.getDouble("yLoc");
                locationses.add(new Locations(Region_num, xLoc, yLoc));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        mapFragment.getMapAsync(this);
    }

    public boolean checkRegions(ArrayList<ListData> list, int region, int Free, int Space_num){
        boolean found = false;

        for (ListData listData: list
                ) {
            if (listData.region == region){
                if(Free == 1){
                    listData.freeSpaces++;

                }
                listData.addToSpaceRecords(Free, Space_num);
                found = true;
            }
        }

        if(!found){
            if(Free == 0){
                ListData data = new ListData(region, 0);
                data.addToSpaceRecords(Free, Space_num);
                list.add(data);
            }else{
                ListData data = new ListData(region, 1);
                data.addToSpaceRecords(Free, Space_num);
                list.add(data);
            }

        }
        return found;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(Locations locations: locationses){
            if(findFreeSpaces(locations.Region_num) > 0){
                Marker mLoc = googleMap.addMarker(new MarkerOptions().position(new LatLng(locations.xLoc, locations.yLoc)).
                        title("Region: " + locations.Region_num + " Free: " + findFreeSpaces(locations.Region_num))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }else{
                Marker mLoc = googleMap.addMarker(new MarkerOptions().position(new LatLng(locations.xLoc, locations.yLoc)).
                        title("Region: " + locations.Region_num + " Free: " + findFreeSpaces(locations.Region_num))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }
    public int findFreeSpaces(int Region_num){
        for (ListData listData:regions
             ) {
            if(listData.region == Region_num){
                return listData.freeSpaces;
            }
        }

        return  -1;
    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private static final String DEBUG_TAG = "HttpExample";

        //Downloads data from URL
        private String downloadUrl(String myurl) throws IOException {

            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //sets some stuff, maybe you can change it if you know what you are doing
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                //sets parameters for HTTP get
                //remember these things even in other HTTP requests, this is very reusable code
                //such code
                //much reusability
                //WOW!!!
                conn.setRequestMethod("GET");
                conn.setRequestProperty("application-id", "A7B592CB-6F8B-B207-FFE8-3706A8A51100");//app id
                conn.setRequestProperty("secret-key", "AA29AB77-265B-016D-FF75-597FA2F18A00");//secret key for REST API
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("application-type", "REST");

                conn.setDoInput(true);

                // Starts the query
                conn.connect();

                int response = conn.getResponseCode();
                List length = conn.getHeaderFields().get("content-length");//finds out how long is HTTP response.
                is = conn.getInputStream();
                //Log.d("Debug", "DownloadWebpageTask: content? = " + conn.getContent());
                //Log.d("Debug", "DownloadWebpageTask: content encoding = " + conn.getContentEncoding());
                //Log.d("Debug", "DownloadWebpageTask: response code = " + conn.getResponseCode() + "");


                // Convert the InputStream into a string
                String contentAsString = readIt(is, Integer.parseInt((String)length.get(0)));//reads HTTP response
                Log.d("Debug", "DownloadWebpageTask: content = " + contentAsString);

                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }


        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, final int length) throws IOException, UnsupportedEncodingException {
            Log.d("Debug", "readIt: length = " + length + "");

            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer= new char[length];
            String str = new String();
            String temp;

            int len = reader.read(buffer);

            do {
                temp = new String(buffer);
                temp = temp.replace('\0', ' ');
                str = str + temp;

                Log.d("Debug", "readIt: num of read characters = " + len + "");
                Log.d("Debug", "readIt: contents = " + temp + "");

                buffer= new char[length];
                len = reader.read(buffer);
            } while (len != -1);

            return str;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            // android.os.Debug.waitForDebugger();
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }


        // onPostExecute displays the results of the AsyncTask.
        //Well, I don`t really know, when we will use this :D
        @Override
        protected void onPostExecute(String result) {

        }
    }
}

class Locations{
    int Region_num;
    double xLoc;
    double yLoc;


    public Locations(int Region_num, double xLoc, double yLoc){
        this.Region_num = Region_num;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
    }
}

