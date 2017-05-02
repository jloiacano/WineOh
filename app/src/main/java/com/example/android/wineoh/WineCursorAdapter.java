package com.example.android.wineoh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wineoh.data.WineContract.WineEntry;

import java.io.ByteArrayInputStream;

/**
 * Created by J on 4/9/2017.
 * <p>
 * The wine cursor adapter to populate and handle list_item.xml views
 */

class WineCursorAdapter extends CursorAdapter {


    WineCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.wine_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ImageView wineImageView = (ImageView) view.findViewById(R.id.list_item_image);
        TextView wineName = (TextView) view.findViewById(R.id.list_item_name);
        TextView wineWinery = (TextView) view.findViewById(R.id.list_item_winery);
        TextView wineVarietal = (TextView) view.findViewById(R.id.list_item_varietal);
        TextView wineyYear = (TextView) view.findViewById(R.id.list_item_year);
        TextView winePrice = (TextView) view.findViewById(R.id.list_item_price);
        final TextView wineQuantity = (TextView) view.findViewById(R.id.list_item_quantity);
        ImageView soldOneButton = (ImageView) view.findViewById(R.id.list_item_button_sell_one);

        final byte[] imageBytesArray = cursor.getBlob(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_PHOTO));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_NAME));
        final String winery = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_WINERY));
        final String varietal = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_VARIETAL));
        final String year = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_YEAR));
        final String cost = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_COST));
        final String price = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_PRICE));
        final String quantity = cursor.getString(cursor.getColumnIndexOrThrow(WineEntry.COLUMN_WINE_QUANTITY));

        ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytesArray);
        Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);


        wineImageView.setImageBitmap(imageBitmap);
        wineName.setText(name);
        wineWinery.setText(winery);
        wineVarietal.setText(varietal);
        wineyYear.setText(year);
        String winePriceSetter = context.getString(R.string.list_item_currency) + price;
        winePrice.setText(winePriceSetter);
        wineQuantity.setText(quantity);

        // Sets the onClickListener on the image button for the individual item in the ListView
        soldOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialQuantity = Integer.valueOf(wineQuantity.getText().toString().trim());
                int updatedQuantity = initialQuantity;
                if (initialQuantity >= 1) {
                    updatedQuantity = initialQuantity - 1;
                    String wineQuantitySetter = Integer.toString(updatedQuantity);
                    wineQuantity.setText(wineQuantitySetter);
                } else {
                    String addOrRemoveError = context.getString(R.string.details_add_or_remove_error);
                    Toast.makeText(context, addOrRemoveError, Toast.LENGTH_SHORT).show();
                }

                ContentValues quantityCV = new ContentValues();
                quantityCV.put(WineEntry.COLUMN_WINE_PHOTO, imageBytesArray);
                quantityCV.put(WineEntry.COLUMN_WINE_NAME, name);
                quantityCV.put(WineEntry.COLUMN_WINE_WINERY, winery);
                quantityCV.put(WineEntry.COLUMN_WINE_VARIETAL, varietal);
                quantityCV.put(WineEntry.COLUMN_WINE_YEAR, year);
                quantityCV.put(WineEntry.COLUMN_WINE_COST, cost);
                quantityCV.put(WineEntry.COLUMN_WINE_PRICE, price);
                quantityCV.put(WineEntry.COLUMN_WINE_QUANTITY, updatedQuantity);

                context.getContentResolver().update(WineEntry.CONTENT_URI, quantityCV, null, null);


            }
        });
    }
}
