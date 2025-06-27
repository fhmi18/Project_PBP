package com.example.veteranrecommendationcanteen;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class MenuItem {

    // Properti/variabel yang sudah ada
    public String name;
    public String canteenName;
    public String leastInfo;
    public String description;
    public int price;
    public int discount;
    public int priceReduction;
    public int totalVisitor;
    public int totalFavorite;
    public double rating;
    public String fotoUrl;
    public int imageResId;

    // Field BARU untuk menyimpan ID dokumen Firestore
    private String menuId;

    // Konstruktor kosong (wajib untuk Firestore)
    public MenuItem() {}

    // Konstruktor yang sudah ada bisa tetap digunakan
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

    // Getter dan Setter untuk ID dokumen
    @Exclude // Mencegah Firebase menyimpan field ini
    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    // --- GETTERS (digunakan oleh Adapter) ---
    @PropertyName("nama_menu")
    public String getName() { return name; }

    @PropertyName("harga")
    public int getPrice() { return price; }

    @PropertyName("foto_url")
    public String getFotoUrl() { return fotoUrl; }

    @PropertyName("deskripsi")
    public String getDescription() { return description; }

    @PropertyName("rating_menu")
    public double getRating() { return rating; }

    // --- SETTERS (digunakan oleh Firestore untuk memetakan data) ---
    @PropertyName("nama_menu")
    public void setName(String name) { this.name = name; }

    @PropertyName("harga")
    public void setPrice(int price) { this.price = price; }

    @PropertyName("foto_url")
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    @PropertyName("deskripsi")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("rating_menu")
    public void setRating(double rating) { this.rating = rating; }
}
