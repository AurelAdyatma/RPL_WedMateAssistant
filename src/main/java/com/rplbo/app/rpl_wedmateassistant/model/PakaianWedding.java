package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Merepresentasikan item pakaian pernikahan yang tersedia untuk disewa.
 */
public class PakaianWedding {
    private int id;
    private String nama;
    private String kategori;      // misalnya: Gaun Pengantin, Jas, Kebaya, dll.
    private String deskripsi;
    private double hargaSewa;
    private String ukuranTersedia;
    private String imagePath;
    private String gender;
    private boolean tersedia;

    public PakaianWedding() {}

    public PakaianWedding(int id, String nama, String kategori, String deskripsi, double hargaSewa, String ukuranTersedia, String imagePath, String gender, boolean tersedia) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.hargaSewa = hargaSewa;
        this.ukuranTersedia = ukuranTersedia;
        this.imagePath = imagePath;
        this.gender = gender;
        this.tersedia = tersedia;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public double getHargaSewa() { return hargaSewa; }
    public void setHargaSewa(double hargaSewa) { this.hargaSewa = hargaSewa; }
    public String getUkuranTersedia() { return ukuranTersedia; }
    public void setUkuranTersedia(String ukuranTersedia) { this.ukuranTersedia = ukuranTersedia; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public boolean isTersedia() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
}
