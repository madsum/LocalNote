package com.masum.locationnote;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.masum.contentprovider.NoteContentProvider;
import com.masum.database.NoteTable;

import java.util.Calendar;

public class NoteEditor extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private EditText mEtTile;
    private EditText mEtDescription;
    private EditText mEtAddress;
    private TextView mTvDate;
    private TextView mTvImageName;
    private Uri noteUri;
    private float mLongitude = 0;
    private float mLatitude = 0;
    private String mImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // init all view as requied
        initializeEditor(savedInstanceState);
    }

    public void initializeEditor(Bundle savedInstanceState) {
        mTvDate = (TextView) findViewById(R.id.etEditorDate);
        mEtTile = (EditText) findViewById(R.id.etEditorTilte);
        mEtDescription = (EditText) findViewById(R.id.etEditorDescription);
        mEtAddress = (EditText) findViewById(R.id.etEditorAddress);
        setEditorBackground(mEtDescription);
        mTvImageName = (TextView) findViewById(R.id.etImageName);

        Bundle extras = getIntent().getExtras();

        // check from the saved Instance
        noteUri = (savedInstanceState == null) ?
                null : (Uri) savedInstanceState.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);

        // Or passed from the other activity
        if (extras != null) {
            noteUri = extras.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);
            if (noteUri != null) {
                // it is old note to open with uri. So fill info
                fillData(noteUri);
            } else {
                mLongitude = extras.getFloat(NoteTable.COLUMN_LONGITUDE);
                mLatitude = extras.getFloat(NoteTable.COLUMN_LATITUDE);
                // Set address
                mEtAddress.setText(extras.getString(NoteTable.COLUMN_ADDRESS));
                mImagePath = extras.getString(NoteTable.COLUMN_IMAGE);
                if (mImagePath != null) {
                    String parts[] = mImagePath.split("/");
                    Log.i(MapsActivity.TAG, "name: " + parts[parts.length - 1]);
                    // set image name. Array last element is file name
                    mTvImageName.setText(parts[parts.length - 1]);
                    mTvImageName.setVisibility(View.VISIBLE);
                }


                // a new note so we should set current date
                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DATE);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                String date = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);
                mTvDate.setText(date);
            }
        }
    }

    void setEditorBackground(EditText editText) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.blue_line_bg_png, options);
        int imageHeight = options.outHeight;
        int padding = 5;//inner line padding
        int fontSize = imageHeight - padding;//calculated font size
        editText.setTextSize(fontSize);//set calculated font size to the edit text
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.editor_save) {
            setResult(RESULT_OK);
            startActivity(new Intent(this, NoteListActivity.class));
            finish();
            return true;
        }

        if (id == R.id.editor_cancel) {
            // Just go beck to the previous activity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //saveNote();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveNote();
    }

    private void fillData(Uri uri) {
        String[] projection = {NoteTable.COLUMN_TITLE,
                NoteTable.COLUMN_DESCRIPTION, NoteTable.COLUMN_DATE,
                NoteTable.COLUMN_ADDRESS};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null,
                    null);
        } catch (Exception e) {
            Log.e(MapsActivity.TAG, "ex: " + e.getMessage());
        }

        if (cursor != null) {
            cursor.moveToFirst();

            mTvDate.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DATE)));
            mEtTile.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));
            mEtDescription.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION)));
            mEtAddress.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_ADDRESS)));
            // always close the cursor
            cursor.close();
        }
    }

    private void saveNote() {
        String date = mTvDate.getText().toString();
        String title = mEtTile.getText().toString();
        String description = mEtDescription.getText().toString();
        String address = mEtAddress.getText().toString();


        // only save if either summary or description is available
        if (description.length() == 0) {
            description = "No description added";
        }
        if (title.length() == 0) {
            title = "No titile added";
        }
        if (address.length() == 0) {
            address = "No address added";
        }

        ContentValues values = new ContentValues();
        values.put(NoteTable.COLUMN_DATE, date);
        values.put(NoteTable.COLUMN_TITLE, title);
        values.put(NoteTable.COLUMN_DESCRIPTION, description);
        values.put(NoteTable.COLUMN_ADDRESS, address);
        values.put(NoteTable.COLUMN_LATITUDE, mLatitude);
        values.put(NoteTable.COLUMN_LONGITUDE, mLongitude);

        if (noteUri == null) {
            // New note
            noteUri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, values);
        } else {
            // Update note
            getContentResolver().update(noteUri, values, null, null);
        }
    }

    public void showPopup(View view) {

        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        // Example: If you have a TextView inside `popup_layout.xml`
        ImageView imageView = (ImageView) popupView.findViewById(R.id.popupImageView);
        if (mImagePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
        }else{
            Toast.makeText(this, "Image not found!",Toast.LENGTH_LONG).show();
        }
        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        view.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
                location[0], location[1] + view.getHeight());

    }

    public void justClick(View view) {
        showPopup(view);
    }

    public void clikMe(View view) {
        Log.i("","got you");
    }
}
