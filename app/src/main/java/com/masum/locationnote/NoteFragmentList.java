package com.masum.locationnote;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by masum on 12/07/15.
 */
public class NoteFragmentList extends ListFragment {

    private List<NoteListItem> noteListItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteListItems = new ArrayList<NoteListItem>();

        //noteListItems.add(new NoteListItem());
    }
}
