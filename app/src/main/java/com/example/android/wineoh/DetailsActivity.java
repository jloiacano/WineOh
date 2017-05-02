package com.example.android.wineoh;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wineoh.data.WineContract.WineEntry;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by J on 4/9/2017.
 * <p>
 * The {@link DetailsActivity} is where all the action happens for the user. Edit all aspects of
 * the wine here.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private static final int EXISTING_WINE_LOADER = 1;

    private Spinner mCategorySpinner;
    private Spinner mRedVarietalSpinner;
    private Spinner mWhiteVarietalSpinner;
    private Spinner mDessertVarietalSpinner;
    private Spinner mFortifiedVarietalSpinner;
    private Spinner mCountrySpinner;
    private Spinner mStatesSpinner;
    private Spinner mBottleSizeSpinner;

    private ImageView mWinePhotoView;
    private TextView mWineNameView;
    private TextView mWineWineryView;
    private TextView mWineYearView;
    private TextView mWineCostView;
    private TextView mWinePriceView;
    private TextView mWineQuantityView;
    private TextView mWineDistributorView;
    private TextView mWinePhoneView;
    private TextView mWineDescription;

    private Bitmap mImageCaptured;

    private Uri mCurrentWineUri;


    private boolean mWineHasChanged = false;

    // To check for edits that might end up unsaved
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mWineHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Get the intent that opened the activity;
        Intent intent = getIntent();
        mCurrentWineUri = intent.getData();
        if (mCurrentWineUri == null) {
            setTitle(getString(R.string.details_activity_label_new));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
            openCameraDialog();

        } else {
            setTitle(getString(R.string.details_activity_label_edit));
            // only initialize the loader if there is a nonnull Uri
            getLoaderManager().initLoader(EXISTING_WINE_LOADER, null, this);
        }

        setupAllTheSpinners();

        // Find all relevant views that we will need to read user input from
        mWinePhotoView = (ImageView) findViewById(R.id.details_wine_image);
        mWineNameView = (TextView) findViewById(R.id.details_wine_name);
        mWineWineryView = (TextView) findViewById(R.id.details_winery);
        mWineYearView = (TextView) findViewById(R.id.details_year);
        mWineCostView = (TextView) findViewById(R.id.details_cost);
        mWinePriceView = (TextView) findViewById(R.id.details_price);
        mWineQuantityView = (TextView) findViewById(R.id.details_quantity);
        mWineDistributorView = (TextView) findViewById(R.id.details_distributor);
        mWinePhoneView = (TextView) findViewById(R.id.details_distributor_phone);
        mWineDescription = (TextView) findViewById(R.id.details_description);

        // Set up the three onClickListeners of the UI
        Button theChangePhotoButton = (Button) findViewById(R.id.details_button_change_photo);
        ImageView decreaseQuantityImage = (ImageView) findViewById(R.id.details_button_decrease_quantity);
        ImageView increaseQuantityImage = (ImageView) findViewById(R.id.details_button_increase_quantity);

        // Set the following three listeners for button clicks
        theChangePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraDialog();
            }
        });

        decreaseQuantityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOrRemoveWineInventory(-1);
            }
        });

        increaseQuantityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOrRemoveWineInventory(1);
            }
        });

        // Set touchlisteners so we know somethings been changed in case "back" is pressed
        theChangePhotoButton.setOnTouchListener(mTouchListener);
        decreaseQuantityImage.setOnTouchListener(mTouchListener);
        increaseQuantityImage.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mRedVarietalSpinner.setOnTouchListener(mTouchListener);
        mWhiteVarietalSpinner.setOnTouchListener(mTouchListener);
        mDessertVarietalSpinner.setOnTouchListener(mTouchListener);
        mFortifiedVarietalSpinner.setOnTouchListener(mTouchListener);
        mCountrySpinner.setOnTouchListener(mTouchListener);
        mStatesSpinner.setOnTouchListener(mTouchListener);
        mBottleSizeSpinner.setOnTouchListener(mTouchListener);
        mWineNameView.setOnTouchListener(mTouchListener);
        mWineWineryView.setOnTouchListener(mTouchListener);
        mWineYearView.setOnTouchListener(mTouchListener);
        mWineCostView.setOnTouchListener(mTouchListener);
        mWinePriceView.setOnTouchListener(mTouchListener);
        mWineQuantityView.setOnTouchListener(mTouchListener);
        mWineDistributorView.setOnTouchListener(mTouchListener);
        mWinePhoneView.setOnTouchListener(mTouchListener);
        mWineDescription.setOnTouchListener(mTouchListener);

    }

    // Self explanatory... it saves the wine being entered or edited.
    private void saveWine() {

        // An error code for entry validation
        int errorCode = 0;

        // CHECK FOR IMPORTANT FIELD EMPTIES
        if (mWineNameView.getText().toString().isEmpty() ||
                mWinePriceView.getText().toString().isEmpty()) {
            errorCode = 1;
        }

        // GET ALL THE ENTERED INFORMATION
        // get the image and convert it to a byte array.
        Bitmap imageBitmap;
        if (mImageCaptured != null) {
            imageBitmap = mImageCaptured;
        } else {
            Drawable drawableImage = ResourcesCompat.getDrawable(getResources(), R.drawable.empty_wine2, null);
            imageBitmap = ((BitmapDrawable) drawableImage).getBitmap();

        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] cvPhoto = stream.toByteArray();

        // and the dependant spinners
        String cvCategory = mCategorySpinner.getSelectedItem().toString();
        if (cvCategory.equals(R.string.category_choose)) {
            cvCategory = getString(R.string.validation_other);
        }

        String cvVarietal;
        if (cvCategory.equals(getString(R.string.category_red))) {
            cvVarietal = mRedVarietalSpinner.getSelectedItem().toString();
        } else if (cvCategory.equals(getString(R.string.category_white))) {
            cvVarietal = mWhiteVarietalSpinner.getSelectedItem().toString();
        } else if (cvCategory.equals(getString(R.string.category_fortified))) {
            cvVarietal = mFortifiedVarietalSpinner.getSelectedItem().toString();
        } else if (cvCategory.equals(getString(R.string.category_dessert))) {
            cvVarietal = mDessertVarietalSpinner.getSelectedItem().toString();
        } else {
            cvVarietal = getString(R.string.validation_other);
        }

        if (cvVarietal.equals(getString(R.string.red_choose)) ||
                cvVarietal.equals(getString(R.string.white_choose)) ||
                cvVarietal.equals(getString(R.string.fortified_choose)) ||
                cvVarietal.equals(getString(R.string.dessert_choose))) {
            cvVarietal = getString(R.string.validation_other);
        }

        String cvCountry = mCountrySpinner.getSelectedItem().toString();
        if (cvCountry.equals(R.string.countries_choose)) {
            cvCountry = getString(R.string.validation_other);
        }

        String cvState;
        if (cvCountry.equals(getString(R.string.countries_united_states))) {
            cvState = mStatesSpinner.getSelectedItem().toString();
        } else {
            cvState = getString(R.string.states_not_applicable);
        }

        // and all the strings and ints...
        String cvName = mWineNameView.getText().toString().trim();
        if (cvName.isEmpty()) {
            cvName = getString(R.string.validation_name_empty);
        }

        String cvWinery = mWineWineryView.getText().toString().trim();
        if (cvWinery.isEmpty()) {
            cvWinery = getString(R.string.validation_winery_empty);
        }

        int cvYear = 0;
        if (mWineYearView.getText().toString() != null &&
                !mWineYearView.getText().toString().isEmpty()) {
            cvYear = Integer.valueOf(mWineYearView.getText().toString());
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar currentCalendar = Calendar.getInstance(timeZone);
            if (cvYear < 0 || cvYear > currentCalendar.get(Calendar.YEAR)) {
                errorCode = 2;
            }
        }

        String cvSize = mBottleSizeSpinner.getSelectedItem().toString();
        if (cvSize.equals(getString(R.string.bottlesize_choose))) {
            cvSize = getString(R.string.validation_bottlesize_empty);
        }

        double cvCost = 0.0;
        if (mWineCostView.getText().toString() != null &&
                !mWineCostView.getText().toString().isEmpty()) {

            String theCost = mWineCostView.getText().toString().trim();
            if (theCost.contains(".")) {
                int decimalIndex = theCost.indexOf(".");
                if (theCost.length() != decimalIndex + 2) {
                    theCost = theCost.substring(0, decimalIndex + 3);
                }
            }
            cvCost = Double.valueOf(theCost);
        }
        double cvPrice = 0.0;
        if (mWinePriceView.getText().toString() != null &&
                !mWinePriceView.getText().toString().isEmpty()) {

            String thePrice = mWinePriceView.getText().toString().trim();
            if (thePrice.contains(".")) {
                int decimalIndex = thePrice.indexOf(".");
                if (thePrice.length() != decimalIndex + 2) {
                    thePrice = thePrice.substring(0, decimalIndex + 3);
                }
            }
            cvPrice = Double.valueOf(thePrice);
        }

        String cvDescription = mWineDescription.getText().toString().trim();
        if (cvDescription.isEmpty()) {
            cvDescription = getString(R.string.validation_description_empty);
        }

        int cvQuantity = 0;
        if (mWineQuantityView.getText().toString() != null &&
                !mWineQuantityView.getText().toString().isEmpty()) {
            cvQuantity = Integer.valueOf(mWineQuantityView.getText().toString().trim());
        }

        String cvDistributor = mWineDistributorView.getText().toString().trim();
        if (cvDistributor.isEmpty()) {
            cvDistributor = getString(R.string.validation_distributor_empty);
        }

        String cvPhone = validatePhoneNumber(mWinePhoneView.getText().toString().trim());
        if (cvPhone.equals("0")) {
            errorCode = 3;
        }

        // Set the ContentValues
        ContentValues currentWineValues = new ContentValues();
        currentWineValues.put(WineEntry.COLUMN_WINE_PHOTO, cvPhoto);
        currentWineValues.put(WineEntry.COLUMN_WINE_NAME, cvName);
        currentWineValues.put(WineEntry.COLUMN_WINE_WINERY, cvWinery);
        currentWineValues.put(WineEntry.COLUMN_WINE_COUNTRY, cvCountry);
        currentWineValues.put(WineEntry.COLUMN_WINE_STATE, cvState);
        currentWineValues.put(WineEntry.COLUMN_WINE_VARIETAL, cvVarietal);
        currentWineValues.put(WineEntry.COLUMN_WINE_YEAR, cvYear);
        currentWineValues.put(WineEntry.COLUMN_WINE_CATEGORY, cvCategory);
        currentWineValues.put(WineEntry.COLUMN_WINE_BOTTLE_SIZE, cvSize);
        currentWineValues.put(WineEntry.COLUMN_WINE_COST, cvCost);
        currentWineValues.put(WineEntry.COLUMN_WINE_PRICE, cvPrice);
        currentWineValues.put(WineEntry.COLUMN_WINE_DESCRIPTION, cvDescription);
        currentWineValues.put(WineEntry.COLUMN_WINE_QUANTITY, cvQuantity);
        currentWineValues.put(WineEntry.COLUMN_WINE_DISTRIBUTOR, cvDistributor);
        currentWineValues.put(WineEntry.COLUMN_WINE_DISTRIBUTOR_PHONE, cvPhone);

        // Check for error codes and do appropriate actions
        if (errorCode == 0) {
            if (mCurrentWineUri == null) {

                // take the Uri, which tells the current wine, and values and insert them into the drinkIt.db
                Uri acquiredUri = getContentResolver().insert(WineEntry.CONTENT_URI, currentWineValues);
                long newRowId = ContentUris.parseId(acquiredUri);

                if (newRowId == -1) {
                    String error = getString(R.string.save_insert_error);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                } else {
                    String succss = getString(R.string.save_insert_success);
                    Toast.makeText(getApplicationContext(), succss, Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {

                int numberOfRowsUpdated =
                        getContentResolver().update(mCurrentWineUri, currentWineValues, null, null);
                if (numberOfRowsUpdated != 0) {
                    String success = getString(R.string.save_update_success);
                    Toast.makeText(getApplicationContext(), success, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String error = getString(R.string.save_update_error);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                }
            }
        } else if (errorCode == 1) {
            String errorCode1 = getString(R.string.save_error_code_1);
            Toast.makeText(this, errorCode1, Toast.LENGTH_SHORT).show();
        } else if (errorCode == 2) {
            String errorCode2 = getString(R.string.save_error_code_2);
            Toast.makeText(this, errorCode2, Toast.LENGTH_SHORT).show();
        } else if (errorCode == 3) {
            String errorCode3 = getString(R.string.save_error_code_3);
            Toast.makeText(this, errorCode3, Toast.LENGTH_SHORT).show();
        }
    }

    // Validates that the phone number is an accurate (U.S.) phone number
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        phoneNumber = phoneNumber.replaceAll("[,;/?:@=&<>#%\\{\\}|\\^~\\[\\]`\\-]", "");

        if (phoneNumber.equals("8675309")) {
            easterEgg();
            phoneNumber = "0";
            return phoneNumber;
        }

        if (phoneNumber.length() == 11 && phoneNumber.charAt(0) == 1) {
            phoneNumber = phoneNumber.substring(1);
        }

        if (phoneNumber.length() > 10 || phoneNumber.length() < 7) {
            phoneNumber = "0";
        }

        if (phoneNumber.length() == 10) {
            String areaCode = phoneNumber.substring(0, 3);
            String prefix = phoneNumber.substring(3, 6);
            String exchange = phoneNumber.substring(6);
            phoneNumber = areaCode + getString(R.string.details_phone_delineator) +
                    prefix + getString(R.string.details_phone_delineator) +
                    exchange;
        }

        if (phoneNumber.length() == 7) {
            String prefix = phoneNumber.substring(0, 3);
            String exchange = phoneNumber.substring(3);
            phoneNumber = prefix + getString(R.string.details_phone_delineator) + exchange;
        }

        return phoneNumber;
    }

    // Because you KNOW someone is going to enter that number
    private void easterEgg() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.jenny);
        mediaPlayer.start();
    }

    // for all the places where inventory quantity can be altered
    private void addOrRemoveWineInventory(int amount) {
        int initialQuantity = Integer.valueOf(mWineQuantityView.getText().toString().trim());
        int updatedQuantity = initialQuantity + amount;
        if (updatedQuantity >= 0) {
            String updatedQuantityString = Integer.toString(updatedQuantity);
            mWineQuantityView.setText(updatedQuantityString);
        } else {
            String addOrRemoveError = getString(R.string.details_add_or_remove_error);
            Toast.makeText(this, addOrRemoveError, Toast.LENGTH_SHORT).show();
        }
    }

    // when a new wine is first initiated or the "change photo" button is clicked
    private void openCameraDialog() {
        // Set the camera to open if the user clicks OK
        DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                takeTheWinesPicture();
            }
        };
        // Set what happens if the user clicks NO
        DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String cameraToast = getString(R.string.details_camera_dialog_toast);
                Toast.makeText(DetailsActivity.this, cameraToast, Toast.LENGTH_SHORT).show();

            }
        };

        // Set the Dialog box Strings, and what OnClickListener is activated with each button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.details_camera_dialog_message);
        builder.setPositiveButton(R.string.details_camera_dialog_affirm, positive);
        builder.setNegativeButton(R.string.details_camera_dialog_deny, negative);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Lots of spinners to set up.
    private void setupAllTheSpinners() {
        // Find all the spinners
        mCategorySpinner = (Spinner) findViewById(R.id.details_wine_category);
        mRedVarietalSpinner = (Spinner) findViewById(R.id.details_red_varietal);
        mWhiteVarietalSpinner = (Spinner) findViewById(R.id.details_white_varietal);
        mDessertVarietalSpinner = (Spinner) findViewById(R.id.details_dessert_varietal);
        mFortifiedVarietalSpinner = (Spinner) findViewById(R.id.details_fortified_varietal);
        mCountrySpinner = (Spinner) findViewById(R.id.details_origin_country);
        mStatesSpinner = (Spinner) findViewById(R.id.details_origin_state);
        mBottleSizeSpinner = (Spinner) findViewById(R.id.details_bottle_size);

        // Set up the category spinner
        ArrayAdapter categorySpinerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);
        categorySpinerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mCategorySpinner.setAdapter(categorySpinerAdapter);

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mRedVarietalSpinner.setVisibility(View.GONE);
                mWhiteVarietalSpinner.setVisibility(View.GONE);
                mDessertVarietalSpinner.setVisibility(View.GONE);
                mFortifiedVarietalSpinner.setVisibility(View.GONE);
                String test = mCategorySpinner.getItemAtPosition(i).toString();
                if (test.equals(getString(R.string.category_red))) {
                    mRedVarietalSpinner.setVisibility(View.VISIBLE);
                } else if (test.equals(getString(R.string.category_white))) {
                    mWhiteVarietalSpinner.setVisibility(View.VISIBLE);
                } else if (test.equals(getString(R.string.category_dessert))) {
                    mDessertVarietalSpinner.setVisibility(View.VISIBLE);
                } else if (test.equals(getString(R.string.category_fortified))) {
                    mFortifiedVarietalSpinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing selected? do nothing then
            }
        });

        // Set up the red varietal spinner
        ArrayAdapter varietalRedSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_reds_options, android.R.layout.simple_spinner_item);
        varietalRedSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mRedVarietalSpinner.setAdapter(varietalRedSpinnerAdapter);

        // Set up the white varietal spinner
        ArrayAdapter varietalWhiteSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_whites_options, android.R.layout.simple_spinner_item);
        varietalWhiteSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mWhiteVarietalSpinner.setAdapter(varietalWhiteSpinnerAdapter);

        // Set up the dessert wine varietal spinner
        ArrayAdapter varietalDessertSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_desserts_options, android.R.layout.simple_spinner_item);
        varietalDessertSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mDessertVarietalSpinner.setAdapter(varietalDessertSpinnerAdapter);

        // Set up the fortified wine varietal spinner
        ArrayAdapter varietalFortifiedSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_fortifieds_options, android.R.layout.simple_spinner_item);
        varietalFortifiedSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mFortifiedVarietalSpinner.setAdapter(varietalFortifiedSpinnerAdapter);

        // Set up the top 72 wine producing countries spinner
        ArrayAdapter countrySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_top_wine_countries, android.R.layout.simple_spinner_item);
        countrySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mCountrySpinner.setAdapter(countrySpinnerAdapter);

        // Set up a listener in case "United States" is selected to view the states spinner
        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mCountrySpinner.getItemAtPosition(i).equals(getString(R.string.countries_united_states))) {
                    mStatesSpinner.setVisibility(View.VISIBLE);
                } else {
                    mStatesSpinner.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing selected? do nothing then
            }
        });

        // Set up the states spinner
        ArrayAdapter statesSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_us_states, android.R.layout.simple_spinner_item);
        statesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mStatesSpinner.setAdapter(statesSpinnerAdapter);

        // Set up the bottle size spinner
        ArrayAdapter bottleSizeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_bottle_sizes, android.R.layout.simple_spinner_item);
        bottleSizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mBottleSizeSpinner.setAdapter(bottleSizeSpinnerAdapter);

    }

    // If the user confirms that they do want to take a picture, from openCameraDialog()
    private void takeTheWinesPicture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Bitmap capturedImage = null;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImage);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    // Also pretty self explanatory; deletes the currently entered or edited wine from the database
    private void deleteCurrentWine() {
        int rowsDeleted = getContentResolver().delete(mCurrentWineUri, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.details_delete_fail),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.details_delete_success),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // Just in case the delete button was touched accidentally.
    private void confirmDelete() {
        DialogInterface.OnClickListener dontDoIt = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };

        DialogInterface.OnClickListener deleteAway = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteCurrentWine();
            }
        };

        AlertDialog.Builder scrapOrGoBack = new AlertDialog.Builder(this);
        scrapOrGoBack.setMessage(R.string.details_delete_message);
        scrapOrGoBack.setPositiveButton(R.string.details_delete_yes, deleteAway);
        scrapOrGoBack.setNegativeButton(R.string.details_delete_no, dontDoIt);

        AlertDialog dialog = scrapOrGoBack.create();
        dialog.show();
    }

    // An alert dialog in case the user forgot to save before hitting back or home
    private void showUnsavedChangesDialog() {
        DialogInterface.OnClickListener scrapIt = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        DialogInterface.OnClickListener saveEm = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveWine();
            }
        };

        DialogInterface.OnClickListener goBackToIt = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };

        AlertDialog.Builder scrapOrGoBack = new AlertDialog.Builder(this);
        scrapOrGoBack.setMessage(R.string.details_unsaved_scrap_or_go_back);
        scrapOrGoBack.setNegativeButton(R.string.details_unsaved_scrap, scrapIt);
        scrapOrGoBack.setPositiveButton(R.string.details_save_it, saveEm);
        scrapOrGoBack.setNeutralButton(R.string.details_unsaved_go_back, goBackToIt);

        AlertDialog dialog = scrapOrGoBack.create();
        dialog.setTitle(getString(R.string.details_unsaved_title));
        dialog.show();
    }

    // Opens the phone to the distributors number as long as there's a phoneNumber
    private void orderMoreWine() {
        String phoneNumber = mWinePhoneView.getText().toString().trim();
        String zero = getString(R.string.details_phone_zeroed);
        if (!phoneNumber.isEmpty() || !phoneNumber.equals(zero)) {
            Intent callTheDistributor = new Intent(Intent.ACTION_DIAL);
            callTheDistributor.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callTheDistributor);
        } else {
            String noNumber = getString(R.string.details_order_more_no_phone);
            Toast.makeText(this, noNumber, Toast.LENGTH_SHORT).show();
        }
    }

    // For onLoadFinished() so that all the spinners can be properly set to the right index
    private int getCorrectIndex(Spinner detail, String value) {

        int index = 0;

        for (int i = 0; i < detail.getCount(); i++) {
            if (detail.getItemAtPosition(i).equals(value)) {
                index = i;
            }
        }
        return index;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

            ImageView detailsImage = (ImageView) findViewById(R.id.details_wine_image);
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mImageCaptured = photo;
            detailsImage.setImageBitmap(photo);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.menu_action_save:
                saveWine();
                return true;

            case R.id.menu_action_add_to_inventory:
                addOrRemoveWineInventory(1);
                return true;

            case R.id.menu_action_add_a_case_to_inventory:
                addOrRemoveWineInventory(12);
                return true;

            case R.id.menu_action_remove_from_inventory:
                addOrRemoveWineInventory(-1);
                return true;

            case R.id.menu_action_remove_a_case_from_inventory:
                addOrRemoveWineInventory(-12);
                return true;

            case R.id.menu_action_delete_product_from_inventory:
                confirmDelete();
                return true;

            case R.id.menu_action_order_more_product:
                orderMoreWine();
                return true;

            case android.R.id.home:
                if (!mWineHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                WineEntry._ID,
                WineEntry.COLUMN_WINE_PHOTO,
                WineEntry.COLUMN_WINE_NAME,
                WineEntry.COLUMN_WINE_WINERY,
                WineEntry.COLUMN_WINE_COUNTRY,
                WineEntry.COLUMN_WINE_STATE,
                WineEntry.COLUMN_WINE_VARIETAL,
                WineEntry.COLUMN_WINE_YEAR,
                WineEntry.COLUMN_WINE_CATEGORY,
                WineEntry.COLUMN_WINE_BOTTLE_SIZE,
                WineEntry.COLUMN_WINE_COST,
                WineEntry.COLUMN_WINE_PRICE,
                WineEntry.COLUMN_WINE_DESCRIPTION,
                WineEntry.COLUMN_WINE_QUANTITY,
                WineEntry.COLUMN_WINE_DISTRIBUTOR,
                WineEntry.COLUMN_WINE_DISTRIBUTOR_PHONE};

        return new CursorLoader(this,
                mCurrentWineUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Get all the column indices
        if (data.moveToFirst()) {
            int imageColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_PHOTO);
            int nameColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_NAME);
            int wineryColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_WINERY);
            int countryColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_COUNTRY);
            int stateColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_STATE);
            int varietalColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_VARIETAL);
            int yearColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_YEAR);
            int categoryColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_CATEGORY);
            int sizeColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_BOTTLE_SIZE);
            int costColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_COST);
            int priceColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_PRICE);
            int descriptionColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_DESCRIPTION);
            int quantityColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_QUANTITY);
            int distributorColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_DISTRIBUTOR);
            int phoneColumnIndex = data.getColumnIndex(WineEntry.COLUMN_WINE_DISTRIBUTOR_PHONE);

            // Get all the data from each columnIndex
            byte[] image = data.getBlob(imageColumnIndex);
            String name = data.getString(nameColumnIndex);
            String winery = data.getString(wineryColumnIndex);
            String country = data.getString(countryColumnIndex);
            String state = data.getString(stateColumnIndex);
            String varietal = data.getString(varietalColumnIndex);
            int year = data.getInt(yearColumnIndex);
            String category = data.getString(categoryColumnIndex);
            String size = data.getString(sizeColumnIndex);
            int cost = data.getInt(costColumnIndex);
            int price = data.getInt(priceColumnIndex);
            String description = data.getString(descriptionColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            String distributor = data.getString(distributorColumnIndex);
            String phone = data.getString(phoneColumnIndex);

            // Set all the information from the database row into the wine details view
            mWinePhotoView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            mWineNameView.setText(name);
            mWineWineryView.setText(winery);
            mCountrySpinner.setSelection(getCorrectIndex(mCountrySpinner, country));
            mStatesSpinner.setSelection(getCorrectIndex(mStatesSpinner, state));
            if (country.equals(getString(R.string.countries_united_states))) {
                mStatesSpinner.setVisibility(View.VISIBLE);
            }
            String yearSetter = Integer.toString(year);
            mWineYearView.setText(yearSetter);

            mCategorySpinner.setSelection(getCorrectIndex(mCategorySpinner, category));
            if (category.equals(getString(R.string.category_red))) {
                mRedVarietalSpinner.setSelection(getCorrectIndex(mRedVarietalSpinner, varietal));
            } else if (category.equals(getString(R.string.category_white))) {
                mWhiteVarietalSpinner.setSelection(getCorrectIndex(mWhiteVarietalSpinner, varietal));
            } else if (category.equals(getString(R.string.category_fortified))) {
                mFortifiedVarietalSpinner.setSelection(getCorrectIndex(mFortifiedVarietalSpinner, varietal));
            } else if (category.equals(getString(R.string.category_dessert))) {
                mDessertVarietalSpinner.setSelection(getCorrectIndex(mDessertVarietalSpinner, varietal));
            }
            mBottleSizeSpinner.setSelection(getCorrectIndex(mBottleSizeSpinner, size));
            String costSetter = Integer.toString(cost);
            mWineCostView.setText(costSetter);
            String priceSetter = Integer.toString(price);
            mWinePriceView.setText(priceSetter);
            mWineDescription.setText(description);
            String quantitySetter = Integer.toString(quantity);
            mWineQuantityView.setText(quantitySetter);
            mWineDistributorView.setText(distributor);
            mWinePhoneView.setText(phone);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        if (!mWineHasChanged) {
            super.onBackPressed();
            return;
        }
        showUnsavedChangesDialog();
    }
}
