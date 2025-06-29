package com.example.veteranrecommendationcanteen;

import com.google.firebase.Timestamp;

public class ReviewItem {
    // Data dari dokumen 'ulasan'
    private String komentar;
    private double rating;
    private Timestamp waktu;
    private String userId;

    // Data dari dokumen 'users' yang akan digabungkan
    private String userName;
    private String userProfileUrl;

    public ReviewItem() {}

    // Getters
    public String getKomentar() { return komentar; }
    public double getRating() { return rating; }
    public Timestamp getWaktu() { return waktu; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserProfileUrl() { return userProfileUrl; }

    // Setters
    public void setKomentar(String komentar) { this.komentar = komentar; }
    public void setRating(double rating) { this.rating = rating; }
    public void setWaktu(Timestamp waktu) { this.waktu = waktu; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }
}