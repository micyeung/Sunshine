package com.example.micyeung.sunshine.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.micyeung.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.micyeung.sunshine.app.data.WeatherContract.LocationEntry;

/**
 * Created by micyeung on 11/21/14.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING +") ON CONFLICT IGNORE"+
                " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";


        // Seeding with dummy data in case there is no connectivity
        final String SQL_INSERT_LOCATION = "INSERT INTO " + LocationEntry.TABLE_NAME +
                " (" + LocationEntry._ID + "," +
                LocationEntry.COLUMN_LOCATION_SETTING + "," +
                LocationEntry.COLUMN_CITY_NAME + "," +
                LocationEntry.COLUMN_COORD_LAT + "," +
                LocationEntry.COLUMN_COORD_LONG + ")" +
                " VALUES (5375480,'94043','Mountain View',37.4123,-122.077);";

        final String SQL_INSERT_WEATHER = "INSERT INTO " + WeatherEntry.TABLE_NAME +
                " ( " + WeatherEntry.COLUMN_LOC_KEY + "," + WeatherEntry.COLUMN_DATETEXT + "," + WeatherEntry.COLUMN_SHORT_DESC + "," + WeatherEntry.COLUMN_WEATHER_ID + "," + WeatherEntry.COLUMN_MIN_TEMP + "," + WeatherEntry.COLUMN_MAX_TEMP + "," + WeatherEntry.COLUMN_HUMIDITY + "," + WeatherEntry.COLUMN_PRESSURE + "," + WeatherEntry.COLUMN_WIND_SPEED + "," + WeatherEntry.COLUMN_DEGREES + ")" +
                " SELECT 5375480 AS " + WeatherEntry.COLUMN_LOC_KEY + ", '20150306' AS " + WeatherEntry.COLUMN_DATETEXT + ", 'Clear' AS " + WeatherEntry.COLUMN_SHORT_DESC + ", 800 AS " + WeatherEntry.COLUMN_WEATHER_ID + ", 9.33 AS " + WeatherEntry.COLUMN_MIN_TEMP + ", 12.15 AS " + WeatherEntry.COLUMN_MAX_TEMP + ", 82 AS " + WeatherEntry.COLUMN_HUMIDITY + ", 1000.7 AS " + WeatherEntry.COLUMN_PRESSURE + ", 0.9 AS " + WeatherEntry.COLUMN_WIND_SPEED + ", 352 AS " + WeatherEntry.COLUMN_DEGREES +
                " UNION SELECT 5375480,20150311,'Clear',800,4.81,25.28,43,998.46,1.32,42" +
                " UNION SELECT 5375480,20150312,'Rain',501,9.76,16.32,43,780.46,0.87,96" +
                " UNION SELECT 5375480,20150313,'Clear',800,10.12,20.12,56,1001.12,1.12,240" +
                " UNION SELECT 5375480,20150314,'Clouds',803,7.3,19.9,72,980.12,2.12,145" +
                " UNION SELECT 5375480,20150315,'Clouds',803,9.1,18.2,54,1003.1,1.12,89" +
                " UNION SELECT 5375480,20150316,'Rain',501,8.2,18.2,40,780.1,1.12,40" +
                " UNION SELECT 5375480,20150317,'Snow',511,-4.2,-2.31,37,818.32,0.72,19" +
                " UNION SELECT 5375480,20150318,'Snow',511,-8.2,4.32,39,828.91,0.93,23" +
                " UNION SELECT 5375480,20150319,'Clear',800,10.3,21.1,72,1000.12,0.51,90" +
                " UNION SELECT 5375480,20150320,'Clear',800,11.9,18.2,83,340.34,0.34,10" +
                " UNION SELECT 5375480,20150321,'Clear',800,9.3,18.7,81,781.7,1.91,23" +
                " UNION SELECT 5375480,20150322,'Storm',200,14.9,21.2,98,1012.8,1.98,45" +
                " UNION SELECT 5375480,20150323,'Storm',200,15.4,22.3,89,1008.9,2.09,115" +
                " UNION SELECT 5375480,20150324,'Storm',200,18.3,25.1,84,988.9,2.55,87";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        sqLiteDatabase.execSQL(SQL_INSERT_LOCATION);
        sqLiteDatabase.execSQL(SQL_INSERT_WEATHER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
