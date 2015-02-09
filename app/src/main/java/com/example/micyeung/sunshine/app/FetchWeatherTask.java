package com.example.micyeung.sunshine.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

/**
 * Created by micyeung on 11/21/14.
 */
// This fetches the weather data from the web through an AsyncTask.
// This is called from ForecastFragment.updateWeather()
// Compare with SunshineService and SunshineSyncAdapter.

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private final Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String locationQuery = params[0];
        String format = "json";
        String units = "metric";
        int numDays = 14;
        final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNIT_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        Uri uri = Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNIT_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();
        try {
            String forecastJsonStr = NetworkManager.getInstance(mContext).syncRequest(new NetworkManager.StringRequestBuilder().url(uri.toString()));
            WeatherDataParser wdp = new WeatherDataParser();
            wdp.getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery, mContext);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, e.toString());
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, e.toString());
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
        // This will only happen if there was an error getting or parsing the forecast
        return null;
    }
}
