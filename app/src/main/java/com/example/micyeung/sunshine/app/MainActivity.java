package com.example.micyeung.sunshine.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.micyeung.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback, DetailFragment.DetailCallback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    // This is the current theme (i.e. status bar and action bar) color.
    // Initialize to an invalid value
    private int mThemeColor = DetailFragment.INVALID_COLOR;
    private static final String THEME_COLOR_KEY = "main_activity_theme_color";

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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(THEME_COLOR_KEY, mThemeColor);
        super.onSaveInstanceState(outState);
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
        if (savedInstanceState != null) {
            mThemeColor = savedInstanceState.getInt(THEME_COLOR_KEY);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in large screen layouts (sw600dp)
            // This view is created only inn the sw600dp activity_main, so its presence means it's a two-pane layout
            mTwoPane = true;

            // Since it's two-pane mode, we show the detail view in this main activity by adding or replacing
            // the detail fragment using a fragment transaction
            // If savedInstanceState exists, then we should let the system handle restoring the detail fragment,
            // otherwise, we have to add in the detail fragment dynamically
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container,new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        // If it's two-pane mode, then we don't use today-layout for the master (left) layout, i.e. first
        // row of the master looks like the other rows
        // Else, in a one-pane mode, the first row looks different, more emphasized, bigger image, etc.
        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayUseLogoEnabled(true);//
        //        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(true);
        ab.setLogo(R.drawable.ic_logo);
        ab.setDisplayShowTitleEnabled(false);

        // Initializes Sync Adapter. If we're using other ways of pulling data
        // e.g. with FetchWeatherTask or SunshineService, comment this line out.
        SunshineSyncAdapter.initializeSyncAdapter(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowReturnTransitionOverlap(true);
            getWindow().setReenterTransition(new Fade()
                    .excludeTarget(android.R.id.navigationBarBackground, true));
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
            Log.d(LOG_TAG, "Could call " + location);
        }
    }

    @Override
    public void onItemSelected(String date, int visiblePosition) {
        // date is the date of the list item selected
        // position is the list position, counted from the visible portion of the list
        if (mTwoPane) {
            // How to pass the selected date to the DetailFragment?
            // Ans: we create an empty DetailFragment, then set its arguments.
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY,date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container,detailFragment)
                    .commit();

        } else { // Not two-pane
            Intent launchDetailActivityIntent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivity.DATE_KEY, date);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setExitTransition(defineExitTransition(visiblePosition));
            }

            View listItemView = ((ListView) findViewById(R.id.listview_forecast))
                    .getChildAt(visiblePosition);
            ForecastAdapter.ViewHolder viewHolder = (ForecastAdapter.ViewHolder) listItemView.getTag();
            View iconView = viewHolder.iconView;
            View textView = viewHolder.descriptionView;
            Resources res = getResources();
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    Pair.create(iconView, res.getString(R.string.transition_image)),
                    Pair.create(textView, res.getString(R.string.transition_forecast_text))
            );
            ActivityCompat.startActivity(this, launchDetailActivityIntent, activityOptions.toBundle());
        }
    }

    // This is only called in two-pane mode, as a callback from DetailFragment
    @Override
    public void doColorChange(int toColor) {
        // Need this check because ValueAnimator methods only work on API 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mThemeColor == DetailFragment.INVALID_COLOR) {
                mThemeColor = Utility.getPrimaryColor(this);
            }
            int fromColor = mThemeColor;
            Utility.changeBarColor(this, fromColor, toColor);
            // Set the theme color to be the new color
            mThemeColor = toColor;
        }
    }


    // Returns the Transition that descibes how MainActivity acts when it goes to DetailActivity
    // Takes in the position in the list that was clicked (counted from the visible portion of the list)
    // The animation is a splitting apart of the List View
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    Transition defineExitTransition(int visiblePosition) {
        ListView lv = (ListView) findViewById(R.id.listview_forecast);

        Transition upperTrans = new Slide(Gravity.TOP)
                .addTarget(findViewById(R.id.toolbar))
                .setDuration(800);
        for (int i=0; i<visiblePosition; i++) {
            upperTrans.addTarget(lv.getChildAt(i));
        }

        Transition lowerTrans = new Slide(Gravity.BOTTOM)
                .setDuration(800);
        for (int i=visiblePosition+1; i<lv.getChildCount(); i++) {
            lowerTrans.addTarget(lv.getChildAt(i));
        }

        Transition middleTrans = new Fade().setDuration(100)
                .addTarget(lv.getChildAt(visiblePosition));

        TransitionSet ts = new TransitionSet();
        ts.addTransition(upperTrans)
                .addTransition(lowerTrans)
                .addTransition(middleTrans)
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .excludeTarget(android.R.id.navigationBarBackground, true)
                .excludeTarget(android.R.id.statusBarBackground, true);

        return ts;
    }
}
