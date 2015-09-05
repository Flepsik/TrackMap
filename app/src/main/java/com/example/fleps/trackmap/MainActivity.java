package com.example.fleps.trackmap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.example.fleps.trackmap.entity.TrackPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Fleps_000 on 05.09.2015.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private ArrayList<TrackPoint> points;
    ArrayList<String> files;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.listView);
        AssetManager myAssetManager = getApplicationContext().getAssets();
        try {
            files = new ArrayList<>(Arrays.asList(myAssetManager.list("")));
            for(int i = 0; i < files.size();) {
                if (!files.get(i).endsWith("gpx"))
                    files.remove(i);
                else i++;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_activated_1, files);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("filename", files.get(i));
        startActivity(intent);
    }




}
