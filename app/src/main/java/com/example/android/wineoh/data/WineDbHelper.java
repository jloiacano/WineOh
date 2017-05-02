package com.example.android.wineoh.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.wineoh.data.WineContract.WineEntry;

/**
 * Created by J on 4/10/2017.
 * <p>
 * A database helper to either create a wine table or upgrade the table
 */

class WineDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "drinkit.db";
    private static final int DATABASE_VERSION = 1;

    WineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + WineEntry.TABLE_NAME + " ("
                + WineEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WineEntry.COLUMN_WINE_PHOTO + " BLOB, "
                + WineEntry.COLUMN_WINE_NAME + " TEXT NOT NULL, "
                + WineEntry.COLUMN_WINE_WINERY + " TEXT NOT NULL, "
                + WineEntry.COLUMN_WINE_COUNTRY + " TEXT, "
                + WineEntry.COLUMN_WINE_STATE + " TEXT, "
                + WineEntry.COLUMN_WINE_VARIETAL + " TEXT NOT NULL, "
                + WineEntry.COLUMN_WINE_YEAR + " INTEGER NOT NULL, "
                + WineEntry.COLUMN_WINE_CATEGORY + " TEXT, "
                + WineEntry.COLUMN_WINE_BOTTLE_SIZE + " TEXT, "
                + WineEntry.COLUMN_WINE_COST + " REAL NOT NULL, "
                + WineEntry.COLUMN_WINE_PRICE + " REAL NOT NULL, "
                + WineEntry.COLUMN_WINE_DESCRIPTION + " TEXT, "
                + WineEntry.COLUMN_WINE_QUANTITY + " INTEGER NOT NULL, "
                + WineEntry.COLUMN_WINE_DISTRIBUTOR + " TEXT, "
                + WineEntry.COLUMN_WINE_DISTRIBUTOR_PHONE + " TEXT); ";

        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
