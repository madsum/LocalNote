package com.masum.locationnote;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    // private Cursor cursor;
    private SimpleCursorAdapter adapter;

    private android.support.v7.widget.Toolbar toolbar;

    //private android.widget.Toolbar toolbar;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        listView = (ListView) findViewById(R.id.note_list);
        listView.setDividerHeight(2);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(this);


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                Uri uri = Uri.parse(NoteContentProvider.CONTENT_URI + "/"
                        + info.id);
                Log.i(MainActivity.TAG, "Info.id: "+info.id);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    private void discardListItem(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Uri uri = Uri.parse(NoteContentProvider.CONTENT_URI + "/"
                + info.id);
        getContentResolver().delete(uri, null, null);
        fillData();
    }

    private void fillData() {

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[]{NoteTable.COLUMN_TITLE};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.tvTitle};
        Log.i(MainActivity.TAG, "NoteListActivity: getLoaderManager().initLoader called!");
        getLoaderManager().initLoader(0, null, this);
        //getSupportLoaderManager().initLoader(0, null, this);
        //android.support.v4.app.getSu

        adapter = new SimpleCursorAdapter(this, R.layout.note_list_item, null, from,
                to, 0);

        listView.setAdapter(adapter);
    }

    // creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {NoteTable.COLUMN_ID, NoteTable.COLUMN_TITLE};
        CursorLoader cursorLoader = new CursorLoader(this,
                NoteContentProvider.CONTENT_URI, projection, null, null, null);
        Log.i(MainActivity.TAG, "NoteListActivity: onCreateLoader called!");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        Log.i(MainActivity.TAG, "NoteListActivity: onLoadFinished called!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
        Log.i(MainActivity.TAG, "NoteListActivity: onLoaderReset called!");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       //TextView title = (TextView) parent.findViewById(R.id.tvTitle);
        //TextView tempTv = (TextView) view;
       // Log.i(MainActivity.TAG, "ttiel: "+tempTv.getText());
        Intent i = new Intent(this, NoteEditor.class);
        Uri todoUri = Uri.parse(NoteContentProvider.CONTENT_URI + "/" + id);
        i.putExtra(NoteContentProvider.CONTENT_ITEM_TYPE, todoUri);
        startActivity(i);
    }
}
