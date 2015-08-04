package com.masum.locationnote;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

        initializeEditor(savedInstanceState);
    }

    public void initializeEditor(Bundle savedInstanceState) {

        // set current date.
        mTvDate = (EditText) findViewById(R.id.etEditorDate);

/*        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = Integer.toString(day)+"/"+Integer.toString(month)+"/"+Integer.toString(year);
        mTvDate.setText(date);*/

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
            // new note
            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DATE);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            String date = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);
            mTvDate.setText(date);
        }
        /*else {
            fillData(noteUri);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.editor_save) {
            //saveNote();
            //startActivity(new Intent(this, NoteRecyclerViewActivity.class));
            setResult(RESULT_OK);
            startActivity(new Intent(this, NoteListActivity.class));
            //finish();
            return true;
        }

        if (id == R.id.editor_cancel) {
            // Just go beck to the previous activity
            //setResult(RESULT_OK);
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
/*
        if (uri == null) {
            // It is new note. So set current date in date EditText
            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DATE);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            String date = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);
            mTvDate.setText(date);
            return;
        }
*/
        String[] projection = {NoteTable.COLUMN_TITLE,
                NoteTable.COLUMN_DESCRIPTION, NoteTable.COLUMN_DATE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();

            mTvDate.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DATE)));
            Log.i(MainActivity.TAG, "found date: " + cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DATE)));

            mEtTile.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));
            Log.i(MainActivity.TAG, "found title: " + cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_TITLE)));

            mEtDescription.setText(cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION)));
            Log.i(MainActivity.TAG, "found description: " + cursor.getString(cursor.
                    getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION)));

            // always close the cursor
            cursor.close();
        }

    }

    private void saveNote() {
        String date = mTvDate.getText().toString();
        String title = mEtTile.getText().toString();
        String description = mEtDescription.getText().toString();

        Log.i(MainActivity.TAG, "title:" + title);
        Log.i(MainActivity.TAG, "description:" + description);
        // only save if either summary or description is available
        if (description.length() == 0 && title.length() == 0) {
            Log.i(MainActivity.TAG, "just retrun coz 0 len.");
            return;
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
