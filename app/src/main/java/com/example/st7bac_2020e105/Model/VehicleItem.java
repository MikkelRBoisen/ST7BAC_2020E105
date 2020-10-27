package com.example.st7bac_2020e105.Model;

public class VehicleItem {

    private String spinnerItemName;
    private int spinnerItemImage;

    public VehicleItem(String spinnerItemName, int spinnerItemImage){
        this.spinnerItemName = spinnerItemName;
        this.spinnerItemImage = spinnerItemImage;
    }

    public int getSpinneritemImage() {
        return spinnerItemImage;
    }

    public String getSpinneritemsName() {
        return spinnerItemName;
    }
}
