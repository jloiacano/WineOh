package com.example.android.wineoh.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by J on 4/9/2017.
 * <p>
 * A {@link WineContract} class to hold the {@link WineEntry} class which sets the parameters of
 * the wines.db
 */

public class WineContract {

    static final String CONTENT_AUTHORITY = "com.example.android.wineoh";

    static final String PATH_WINES = "wines";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // An empty constructor to prevent instantiation
    private WineContract() {
    }

    public static final class WineEntry implements BaseColumns {


        //The URI for the class
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WINES);
        // The Table Columns
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_WINE_PHOTO = "photo";
        public final static String COLUMN_WINE_NAME = "name";
        public final static String COLUMN_WINE_WINERY = "winery";
        public final static String COLUMN_WINE_COUNTRY = "country";
        public final static String COLUMN_WINE_STATE = "state";
        public final static String COLUMN_WINE_VARIETAL = "varietal";
        public final static String COLUMN_WINE_YEAR = "year";
        public final static String COLUMN_WINE_CATEGORY = "category";
        public final static String COLUMN_WINE_BOTTLE_SIZE = "size";
        public final static String COLUMN_WINE_COST = "cost";
        public final static String COLUMN_WINE_PRICE = "price";
        public final static String COLUMN_WINE_DESCRIPTION = "description";
        public final static String COLUMN_WINE_QUANTITY = "quantity";
        public final static String COLUMN_WINE_DISTRIBUTOR = "distributor";
        public final static String COLUMN_WINE_DISTRIBUTOR_PHONE = "phone";
        // THe Table name
        final static String TABLE_NAME = "wines";

    }
}
