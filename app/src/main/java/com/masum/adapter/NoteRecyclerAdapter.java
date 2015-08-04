package com.masum.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.masum.locationnote.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by masum on 02/08/15.
 */
public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteViewHolder> {

    private List<NoteItem> notes = Collections.emptyList();
    private Context context;
    private LayoutInflater inflater;

    public NoteRecyclerAdapter(Context context, List<NoteItem> notes){
        this.context = context;
        this.notes = notes;
        inflater = LayoutInflater.from(context);
    }

    public void clearNotes(){
        notes.clear();
    }

    public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // view is the root view of of custom_row.xml meaning view is the RelativeLayout
        View view = inflater.inflate(R.layout.note_list_item, viewGroup, false);
        final NoteViewHolder holder = new NoteViewHolder(view);
        //Log.i(MainActivity.TAG, "onCreateViewHolder called.");
        return holder;
    }

    @Override
    public void onBindViewHolder(NoteViewHolder noteViewHolder, int position) {
        NoteItem currentItem = notes.get(position);
        noteViewHolder.tvTile.setText(currentItem.title);
        noteViewHolder.tvDescription.setText(currentItem.description);
        noteViewHolder.tvData.setText(currentItem.date);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView tvTile;
        TextView tvDescription;
        TextView tvData;

        public NoteViewHolder(View itemView) {
            super(itemView);
            tvTile = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvData = (TextView) itemView.findViewById(R.id.tvDate);
        }
    }
}
