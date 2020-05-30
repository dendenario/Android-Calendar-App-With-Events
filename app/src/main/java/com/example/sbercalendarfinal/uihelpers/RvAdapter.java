package com.example.sbercalendarfinal.uihelpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sbercalendarfinal.R;

import java.util.ArrayList;
import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;

    public RvAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ListItem listItem = listItems.get(i);

        viewHolder.textViewHead.setText(listItem.getHead());
        viewHolder.textViewDesc.setText(listItem.getDesc());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewHead;
        public TextView textViewDesc;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewDesc = itemView.findViewById(R.id.tvDesc);
            textViewHead = itemView.findViewById(R.id.tvHead);
        }
    }
}
