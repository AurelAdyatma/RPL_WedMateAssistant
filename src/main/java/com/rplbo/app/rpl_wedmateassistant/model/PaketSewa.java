package com.rplbo.app.rpl_wedmateassistant.model;

// Import List untuk menyimpan daftar pakaian yang termasuk dalam paket
import java.util.List;

/**
 * Merepresentasikan paket sewa pernikahan (bundel beberapa item + layanan).
 *
 * <p>Kelas ini memetakan data dari tabel {@code paket_sewa} di database SQLite.
 * Setiap paket berisi bundel layanan pernikahan dengan harga total yang
 * lebih hemat dibanding menyewa item satu per satu.</p>
 *
 * <p>Contoh paket yang tersedia:</p>
 * <ul>
 *   <li>Paket Basic — 1 busana pengantin, sewa 1 hari</li>
 *   <li>Paket Silver — busana pengantin + 2 busana keluarga, 2 hari</li>
 *   <li>Paket Gold — busana pengantin + 4 keluarga + pagar ayu, 2 hari</li>
 *   <li>Paket Platinum — all-in termasuk MUA dan konsultasi</li>
 *   <li>Paket Pre-Wedding — 2 outfit couple + aksesori foto</li>
 * </ul>
 */
public class PaketSewa {
    /** ID unik paket (primary key dari database, auto-increment) */
    private int id;
    /** Nama paket, misalnya "Paket Gold" */
    private String namaPaket;
    /** Deskripsi lengkap isi dan fasilitas paket */
    private String deskripsi;
    /** Harga total paket dalam Rupiah */
    private double hargaTotal;
    /** Durasi sewa paket dalam satuan hari */
    private int durasiSewa;           // dalam hari
    /** Daftar pakaian yang termasuk dalam paket ini */
    private List<PakaianWedding> daftarPakaian;

    /** Constructor default tanpa parameter (diperlukan untuk mapping dari ResultSet) */
    public PaketSewa() {}

    /**
     * Constructor lengkap dengan semua atribut.
     *
     * @param id              ID unik paket
     * @param namaPaket       nama paket
     * @param deskripsi       deskripsi isi paket
     * @param hargaTotal      harga total paket (Rupiah)
     * @param durasiSewa      durasi sewa (hari)
     * @param daftarPakaian   list pakaian yang termasuk dalam paket
     */
    public PaketSewa(int id, String namaPaket, String deskripsi, double hargaTotal, int durasiSewa, List<PakaianWedding> daftarPakaian) {
        this.id = id;
        this.namaPaket = namaPaket;
        this.deskripsi = deskripsi;
        this.hargaTotal = hargaTotal;
        this.durasiSewa = durasiSewa;
        this.daftarPakaian = daftarPakaian;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengembalikan ID unik paket */
    public int getId() { return id; }
    /** Mengatur ID unik paket */
    public void setId(int id) { this.id = id; }
    /** Mengembalikan nama paket */
    public String getNamaPaket() { return namaPaket; }
    /** Mengatur nama paket */
    public void setNamaPaket(String namaPaket) { this.namaPaket = namaPaket; }
    /** Mengembalikan deskripsi isi paket */
    public String getDeskripsi() { return deskripsi; }
    /** Mengatur deskripsi isi paket */
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    /** Mengembalikan harga total paket dalam Rupiah */
    public double getHargaTotal() { return hargaTotal; }
    /** Mengatur harga total paket dalam Rupiah */
    public void setHargaTotal(double hargaTotal) { this.hargaTotal = hargaTotal; }
    /** Mengembalikan durasi sewa paket (dalam hari) */
    public int getDurasiSewa() { return durasiSewa; }
    /** Mengatur durasi sewa paket (dalam hari) */
    public void setDurasiSewa(int durasiSewa) { this.durasiSewa = durasiSewa; }
    /** Mengembalikan daftar pakaian yang termasuk dalam paket */
    public List<PakaianWedding> getDaftarPakaian() { return daftarPakaian; }
    /** Mengatur daftar pakaian yang termasuk dalam paket */
    public void setDaftarPakaian(List<PakaianWedding> daftarPakaian) { this.daftarPakaian = daftarPakaian; }
}
