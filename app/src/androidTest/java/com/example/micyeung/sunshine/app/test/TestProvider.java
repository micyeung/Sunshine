package com.example.micyeung.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.micyeung.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.micyeung.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.micyeung.sunshine.app.data.WeatherDbHelper;

/**
 * Created by micyeung on 11/21/14.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testInsertReadProvider() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI,values);
        assertTrue(locationUri != null);

        long locationRowId = ContentUris.parseId(locationUri);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                // LocationEntry.CONTENT_URI, // Use this to test for location
                LocationEntry.buildLocationUri(locationRowId), // And use this to see if we can query the row Id
                null, // leaving null queries all columns
                null, // cols for 'where' clause
                null, // values for 'where' clause
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            // Fantastic.  Now that we have a location, add some weather!
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);


        Uri weatherUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI,weatherValues);
        assertTrue(weatherUri != null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                null, // leaving null queries all columns
                null, // cols for 'where' clause
                null, // values for 'where' clause
                null // sort order
        );

        if (!weatherCursor.moveToFirst()) {
            fail("No weather data returned!");
        }

        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)), locationRowId);
        assertEquals(weatherCursor.getString(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)), "20141205");
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)), 1.1);
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)), 1.2);
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)), 1.3);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), 75);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), 65);
        assertEquals(weatherCursor.getString(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)), "Asteroids");
        assertEquals(weatherCursor.getDouble(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)), 5.5);
        assertEquals(weatherCursor.getInt(
                weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)), 321);

        weatherCursor.close();

        dbHelper.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
