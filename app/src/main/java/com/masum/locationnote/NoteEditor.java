package com.masum.locationnote;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.masum.contentprovider.NoteContentProvider;
import com.masum.database.NoteTable;

import java.util.Calendar;

public class NoteEditor extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private EditText mEtTile;
    private EditText mEtDescription;
    private EditText mTvDate;
    private Uri noteUri;

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
        mTvDate = (EditText) findViewById(R.id.etEditorDate);
        mEtTile = (EditText) findViewById(R.id.etEditorTilte);
        mEtDescription = (EditText) findViewById(R.id.etEditorDescription);

        Bundle extras = getIntent().getExtras();

        // check from the saved Instance
        noteUri = (savedInstanceState == null) ?
                null : (Uri) savedInstanceState.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);

        // Or passed from the other activity
        if (extras != null) {
            noteUri = extras.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);
            fillData(noteUri);
        } else {
            // a new note so we should set current date
            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DATE);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            String date = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);
            mTvDate.setText(date);
        }
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
        saveNote();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveNote();
    }

    private void fillData(Uri uri) {
        String[] projection = {NoteTable.COLUMN_TITLE,
                NoteTable.COLUMN_DESCRIPTION, NoteTable.COLUMN_DATE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();

            mTvDate.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DATE)));
            mEtTile.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));
            mEtDescription.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION)));
            // always close the cursor
            cursor.close();
        }
    }

    private void saveNote() {
        String date = mTvDate.getText().toString();
        String title = mEtTile.getText().toString();
        String description = mEtDescription.getText().toString();


        // only save if either summary or description is available
        if (description.length() == 0)  {
            description = "No description added";
        }
        if(title.length() == 0){
            title = "No titile added";
        }

        ContentValues values = new ContentValues();
        values.put(NoteTable.COLUMN_DATE, date);
        values.put(NoteTable.COLUMN_TITLE, title);
        values.put(NoteTable.COLUMN_DESCRIPTION, description);

        if (noteUri == null) {
            // New note
            noteUri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, values);
        } else {
            // Update note
            getContentResolver().update(noteUri, values, null, null);
        }
    }
}
