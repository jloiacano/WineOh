package com.example.android.wineoh.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.wineoh.R;
import com.example.android.wineoh.data.WineContract.WineEntry;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by J on 4/9/2017.
 *
 * The {@link WineProvider} does just that; Provides the Wine to and from the database
 */

public class WineProvider extends ContentProvider {

    private static final String LOG_TAG = WineProvider.class.getSimpleName();
    // For routing the requests to the whole database
    private static final int WINES = 100;
    // For routing the requests to specific rows in the database
    private static final int WINE_ID = 101;
    // Matches the URI to the proper code, WINES or WINE_ID, to direct the request
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(WineContract.CONTENT_AUTHORITY, WineContract.PATH_WINES, WINES);
        sUriMatcher.addURI(WineContract.CONTENT_AUTHORITY, WineContract.PATH_WINES + "/#", WINE_ID);
    }

    private WineDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        //initialize WineDbHelper
        mDbHelper = new WineDbHelper(getContext());

        return false;
    }

    // Performs the query for the given URI with the given CursorLoader
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query to be returned
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                cursor = database.query(WineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WINE_ID:
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(WineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                String queryException = getContext().getString(R.string.provider_exception_query);
                throw new IllegalArgumentException(queryException + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), WineEntry.CONTENT_URI);
        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                return insertWine(uri, contentValues);
            default:
                String insertionException = getContext().getString(R.string.provider_exception_insert);
                throw new IllegalArgumentException(insertionException + uri);
        }
    }


     // Validate the incoming ContentValues then insert into database
    private Uri insertWine(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(WineEntry.COLUMN_WINE_NAME);
        if (name == null) {
            String namedException = getContext().getString(R.string.provider_exception_name);
            throw new IllegalArgumentException(namedException);
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the values
        long id = database.insert(WineEntry.TABLE_NAME, null, values);

        if (id == -1) {
            String logInsert = getContext().getString(R.string.provider_error_log_insert) + uri;
            Log.e(LOG_TAG, logInsert);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

     // Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                return updateWine(uri, contentValues, selection, selectionArgs);
            case WINE_ID:
                // For the WINE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateWine(uri, contentValues, selection, selectionArgs);
            default:
                String updateException = getContext().getString(R.string.provider_exception_update);
                throw new IllegalArgumentException(updateException + uri);
        }
    }

     // Verify the ContentValues of the incoming update request, then update.
     // Return the number of rows that were successfully updated.
    private int updateWine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that the winery is not null
        String winery = values.getAsString(WineEntry.COLUMN_WINE_WINERY);
        if (winery == null) {
            String wineryException = getContext().getString(R.string.provider_exception_winery);
            throw new IllegalArgumentException(wineryException);
        }

        // Check that the name is not null
        String name = values.getAsString(WineEntry.COLUMN_WINE_NAME);
        if (name == null) {
            String needsANameException = getContext().getString(R.string.provider_exception_need_name);
            throw new IllegalArgumentException(needsANameException);
        }

        // Check that the varietal of the wine is not null
        String varietal = values.getAsString(WineEntry.COLUMN_WINE_VARIETAL);
        if (varietal == null) {
            String varietalException = getContext().getString(R.string.provider_exception_varietal);
            throw new IllegalArgumentException(varietalException);
        }

        // Check that the year is between 0 and the current year
        int year = values.getAsInteger(WineEntry.COLUMN_WINE_YEAR);

        String zoneString = getContext().getString(R.string.provider_timezone_);
        TimeZone timeZone = TimeZone.getTimeZone(zoneString);
        Calendar currentCalendar = Calendar.getInstance(timeZone);

        if (year < 0 || year > currentCalendar.get(Calendar.YEAR)) {
            String yearException = getContext().getString(R.string.provider_exception_year);
            throw new IllegalArgumentException(yearException);
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Update the values
        int rowsAffected = database.update(WineEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsAffected > 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

     // Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        switch (match) {
            case WINES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(WineEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case WINE_ID:
                // Delete a single row given by the ID in the URI
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(WineEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                String deleteException = getContext().getString(R.string.provider_exception_delete);
                throw new IllegalArgumentException(deleteException + uri);
        }
    }

     // Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        return null;
    }
}