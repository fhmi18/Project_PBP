package com.example.veteranrecommendationcanteen;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.List;

/**
 * POJO class for mapping a Canteen document from Firestore.
 */
public class CanteenItem {
    // Fields that match the document fields in Firestore
    private String nama_kantin;
    private String lokasi_spesifik;
    private String jam_operasional;
    private double rating_keseluruhan;
    private String pemilik;
    private String lokasi_url;
    private List<String> promo_id;
    private int jumlah_like;

    private String campusId;
    private String canteenId;

    public CanteenItem() {}

    // --- Getters for Firestore properties ---
    @PropertyName("nama_kantin")
    public String getNama_kantin() {
        return nama_kantin;
    }

    @PropertyName("lokasi_spesifik")
    public String getLokasi_spesifik() {
        return lokasi_spesifik;
    }

    @PropertyName("jam_operasional")
    public String getJam_operasional() {
        return jam_operasional;
    }

    @PropertyName("rating_keseluruhan")
    public double getRating_keseluruhan() {
        return rating_keseluruhan;
    }

    @PropertyName("pemilik")
    public String getPemilik() {
        return pemilik;
    }

    @PropertyName("lokasi_url")
    public String getLokasi_url() {
        return lokasi_url;
    }

    @PropertyName("promo_id")
    public List<String> getPromo_id() {
        return promo_id;
    }

    @PropertyName("jumlah_like")
    public int getJumlah_like() {
        return jumlah_like;
    }

    // --- Setters for Firestore properties ---
    @PropertyName("nama_kantin")
    public void setNama_kantin(String nama_kantin) {
        this.nama_kantin = nama_kantin;
    }

    @PropertyName("lokasi_spesifik")
    public void setLokasi_spesifik(String lokasi_spesifik) {
        this.lokasi_spesifik = lokasi_spesifik;
    }

    @PropertyName("jam_operasional")
    public void setJam_operasional(String jam_operasional) {
        this.jam_operasional = jam_operasional;
    }

    @PropertyName("rating_keseluruhan")
    public void setRating_keseluruhan(double rating_keseluruhan) {
        this.rating_keseluruhan = rating_keseluruhan;
    }

    @PropertyName("pemilik")
    public void setPemilik(String pemilik) {
        this.pemilik = pemilik;
    }

    @PropertyName("lokasi_url")
    public void setLokasi_url(String lokasi_url) {
        this.lokasi_url = lokasi_url;
    }

    @PropertyName("promo_id")
    public void setPromo_id(List<String> promo_id) {
        this.promo_id = promo_id;
    }

    @PropertyName("jumlah_like")
    public void setJumlah_like(int jumlah_like) {
        this.jumlah_like = jumlah_like;
    }

    // --- Getters and Setters for local IDs (Excluded from Firestore) ---
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
}