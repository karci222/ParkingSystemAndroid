package org.marso.karol.client2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
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
    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<ListData> regions = new ArrayList<ListData>();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final ListView listView = (ListView) findViewById(R.id.list_view);



        String data = null;
        String stringUrl = "https://api.backendless.com/v1/data/SPACES";//url to download from. Downloads all the data
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
                Log.d("DATA: ", "REGION:  " + Region_num + " SPACE NUMBER: " + Space_num + " Free: " + Free);
                checkRegions(regions, Region_num,Free, Space_num);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final yourAdapter mAdapter = new yourAdapter(this, regions);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ParkingSpacesDisplay.class);
                intent.putExtra("DATA", (Parcelable) mAdapter.getItem(i));
                startActivity(intent);

            }
        });
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapButton:
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean checkRegions(ArrayList<ListData> list, int region, int Free, int Space_num){
        boolean found = false;

        for (ListData listData: list
             ) {
            if (listData.region == region){
                if(Free == 0){
                    listData.freeSpaces++;

                }
                listData.addToSpaceRecords(Free, Space_num);
                found = true;
            }
        }

        if(!found){
            if(Free == 0){
                ListData data = new ListData(region, 1);
                data.freeSpaces = 1;
                data.addToSpaceRecords(Free, Space_num);
                list.add(data);
            }else{
                ListData data = new ListData(region, 0);
                data.addToSpaceRecords(Free, Space_num);
                list.add(data);
            }

        }
        return found;
    }





}
class yourAdapter extends ArrayAdapter<ListData> implements View.OnClickListener {

    Context context;
    ArrayList<ListData> data;
    private static LayoutInflater inflater = null;

    public yourAdapter(Context context, ArrayList<ListData> data) {
        // TODO Auto-generated constructor stub
        super(context, R.layout.row, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public ListData getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ListData listData = getItem(position);

        final View result;
        TextView txtRegion = null;
        TextView txtNumberOfFree = null;

        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row, parent, false);
            txtRegion = (TextView) convertView.findViewById(R.id.RegionNumber);
            txtNumberOfFree = (TextView) convertView.findViewById(R.id.NumberOfFreeSpaces);
            if(position%2 ==0)
                convertView.setBackgroundColor(parent.getResources().getColor(R.color.colorPrimary));
            result=convertView;
        } else {
            result=convertView;
        }


        txtRegion.setText("Region number: " + String.valueOf(listData.region));
        txtNumberOfFree.setText(String.valueOf("Number of free spaces: " + listData.freeSpaces));
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void onClick(View view) {

    }
}

class SPACE_RECORD implements Parcelable{
    int Space_num;
    int Region_num;
    int Free;

    public SPACE_RECORD(int Space_num, int Region_num, int Free){
        this.Space_num = Space_num;
        this.Region_num = Region_num;
        this.Free = Free;
    }

    protected SPACE_RECORD(Parcel in) {
        Space_num = in.readInt();
        Region_num = in.readInt();
        Free = in.readInt();
    }

    public static final Creator<SPACE_RECORD> CREATOR = new Creator<SPACE_RECORD>() {
        @Override
        public SPACE_RECORD createFromParcel(Parcel in) {
            return new SPACE_RECORD(in);
        }

        @Override
        public SPACE_RECORD[] newArray(int size) {
            return new SPACE_RECORD[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Space_num);
        parcel.writeInt(Region_num);
        parcel.writeInt(Free);
    }
}

class ListData implements Parcelable{
    public int region;
    public int freeSpaces;
    public ArrayList<SPACE_RECORD> space_records = new ArrayList<SPACE_RECORD>();

    public ListData(int region, int freeSpaces){
        this.region = region;
        this.freeSpaces = freeSpaces;
    }

    protected ListData(Parcel in) {
        region = in.readInt();
        freeSpaces = in.readInt();
        space_records = in.createTypedArrayList(SPACE_RECORD.CREATOR);
    }

    public static final Creator<ListData> CREATOR = new Creator<ListData>() {
        @Override
        public ListData createFromParcel(Parcel in) {
            return new ListData(in);
        }

        @Override
        public ListData[] newArray(int size) {
            return new ListData[size];
        }
    };

    public void addToSpaceRecords(int Free, int Space_num){
        space_records.add(new SPACE_RECORD(Space_num, this.region, Free));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(region);
        parcel.writeInt(freeSpaces);
        parcel.writeTypedList(space_records);
    }
}