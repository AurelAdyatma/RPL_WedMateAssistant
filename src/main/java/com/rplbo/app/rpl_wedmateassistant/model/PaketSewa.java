package com.rplbo.app.rpl_wedmateassistant.model;

import java.util.List;

/**
 * Merepresentasikan paket sewa pernikahan (bundel beberapa item + layanan).
 */
public class PaketSewa {
    private int id;
    private String namaPaket;
    private String deskripsi;
    private double hargaTotal;
    private int durasiSewa;           // dalam hari
    private List<PakaianWedding> daftarPakaian;

    public PaketSewa() {}

    public PaketSewa(int id, String namaPaket, String deskripsi, double hargaTotal, int durasiSewa, List<PakaianWedding> daftarPakaian) {
        this.id = id;
        this.namaPaket = namaPaket;
        this.deskripsi = deskripsi;
        this.hargaTotal = hargaTotal;
        this.durasiSewa = durasiSewa;
        this.daftarPakaian = daftarPakaian;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNamaPaket() { return namaPaket; }
    public void setNamaPaket(String namaPaket) { this.namaPaket = namaPaket; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public double getHargaTotal() { return hargaTotal; }
    public void setHargaTotal(double hargaTotal) { this.hargaTotal = hargaTotal; }
    public int getDurasiSewa() { return durasiSewa; }
    public void setDurasiSewa(int durasiSewa) { this.durasiSewa = durasiSewa; }
    public List<PakaianWedding> getDaftarPakaian() { return daftarPakaian; }
    public void setDaftarPakaian(List<PakaianWedding> daftarPakaian) { this.daftarPakaian = daftarPakaian; }
}
