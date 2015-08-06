package com.masum.adapter;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.masum.contentprovider.NoteContentProvider;
import com.masum.database.NoteTable;
import com.masum.locationnote.MainActivity;
import com.masum.locationnote.R;

import java.util.List;

/**
 * Created by masum on 02/08/15.
 */
public class NoteRecyclerView extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private RecyclerView recyclerView;
    private NoteRecyclerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_note_recycler_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        Log.i(MainActivity.TAG, "NoteRecyclerView onCreate. ");

        getSupportLoaderManager().initLoader(0, null, this);
        setTitle("List Note");
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {NoteTable.COLUMN_ID, NoteTable.COLUMN_TITLE, NoteTable.COLUMN_DESCRIPTION};
        android.support.v4.content.CursorLoader cursorLoader = new android.support.v4.content.
                            CursorLoader(this, NoteContentProvider.CONTENT_URI, projection, null, null, null);
        // CursorLoader cursorLoader = new android.support.v4.content.Curso
        // android.content.CursorLoader cursorLoader = new CursorLoader(this,
        //  NoteContentProvider.CONTENT_URI, projection, null, null, null);
        // Log.i(TodosOverviewActivity.TAG, "TodosOverviewActivity: onCreateLoader called!");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        adapter =  new NoteRecyclerAdapter(this, (List<NoteItem>)data);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.clearNotes();
        adapter.notifyDataSetChanged();
    }
}
