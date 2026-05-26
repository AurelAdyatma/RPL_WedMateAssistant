package com.rplbo.app.rpl_wedmateassistant.model;

// Import LocalDateTime untuk menangani tipe data waktu (tanggal dan jam)
import java.time.LocalDateTime;
// Import ArrayList dan List untuk koleksi data
import java.util.ArrayList;
import java.util.List;

/**
 * Merepresentasikan satu pesan dalam percakapan (baik dari user maupun bot).
 */
public class Pesan {
    /** ID unik pesan dalam database */
    private int           id;
    
    /** ID sesi percakapan tempat pesan ini berada */
    private int           sesiId;
    
    /** Isi teks dari pesan (baik input user maupun respon bot) */
    private String        isiPesan;
    
    /** Status pengirim: true jika dikirim oleh bot, false jika dari user */
    private boolean       dariBot;      // true = pesan dari bot, false = dari user
    
    /** Waktu kapan pesan dikirim (diset otomatis saat objek dibuat) */
    private LocalDateTime waktuKirim;
    
    /** Daftar data gambar (BLOB) yang mungkin dilampirkan pada pesan (contoh: foto busana) */
    private List<byte[]>  imageDataList = new ArrayList<>();

    /**
     * Daftar pasangan detail-item (teks + gambar opsional) untuk mode detail busana.
     * Bila tidak kosong, controller akan merender setiap item secara berurutan
     * sehingga setiap foto tampil tepat di bawah teks detail busana yang bersesuaian.
     */
    private List<DetailItem> detailItems = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * Constructor default tanpa parameter. 
     * Otomatis mengatur waktuKirim dengan waktu saat ini.
     */
    public Pesan() {
        this.waktuKirim = LocalDateTime.now();
    }

    /**
     * Constructor lengkap untuk membuat pesan baru.
     * Otomatis mengatur waktuKirim dengan waktu saat ini.
     *
     * @param sesiId   ID sesi percakapan
     * @param isiPesan Isi teks pesan
     * @param dariBot  Apakah pesan ini dari bot (true) atau user (false)
     */
    public Pesan(int sesiId, String isiPesan, boolean dariBot) {
        this.sesiId    = sesiId;
        this.isiPesan  = isiPesan;
        this.dariBot   = dariBot;
        this.waktuKirim = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengambil ID pesan */
    public int getId()                       { return id; }
    /** Mengatur ID pesan */
    public void setId(int id)                { this.id = id; }

    /** Mengambil ID sesi tempat pesan berada */
    public int getSesiId()                   { return sesiId; }
    /** Mengatur ID sesi tempat pesan berada */
    public void setSesiId(int sesiId)        { this.sesiId = sesiId; }

    /** Mengambil teks isi pesan */
    public String getIsiPesan()                    { return isiPesan; }
    /** Mengatur teks isi pesan */
    public void   setIsiPesan(String isiPesan)     { this.isiPesan = isiPesan; }

    /** Mengecek apakah pesan dari bot */
    public boolean isDariBot()               { return dariBot; }
    /** Mengatur status pengirim (true=bot, false=user) */
    public void    setDariBot(boolean b)     { this.dariBot = b; }

    /** Mengambil waktu pesan dikirim */
    public LocalDateTime getWaktuKirim()                     { return waktuKirim; }
    /** Mengatur waktu pesan dikirim */
    public void          setWaktuKirim(LocalDateTime w)      { this.waktuKirim = w; }

    /** Mengambil list byte[] gambar yang dilampirkan */
    public List<byte[]>  getImageDataList()                      { return imageDataList; }
    /** Mengatur list byte[] gambar yang dilampirkan, inisialisasi kosong jika null */
    public void          setImageDataList(List<byte[]> data)     { this.imageDataList = data != null ? data : new ArrayList<>(); }
    /** Menambahkan satu gambar baru (byte[]) ke dalam list lampiran */
    public void          addImageData(byte[] data)               { if (data != null && data.length > 0) imageDataList.add(data); }
    /** Mengecek apakah pesan ini memiliki gambar lampiran */
    public boolean       hasImages()                             { return imageDataList != null && !imageDataList.isEmpty(); }

    /** Mengambil list pasangan detail-item (teks + gambar bersesuaian) */
    public List<DetailItem> getDetailItems()                          { return detailItems; }
    /** Mengatur list pasangan detail-item */
    public void             setDetailItems(List<DetailItem> items)    { this.detailItems = items != null ? items : new ArrayList<>(); }
    /** Menambahkan satu pasangan detail-item ke dalam list */
    public void             addDetailItem(DetailItem item)            { if (item != null) detailItems.add(item); }
    /** Mengecek apakah pesan ini memiliki detail-item berstruktur */
    public boolean          hasDetailItems()                          { return detailItems != null && !detailItems.isEmpty(); }

    /**
     * Menghasilkan representasi string pesan untuk kebutuhan debugging.
     *
     * @return String pesan dengan prefix [BOT] atau [USER]
     */
    @Override
    public String toString() {
        return (dariBot ? "[BOT] " : "[USER] ") + isiPesan;
    }
}
