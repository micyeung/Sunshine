package com.example.micyeung.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.micyeung.sunshine.app.data.WeatherContract;
import com.example.micyeung.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.micyeung.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.micyeung.sunshine.app.sync.SunshineSyncAdapter;

import java.util.Date;

/**
 * Created by micyeung on 9/17/14.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    //ArrayAdapter<String> forecastAdapter;
    ForecastAdapter forecastAdapter;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final int FORECAST_LOADER = 0;
    private String mLocation;
    public static final String SELECTED_KEY = "selected_position";
    private int mCursorPosition = ListView.INVALID_POSITION;
    private ListView mListView;
    private boolean mUseTodayLayout;

    private static final String[] FORECAST_COLUMNS = {
        // In this case the id needs to be fully qualified with a table name, since
        // the content provider joins the location & weather tables in the background
        // (both have an _id column)
        // On the one hand, that's annoying.  On the other, you can search the weather table
        // using the location set by the user, which is only in the Location table.
        // So the convenience is worth it.
        WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATETEXT,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP,
        LocationEntry.COLUMN_LOCATION_SETTING,
        WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    /**
     * A callback interface that all activities containing this fragment must implement.
     * This mechanism allows activities to be notified of item selections
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an items has been selected
         */
        public void onItemSelected(String date, int position);
    }


    public ForecastFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void updateWeather() {
        // 3 Ways to call web service and update/refresh weather


        // Method 1: Using AsyncTask (FetchWeatherTask)

//        String location = Utility.getPreferredLocation(getActivity());
//        new FetchWeatherTask(getActivity()).execute(location);



/*
        // Method 2: Using a service, and fire the service through an alarm manager, 5 secs after hitting refresh

        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                Utility.getPreferredLocation(getActivity()));

        // Wrap in a pending intent, so that AlarmManager can fire it under a set of well-defined constraints
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        // Set the AlarmManager to wake up the system
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
*/

        // Method 3: Using a Sync Adapter

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

/*        List<String> forecasts = new ArrayList<String>();
        forecasts.add("Today - Sunny - 88/63");
        forecasts.add("Tomorrow - Foggy - 70/46");
        forecasts.add("Weds - Cloudy - 72/63");
        forecasts.add("Thurs - Rainy - 64/51");
        forecasts.add("Fri - Foggy - 70/46");
        forecasts.add("Sat - Sunny - 76/68");
*/

        forecastAdapter = new ForecastAdapter(getActivity(),null,0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(forecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = forecastAdapter.getCursor();

                if (cursor!=null && cursor.moveToPosition(position)) {
                    // Let MainActivity do the work of figuring out:
                    // on phone, launch DetailActivity;
                    // on tablet, replace DetailFragment
                    ((Callback)getActivity())
                            .onItemSelected(
                                    cursor.getString(COL_WEATHER_DATE),
                                    position - mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount());

                    // When item is clicked, store clicked cursor position
                    mCursorPosition = position;
                }
                forecastAdapter.setUseTodayLayout(mUseTodayLayout);
            }
        });
        // Use the savedInstanceState to restore the selected position
        // when screen's rotated so we can scroll back to the selected position
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mCursorPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG,"onCreateLoader");
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order : Asc by date
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Log.v(LOG_TAG,mLocation);
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        forecastAdapter.swapCursor(cursor);

        // If cursor position has been previously set, scroll the list to that position
        //     This can get called if there's a screen orientation.
        // Else, the cursor position has not been previously set.
        //     If it's a two-pane mode (i.e. "use today layout"), set it to be the first position by default.
        //     Else, it's a one-pane mode, so just leave it.
        if (mCursorPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mCursorPosition);
        } else {
            if (!mUseTodayLayout) {
                mCursorPosition = 0;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.performItemClick(
                                mListView.getChildAt(mCursorPosition),
                                mCursorPosition,
                                mListView.getAdapter().getItemId(mCursorPosition));
                    }
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When rotated, the current selected list item needs to be saved. However, when no item is selected,
        // we don't want mCursorPosition to be set to ListView.INVALID_POSITION, so check for that first.
        if (mCursorPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY,mCursorPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        forecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (forecastAdapter != null) {
            // Need this null check because it's possible that forecastAdapter is null, because
            // MainActivity.onCreate() method (which calls ForecastFragment.setUseTodayLayout()) happens before forecastAdapter
            // is initialized in ForecastFragment's onCreateView() method
            forecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }
}
