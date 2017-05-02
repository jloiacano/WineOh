package com.example.android.wineoh;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.wineoh.data.WineContract.WineEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int WINE_LOADER = 12;

    WineCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_catelog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openDetails = new Intent(CatalogActivity.this, DetailsActivity.class);
                startActivity(openDetails);
            }
        });

        ListView wineListView = (ListView) findViewById(R.id.list_view_of_wines);

        // Set the empty view
        View emptyView = findViewById(R.id.empty_state_layout);
        wineListView.setEmptyView(emptyView);

        mCursorAdapter = new WineCursorAdapter(this, null);
        wineListView.setAdapter(mCursorAdapter);

        // Check to see if the data in the ListView has changed, and if so, reset the Options Menu
        mCursorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                invalidateOptionsMenu();
            }
        });

        // An onItemClickListener for each Item in the ListView to send it to the proper details
        wineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent openDetailsEditor = new Intent(CatalogActivity.this, DetailsActivity.class);
                Uri selectedWineUri = ContentUris.withAppendedId(WineEntry.CONTENT_URI, l);
                openDetailsEditor.setData(selectedWineUri);
                startActivity(openDetailsEditor);
            }
        });

        getLoaderManager().initLoader(WINE_LOADER, null, this);
    }

    // In case the user accidentally touches the "DELETE ALL" menu option
    private void confirmDelete() {
        DialogInterface.OnClickListener dontDoIt = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };

        DialogInterface.OnClickListener deleteAway = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getContentResolver().delete(WineEntry.CONTENT_URI, null, null);
            }
        };


        AlertDialog.Builder scrapOrGoBack = new AlertDialog.Builder(this);
        scrapOrGoBack.setMessage(R.string.catalog_delete_message);
        scrapOrGoBack.setPositiveButton(R.string.catalog_delete_yes, deleteAway);
        scrapOrGoBack.setNegativeButton(R.string.catalog_delete_no, dontDoIt);

        AlertDialog dialog = scrapOrGoBack.create();
        dialog.setTitle(R.string.catalog_delete_title);
        dialog.show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                WineEntry._ID,
                WineEntry.COLUMN_WINE_PHOTO,
                WineEntry.COLUMN_WINE_NAME,
                WineEntry.COLUMN_WINE_WINERY,
                WineEntry.COLUMN_WINE_VARIETAL,
                WineEntry.COLUMN_WINE_YEAR,
                WineEntry.COLUMN_WINE_COST,
                WineEntry.COLUMN_WINE_PRICE,
                WineEntry.COLUMN_WINE_QUANTITY
        };

        switch (i) {
            case WINE_LOADER:
                return new CursorLoader(
                        this,
                        WineEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_delete_all);
        if (mCursorAdapter.isEmpty()) {
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add menu_catalog to the menu bar
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_all:
                confirmDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
