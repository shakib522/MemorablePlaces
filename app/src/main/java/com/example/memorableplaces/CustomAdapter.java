package com.example.memorableplaces;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<MapModel> {

    final private Context context;
    final private ArrayList<MapModel>arrayList;
    public CustomAdapter(@NonNull Context context, ArrayList<MapModel> arrayList) {
        super(context, R.layout.sample_layout,arrayList);
        this.context=context;
        this.arrayList=arrayList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView==null){
            LayoutInflater layoutInflater=LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.sample_layout,parent,false);
        }
        TextView title=convertView.findViewById(R.id.textViewId);
        title.setText(arrayList.get(position).getAddress());
        return convertView;
    }
}
