package com.masum.locationnote;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.masum.contentprovider.NoteContentProvider;
import com.masum.database.NoteTable;

public class NoteListActivity extends AppCompatActivity implements
       LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final int DELETE_ID = Menu.FIRST + 1;
    private SimpleCursorAdapter mAdapter;
    private android.support.v7.widget.Toolbar mToolbar;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        mListView = (ListView) findViewById(R.id.note_list);
        mListView.setDividerHeight(1);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(this);


        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Note list");
        getSupportActionBar().setIcon(R.drawable.location_note);
        fillData();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                Uri uri = Uri.parse(NoteContentProvider.CONTENT_URI + "/" + info.id);
                getContentResolver().delete(uri, null, null);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    private void fillData() {
        String[] from = new String[]{NoteTable.COLUMN_TITLE};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.listItemTvTitle};
        Log.i(MainActivity.TAG, "NoteListActivity: getLoaderManager().initLoader called!");
        getSupportLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(this, R.layout.note_list_item, null, from, to, 0);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {NoteTable.COLUMN_ID, NoteTable.COLUMN_TITLE};
        // last parameter is how to sort query
        // column_name DESC OR column_name ASC
        android.support.v4.content.CursorLoader  cursorLoader = new android.support.v4.content.CursorLoader(this,
                NoteContentProvider.CONTENT_URI, projection, null, null, NoteTable.COLUMN_ID+" DESC");
        Log.i(MainActivity.TAG, "NoteListActivity: onCreateLoader called!");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        Log.i(MainActivity.TAG, "NoteListActivity: onLoadFinished called!");
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, NoteEditor.class);
        Uri noteUri = Uri.parse(NoteContentProvider.CONTENT_URI + "/" + id);
        intent.putExtra(NoteContentProvider.CONTENT_ITEM_TYPE, noteUri);
        startActivity(intent);
        finish();
    }
}
