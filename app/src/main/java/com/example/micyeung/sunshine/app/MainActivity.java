package com.example.micyeung.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.i("MainActivity","onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.i("MainActivity","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.i("MainActivity","onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.i("MainActivity","onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("MainActivity","onResume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity","onCreate");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent launchSettingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(launchSettingsIntent);
        } else if (id == R.id.action_map) {
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location= prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));

        Uri uri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();
        Intent launchMapIntent = new Intent (Intent.ACTION_VIEW);
        launchMapIntent.setData(uri);
        if (launchMapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(launchMapIntent);
        } else {
            Log.d(LOG_TAG,"Could call " + location);
        }
    }

}
