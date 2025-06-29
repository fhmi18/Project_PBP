package com.example.veteranrecommendationcanteen;

public class MyReviewsItem {

    // Data dari dokumen 'ulasan'
    private String komentar;
    private double rating;

    // Data dari dokumen menu
    private String menuName;
    private String menuImageUrl;

    // Data dari dokumen kantin
    private String canteenName;

    // Data path untuk navigasi
    private String campusId, canteenId, categoryPath, menuId;

    public MyReviewsItem() {}

    // Getters
    public String getKomentar() { return komentar; }
    public double getRating() { return rating; }
    public String getMenuName() { return menuName; }
    public String getMenuImageUrl() { return menuImageUrl; }
    public String getCanteenName() { return canteenName; }
    public String getCampusId() { return campusId; }
    public String getCanteenId() { return canteenId; }
    public String getCategoryPath() { return categoryPath; }
    public String getMenuId() { return menuId; }

    // Setters
    public void setKomentar(String komentar) { this.komentar = komentar; }
    public void setRating(double rating) { this.rating = rating; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public void setMenuImageUrl(String menuImageUrl) { this.menuImageUrl = menuImageUrl; }
    public void setCanteenName(String canteenName) { this.canteenName = canteenName; }
    public void setCampusId(String campusId) { this.campusId = campusId; }
    public void setCanteenId(String canteenId) { this.canteenId = canteenId; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
}