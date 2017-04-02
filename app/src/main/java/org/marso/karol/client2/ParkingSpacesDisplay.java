package org.marso.karol.client2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

public class ParkingSpacesDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_spaces_display);
        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.my_toolbar_parking_spaces);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);



        Intent intent = getIntent();
        ListData listData = (ListData) intent.getParcelableExtra("DATA");
        Button buttons[] = new Button[4];
        buttons[0] = (Button)findViewById(R.id.Button1);
        buttons[1] = (Button)findViewById(R.id.Button2);
        buttons[2] = (Button)findViewById(R.id.Button3);
        buttons[3] = (Button)findViewById(R.id.Button4);


        Log.d("Data","" + listData.space_records.size());

        for (SPACE_RECORD space_record : listData.space_records){
            if(space_record.Free == 0){
                buttons[space_record.Space_num].setBackgroundColor(getResources().getColor(R.color.colorGreen));
            }else{
                buttons[space_record.Space_num].setBackgroundColor(getResources().getColor(R.color.colorRed));
            }
        }
    }
}
