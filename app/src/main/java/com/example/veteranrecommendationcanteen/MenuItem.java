package com.example.veteranrecommendationcanteen;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

@IgnoreExtraProperties
public class MenuItem {

    public String name;
    public String description;
    public int price;
    public double rating;
    public List<String> gambarUrl;

    public String canteenName;
    public String leastInfo;
    public int discount;
    public int priceReduction;
    public int totalVisitor;
    public int totalLike;
    public int imageResId;

    private String menuId;
    private String campusId;
    private String canteenId;
    private String categoryPath;


    public MenuItem() {}

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
        this.totalLike = totalFavorite;
        this.rating = rating;
        this.imageResId = imageResId;
    }

    // --- Getters and Setters for local IDs (Excluded from Firestore) ---
    @Exclude
    public String getMenuId() {
        return menuId;
    }

    @Exclude
    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    @Exclude
    public String getCampusId() {
        return campusId;
    }

    @Exclude
    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    @Exclude
    public String getCanteenId() {
        return canteenId;
    }

    @Exclude
    public void setCanteenId(String canteenId) {
        this.canteenId = canteenId;
    }

    @Exclude
    public String getCategoryPath() {
        return categoryPath;
    }

    @Exclude
    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }


    // --- GETTERS for Firestore properties ---
    @PropertyName("nama_menu")
    public String getName() { return name; }

    @PropertyName("harga")
    public int getPrice() { return price; }

    @PropertyName("gambar_url")
    public List<String> getGambarUrl() {
        return gambarUrl;
    }

    @PropertyName("jumlah_like")
    public int getTotalLike() {
        return totalLike;
    }

    @PropertyName("deskripsi")
    public String getDescription() { return description; }

    @PropertyName("rating_menu")
    public double getRating() { return rating; }

    // --- SETTERS for Firestore properties ---
    @PropertyName("nama_menu")
    public void setName(String name) { this.name = name; }

    @PropertyName("harga")
    public void setPrice(int price) { this.price = price; }

    @PropertyName("gambar_url")
    public void setGambarUrl(List<String> gambarUrl) {
        this.gambarUrl = gambarUrl;
    }

    @PropertyName("jumlah_like")
    public void setTotalLike(int totalLike) {
        this.totalLike = totalLike;
    }

    @PropertyName("deskripsi")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("rating_menu")
    public void setRating(double rating) { this.rating = rating; }
}