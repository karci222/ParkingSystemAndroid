package org.marso.karol.client2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    class SPACE_RECORD{
        int Space_num;
        int Region_num;
        int Free;

        public SPACE_RECORD(){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv1 = (TextView)findViewById(R.id.Space1);
        TextView tv2 = (TextView)findViewById(R.id.Space2);
        TextView tv3 = (TextView)findViewById(R.id.Space3);
        TextView tv4 = (TextView)findViewById(R.id.Space4);

        ArrayList<TextView> tvs = new ArrayList<TextView>();
        tvs.add(tv1);
        tvs.add(tv2);
        tvs.add(tv3);
        tvs.add(tv4);

        String data = null;
        String stringUrl = "https://api.backendless.com/v1/data/SPACES?where=REGION_NUM%3D0";//url to download from. Downloads all the data
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);//manages connections
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();//gathers information about network

        //if device is being connected and network works
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                data = new DownloadWebpageTask().execute(stringUrl).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        //parses data downloaded from server
        try {
            JSONObject obj = new JSONObject(data);
            JSONArray jArr = obj.getJSONArray("data");
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jObj = jArr.getJSONObject(i);
                int Space_num = jObj.getInt("SPACE_NUM");
                int Region_num = jObj.getInt("REGION_NUM");
                int Free = jObj.getInt("FREE");
                if(Free==1) {
                    tvs.get(i).setText("Region: " + Region_num + " SPACE: " + Space_num +  " is occupied");
                }else{
                    tvs.get(i).setText("Region: " + Region_num + " SPACE: " + Space_num +  " is free");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
