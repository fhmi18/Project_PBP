package com.example.veteranrecommendationcanteen;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Model class untuk menampung data sebuah Kantin dari Firestore.
 * Nama variabel di sini harus cocok dengan nama field di dokumen Firestore Anda.
 */
@IgnoreExtraProperties
public class CanteenItem {
    private String nama_kantin;
    private String lokasi_spesifik;
    private double rating_keseluruhan;
    private String jam_operasional;

    // Properti BARU untuk menyimpan ID (tidak diambil dari field Firestore)
    private String canteenId;
    private String campusId;

    // Konstruktor kosong wajib untuk deserialisasi data dari Firestore
    public CanteenItem() {}

    // Getters
    public String getNama_kantin() { return nama_kantin; }
    public String getLokasi_spesifik() { return lokasi_spesifik; }
    public double getRating_keseluruhan() { return rating_keseluruhan; }
    public String getJam_operasional() { return jam_operasional; }

    // Setters (juga diperlukan oleh Firestore untuk mengisi data)
    public void setNama_kantin(String nama_kantin) { this.nama_kantin = nama_kantin; }
    public void setLokasi_spesifik(String lokasi_spesifik) { this.lokasi_spesifik = lokasi_spesifik; }
    public void setRating_keseluruhan(double rating_keseluruhan) { this.rating_keseluruhan = rating_keseluruhan; }
    public void setJam_operasional(String jam_operasional) { this.jam_operasional = jam_operasional; }

    // Getter dan Setter untuk ID
    // Anotasi @Exclude mencegah Firebase mencoba menyimpan field ini kembali ke database.
    @Exclude
    public String getCanteenId() { return canteenId; }
    @Exclude
    public String getCampusId() { return campusId; }

    public void setCanteenId(String canteenId) { this.canteenId = canteenId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }
}
