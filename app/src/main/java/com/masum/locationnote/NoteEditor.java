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
import com.masum.utils.Utility;

import java.util.Calendar;

public class NoteEditor extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private EditText mEtTile;
    private EditText mEtDescription;
    private EditText mEtAddress;
    private TextView mTvDate;
    private TextView mTvImageName;
    private Uri noteUri;
    private double mLongitude = 0;
    private double mLatitude = 0;
    private String mImagePath = null;
    private int mNoteTableUid = 0;
    PopupWindow mPopupWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Editor");
        getSupportActionBar().setIcon(R.drawable.ic_action_app_bar_icon);
        // init all view as required
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
                mLongitude = extras.getDouble(NoteTable.COLUMN_LONGITUDE);
                mLatitude = extras.getDouble(NoteTable.COLUMN_LATITUDE);
                // Set address
                mEtAddress.setText(extras.getString(NoteTable.COLUMN_ADDRESS));
                mImagePath = extras.getString(NoteTable.COLUMN_IMAGE);
                if (mImagePath != null) {
                    mTvImageName.setText(getImageName(mImagePath));
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

    String getImageName(String imagePath) {
        if (imagePath != null) {
            String parts[] = imagePath.split("/");
            return parts[parts.length - 1];
        }
        return null;
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
            saveNote();
            setResult(RESULT_OK);
            startActivity(new Intent(this, NoteListActivity.class));
            finish();
            return true;
        }

        if (id == R.id.editor_delete) {
            if (mNoteTableUid > 0) {
                Uri uri = Uri.parse(NoteContentProvider.CONTENT_URI + "/" + mNoteTableUid);
                getContentResolver().delete(uri, null, null);
                setResult(RESULT_OK);
                startActivity(new Intent(this, NoteListActivity.class));
                finish();
            } else {
                // nothing to delte from database. Just return to previous activity.
                finish();
            }
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
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveNote();
    }

    private void fillData(Uri uri) {
        String[] projection = {NoteTable.COLUMN_ID, NoteTable.COLUMN_TITLE,
                NoteTable.COLUMN_DESCRIPTION, NoteTable.COLUMN_DATE,
                NoteTable.COLUMN_ADDRESS, NoteTable.COLUMN_IMAGE};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null,
                    null);
        } catch (Exception e) {
            Log.e(MapsActivity.TAG, "ex: " + e.getMessage());
        }

        if (cursor != null) {
            cursor.moveToFirst();
            mNoteTableUid = cursor.getInt(cursor.getColumnIndex(NoteTable.COLUMN_ID));
            mTvDate.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DATE)));
            mEtTile.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));
            mEtDescription.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION)));
            mEtAddress.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_ADDRESS)));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(NoteTable.COLUMN_IMAGE));
            if (imagePath != null) {
                mImagePath = imagePath;
                mTvImageName.setText(getImageName(imagePath));
                mTvImageName.setVisibility(View.VISIBLE);
            } //else {
            // Toast.makeText(this, "No image found for this note", Toast.LENGTH_LONG).show();
            //}
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
        values.put(NoteTable.COLUMN_IMAGE, mImagePath);
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
        mPopupWindow = new PopupWindow(popupView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // Example: If you have a TextView inside `popup_layout.xml`
        ImageView imageView = (ImageView) popupView.findViewById(R.id.popupImageView);
        if (mImagePath != null) {
            if (Utility.fileExist(mImagePath)) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
            } else {
                Toast.makeText(this, "Image not found!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Image not found!", Toast.LENGTH_LONG).show();
        }
        // If the PopupWindow should be focusable
        mPopupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];
        // Get the View's(the one that was clicked in the Fragment) location
        view.getLocationOnScreen(location);
        // Using location, the PopupWindow will be displayed right under anchorView
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
                location[0], location[1] + view.getHeight());
    }
}
