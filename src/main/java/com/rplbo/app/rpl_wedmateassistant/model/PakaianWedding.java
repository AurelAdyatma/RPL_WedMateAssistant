package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Merepresentasikan item pakaian pernikahan yang tersedia untuk disewa.
 *
 * <p>Kelas ini memetakan data dari tabel {@code pakaian_wedding} di database SQLite.
 * Setiap objek merepresentasikan satu item pakaian dengan atribut seperti
 * nama, kategori, ukuran, harga sewa, gender, dan foto (disimpan sebagai BLOB).</p>
 *
 * <p>Kategori pakaian yang didukung:</p>
 * <ul>
 *   <li>Modern — gaun internasional, jas tuxedo</li>
 *   <li>Tradisional — kebaya, beskap, batik daerah</li>
 *   <li>Muslim — gaun muslimah, baju koko</li>
 *   <li>Internasional — hanbok, kimono, cheongsam</li>
 *   <li>Bertema — vintage, bohemian, royal</li>
 *   <li>Pre-Wedding — outfit sesi foto couple</li>
 *   <li>Keluarga — orang tua, pagar ayu/bagus</li>
 *   <li>Pesta — gaun cocktail, blazer elegan</li>
 * </ul>
 */
public class PakaianWedding {
    /** ID unik pakaian (primary key dari database, auto-increment) */
    private int id;
    /** Nama pakaian, misalnya "Gaun Ball Gown Ivory" */
    private String nama;
    /** Kategori/jenis pakaian, misalnya: Gaun Pengantin, Jas, Kebaya, dll. */
    private String kategori;      // misalnya: Gaun Pengantin, Jas, Kebaya, dll.
    /** Deskripsi detail pakaian (bahan, detail, kecocokan acara, dll.) */
    private String deskripsi;
    /** Harga sewa per hari dalam Rupiah */
    private double hargaSewa;
    /** Ukuran yang tersedia, dipisahkan koma (misalnya "S,M,L,XL") */
    private String ukuranTersedia;
    /** Data foto pakaian dalam format byte array (BLOB dari database) */
    private byte[] imageData;     // foto disimpan sebagai BLOB di database
    /** Gender target pakaian: "Pria", "Wanita", atau "Unisex" */
    private String gender;
    /** Status ketersediaan pakaian (true = tersedia untuk disewa, false = sedang disewa) */
    private boolean tersedia;

    /** Constructor default tanpa parameter (diperlukan untuk mapping dari ResultSet) */
    public PakaianWedding() {}

    /**
     * Constructor lengkap dengan semua atribut.
     *
     * @param id              ID unik pakaian
     * @param nama            nama pakaian
     * @param kategori        kategori/jenis pakaian
     * @param deskripsi       deskripsi detail pakaian
     * @param hargaSewa       harga sewa per hari (Rupiah)
     * @param ukuranTersedia  ukuran yang tersedia (contoh: "S,M,L")
     * @param imageData       data foto dalam byte array (BLOB), boleh null
     * @param gender          gender target ("Pria", "Wanita", "Unisex")
     * @param tersedia        status ketersediaan
     */
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

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengembalikan ID unik pakaian */
    public int getId() { return id; }
    /** Mengatur ID unik pakaian */
    public void setId(int id) { this.id = id; }
    /** Mengembalikan nama pakaian */
    public String getNama() { return nama; }
    /** Mengatur nama pakaian */
    public void setNama(String nama) { this.nama = nama; }
    /** Mengembalikan kategori/jenis pakaian */
    public String getKategori() { return kategori; }
    /** Mengatur kategori/jenis pakaian */
    public void setKategori(String kategori) { this.kategori = kategori; }
    /** Mengembalikan deskripsi detail pakaian */
    public String getDeskripsi() { return deskripsi; }
    /** Mengatur deskripsi detail pakaian */
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    /** Mengembalikan harga sewa per hari dalam Rupiah */
    public double getHargaSewa() { return hargaSewa; }
    /** Mengatur harga sewa per hari dalam Rupiah */
    public void setHargaSewa(double hargaSewa) { this.hargaSewa = hargaSewa; }
    /** Mengembalikan daftar ukuran yang tersedia (contoh: "S,M,L") */
    public String getUkuranTersedia() { return ukuranTersedia; }
    /** Mengatur daftar ukuran yang tersedia */
    public void setUkuranTersedia(String ukuranTersedia) { this.ukuranTersedia = ukuranTersedia; }
    /** Mengembalikan data foto sebagai byte array (BLOB) */
    public byte[] getImageData() { return imageData; }
    /** Mengatur data foto sebagai byte array (BLOB) */
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    /** Mengembalikan gender target pakaian */
    public String getGender() { return gender; }
    /** Mengatur gender target pakaian */
    public void setGender(String gender) { this.gender = gender; }
    /** Mengecek apakah pakaian tersedia untuk disewa */
    public boolean isTersedia() { return tersedia; }
    /** Mengatur status ketersediaan pakaian */
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
    /** Mengecek apakah pakaian ini memiliki foto (byte array tidak null dan tidak kosong) */
    public boolean hasImage() { return imageData != null && imageData.length > 0; }
}
