package com.example.micyeung.sunshine.app.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.micyeung.sunshine.app.WeatherDataParser;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by micyeung on 12/27/14.
 */
// This fetches the weather data from the web through a service.
// This is called from ForecastFragment.updateWeather()
// Compare with FetchWeatherTask and SunshineSyncAdapter.

public class SunshineService extends IntentService {
    public static final String LOG_TAG = SunshineService.class.getSimpleName();
    public static final String LOCATION_QUERY_EXTRA = "lqe";

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);

        String format = "json";
        String units = "metric";
        int numDays = 14;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;
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
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                forecastJsonStr = null;
            } else {
                forecastJsonStr = buffer.toString();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        WeatherDataParser wdp = new WeatherDataParser();
        try {
            wdp.getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery, this);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                    intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sendIntent);

        }
    }
}
