package com.example.micyeung.sunshine.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends ActionBarActivity implements DetailFragment.DetailCallback {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String DATE_KEY = "forecast_date";
    public static final String THEME_COLOR_KEY = "detail_activity_theme_color";

    // This is the current theme (i.e. status bar and action bar) color.
    // Initialize to an invalid value
    private int mThemeColor = DetailFragment.INVALID_COLOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mThemeColor = savedInstanceState.getInt(THEME_COLOR_KEY);
        }
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            // Create the detail fragment, set its date string, and add it to the activity using a fragment transaction
            String date = getIntent().getStringExtra(DATE_KEY);
            Bundle arguments = new Bundle();
            arguments.putString(DATE_KEY,date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, detailFragment)
                    .commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(false);

            getWindow().setEnterTransition(new Explode()
                    .excludeTarget(android.R.id.navigationBarBackground, true)
                    .excludeTarget(android.R.id.statusBarBackground, true));

            getWindow().setReturnTransition(new Explode()
                    .excludeTarget(android.R.id.navigationBarBackground, true));

            postponeEnterTransition();
        }

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(THEME_COLOR_KEY, mThemeColor);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
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
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doColorChange(int toColor) {
        if (mThemeColor == DetailFragment.INVALID_COLOR) {
            mThemeColor = Utility.getPrimaryColor(this);
        }
        int fromColor = mThemeColor;
        Utility.changeBarColor(this, fromColor, toColor);
        // Set the theme color to be the new color
        mThemeColor = toColor;
    }
}
