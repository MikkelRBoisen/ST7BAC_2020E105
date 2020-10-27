package com.example.st7bac_2020e105.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.st7bac_2020e105.Model.VehicleItem;
import com.example.st7bac_2020e105.R;

import java.util.ArrayList;
import java.util.List;

public class VehicleItemAdapter extends ArrayAdapter<VehicleItem> {

    public VehicleItemAdapter(@NonNull Context context, ArrayList<VehicleItem> vehicleList){
        super(context,0,vehicleList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_layout,parent,false);
        }
        VehicleItem vehicle = getItem(position);
        ImageView spinnerimg = convertView.findViewById(R.id.imgSpinnerlayout);
        TextView spinnertv = convertView.findViewById(R.id.tvSpinnerLayout);
        if (vehicle != null){
            spinnerimg.setImageResource(vehicle.getSpinneritemImage());
            spinnertv.setText(vehicle.getSpinneritemsName());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_layout,parent,false);
        }
        VehicleItem vehicle = getItem(position);
        ImageView dropdownimg = convertView.findViewById(R.id.imgDropdown);
        TextView dropdowntv = convertView.findViewById(R.id.tvDropdown);
        if (vehicle != null){
            dropdownimg.setImageResource(vehicle.getSpinneritemImage());
            dropdowntv.setText(vehicle.getSpinneritemsName());
        }
        return convertView;
    }
}
