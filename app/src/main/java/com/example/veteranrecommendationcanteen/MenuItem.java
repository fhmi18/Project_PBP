package com.example.veteranrecommendationcanteen;

public class MenuItem {
    public String name;
    public String location;
    public String priceInfo;
    public int imageResId;

    public MenuItem(String name, String location, String priceInfo, int imageResId) {
        this.name = name;
        this.location = location;
        this.priceInfo = priceInfo;
        this.imageResId = imageResId;
    }
}
