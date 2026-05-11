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
    private byte[] imageData;     // foto disimpan sebagai BLOB di database
    private String gender;
    private boolean tersedia;

    public PakaianWedding() {}

    public PakaianWedding(int id, String nama, String kategori, String deskripsi, double hargaSewa, String ukuranTersedia, byte[] imageData, String gender, boolean tersedia) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.hargaSewa = hargaSewa;
        this.ukuranTersedia = ukuranTersedia;
        this.imageData = imageData;
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
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public boolean isTersedia() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
    public boolean hasImage() { return imageData != null && imageData.length > 0; }
}
