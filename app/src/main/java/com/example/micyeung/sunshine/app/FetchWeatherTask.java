package com.example.micyeung.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.micyeung.sunshine.app.data.WeatherContract.LocationEntry;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by micyeung on 11/21/14.
 */
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
                return null;
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
            wdp.getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery, mContext);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
        // This will only happen if there was an error getting or parsing the forecast
        return null;
    }
}