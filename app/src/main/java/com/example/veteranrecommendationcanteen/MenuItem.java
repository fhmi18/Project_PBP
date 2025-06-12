package com.example.veteranrecommendationcanteen;

public class MenuItem {
    public String name, canteenName, leastInfo, description;
    public int price, discount, priceReduction, totalVisitor, totalFavorite;
    public double rating;
    public int imageResId;

    public MenuItem(String name, String canteenName, String leastInfo, int imageResId) {
        this.name = name;
        this.canteenName = canteenName;
        this.leastInfo = leastInfo;
        this.imageResId = imageResId;
    }

    public MenuItem(String name, int price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    public MenuItem(String name, String canteenName, String leastInfo, String description,
                    int price, int discount, int priceReduction,
                    int totalVisitor, int totalFavorite,
                    double rating, int imageResId) {
        this.name = name;
        this.canteenName = canteenName;
        this.leastInfo = leastInfo;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.priceReduction = priceReduction;
        this.totalVisitor = totalVisitor;
        this.totalFavorite = totalFavorite;
        this.rating = rating;
        this.imageResId = imageResId;
    }
}