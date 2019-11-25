package com.example.recordtest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<NoteViewHolder> {
    ArrayList<JsonDataList> arrayList;
    Context context;
    OnClickInterface onClickInterface;

    public  ListAdapter() {}

    public ListAdapter(ArrayList<JsonDataList> arrayList, Context context, OnClickInterface onClickInterface) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClickInterface = onClickInterface;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_note_view, viewGroup, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, final int i) {
        JsonDataList currentData = arrayList.get(i);

        noteViewHolder.textTitle.setText(currentData.getTitle());
        noteViewHolder.textTime.setText(currentData.getTime());

        ////////////////get Note_Index
        noteViewHolder.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickInterface.setClick(i);
            }
        });
        noteViewHolder.textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickInterface.setClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public int getAdapterPosition(@NonNull NoteViewHolder noteViewHolder) {
        return noteViewHolder.getAdapterPosition();
    }
}
