package com.masum.locationnote;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.masum.adapter.NoteItem;
import com.masum.adapter.NoteRecyclerAdapter;
import com.masum.contentprovider.NoteContentProvider;
import com.masum.database.NoteTable;

import java.util.Collections;
import java.util.List;

public class NoteRecyclerViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private NoteRecyclerAdapter adapter;
    private List<NoteItem> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_recycler_view);
        notes = Collections.emptyList();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        Log.i(MainActivity.TAG, "NoteRecyclerView onCreate. ");

        getSupportLoaderManager().initLoader(0, null, this);

        setTitle("List Note");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_recycler_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {NoteTable.COLUMN_DATE, NoteTable.COLUMN_TITLE, NoteTable.COLUMN_DESCRIPTION};
        android.support.v4.content.CursorLoader cursorLoader = new android.support.v4.content.
                CursorLoader(this, NoteContentProvider.CONTENT_URI, projection, null, null, null);

        Log.i(MainActivity.TAG, "NoteRecyclerViewActivity: onCreateLoader called!");
        return cursorLoader;
    }

    //@Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        notes.clear();
        //adapter.
        Log.i(MainActivity.TAG, "NoteRecyclerViewActivity: onLoadFinished called!");
        if(cursor != null){
            Log.i(MainActivity.TAG, "NoteRecyclerViewActivity: onLoadFinished cursor not null!");
        }else {
            Log.i(MainActivity.TAG, "NoteRecyclerViewActivity: onLoadFinished cursor is null!");
        }
        while (cursor != null){


            try {
                String date =  cursor.getString(cursor.
                        getColumnIndexOrThrow(NoteTable.COLUMN_DATE));
                String title =  cursor.getString(cursor.
                        getColumnIndexOrThrow(NoteTable.COLUMN_TITLE));
                String description =  cursor.getString(cursor.
                        getColumnIndexOrThrow(NoteTable.COLUMN_DESCRIPTION));
                notes.add(new NoteItem(date, title, description));
                cursor.moveToFirst();

            }catch (Exception e){
                Log.i(MainActivity.TAG, "###ex: "+e.getMessage());
            }

        }
        adapter =  new NoteRecyclerAdapter(this, notes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(MainActivity.TAG, "NoteRecyclerViewActivity: onLoaderReset called!");
        adapter.clearNotes();
        adapter.notifyDataSetChanged();
    }
}


