package com.rplbo.app.rpl_wedmateassistant.model;

// Import LocalDateTime untuk menangani tipe data waktu
import java.time.LocalDateTime;
// Import ArrayList dan List untuk koleksi pesan
import java.util.ArrayList;
import java.util.List;

/**
 * Merepresentasikan satu sesi percakapan antara pengguna dan chatbot.
 */
public class Sesi {
    
    /** ID unik dari sesi percakapan */
    private int           id;
    
    /** ID user yang melakukan percakapan */
    private int           userId;
    
    /** Waktu kapan sesi ini dimulai */
    private LocalDateTime waktuMulai;
    
    /** Waktu kapan sesi ini ditutup/selesai */
    private LocalDateTime waktuSelesai;
    
    /** Kumpulan riwayat pesan (baik dari user maupun bot) selama sesi ini berlangsung */
    private List<Pesan>   daftarPesan;

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * Constructor default tanpa parameter.
     * Otomatis mengeset waktu mulai dengan waktu saat ini dan 
     * menginisialisasi daftar pesan menjadi ArrayList kosong.
     */
    public Sesi() {
        this.waktuMulai  = LocalDateTime.now();
        this.daftarPesan = new ArrayList<>();
    }

    /**
     * Constructor dengan parameter userId.
     *
     * @param userId ID pengguna yang memiliki sesi ini
     */
    public Sesi(int userId) {
        this();
        this.userId = userId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengembalikan ID sesi */
    public int getId()                       { return id; }
    /** Mengatur ID sesi */
    public void setId(int id)                { this.id = id; }

    /** Mengembalikan ID user */
    public int getUserId()                   { return userId; }
    /** Mengatur ID user */
    public void setUserId(int userId)        { this.userId = userId; }

    /** Mengembalikan waktu sesi dimulai */
    public LocalDateTime getWaktuMulai()                       { return waktuMulai; }
    /** Mengatur waktu sesi dimulai */
    public void          setWaktuMulai(LocalDateTime w)        { this.waktuMulai = w; }

    /** Mengembalikan waktu sesi selesai */
    public LocalDateTime getWaktuSelesai()                     { return waktuSelesai; }
    /** Mengatur waktu sesi selesai */
    public void          setWaktuSelesai(LocalDateTime w)      { this.waktuSelesai = w; }

    /** Mengembalikan daftar riwayat pesan di sesi ini */
    public List<Pesan> getDaftarPesan()                        { return daftarPesan; }
    /** Mengatur daftar pesan secara manual */
    public void        setDaftarPesan(List<Pesan> list)        { this.daftarPesan = list; }

    /** 
     * Menambahkan satu pesan ke dalam sesi ini. 
     * Jika list daftarPesan belum diinisialisasi (null), maka akan diinisialisasi dulu.
     * 
     * @param pesan Objek pesan yang ditambahkan
     */
    public void tambahPesan(Pesan pesan) {
        if (daftarPesan == null) daftarPesan = new ArrayList<>();
        daftarPesan.add(pesan);
    }

    /** 
     * Menutup sesi dengan mencatat waktu selesai berdasarkan waktu saat metode ini dipanggil.
     */
    public void tutupSesi() {
        this.waktuSelesai = LocalDateTime.now();
    }
}
