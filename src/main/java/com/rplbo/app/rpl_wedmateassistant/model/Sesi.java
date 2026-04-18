package com.rplbo.app.rpl_wedmateassistant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Merepresentasikan satu sesi percakapan antara pengguna dan chatbot.
 */
public class Sesi {
    private int           id;
    private int           userId;
    private LocalDateTime waktuMulai;
    private LocalDateTime waktuSelesai;
    private List<Pesan>   daftarPesan;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Sesi() {
        this.waktuMulai  = LocalDateTime.now();
        this.daftarPesan = new ArrayList<>();
    }

    public Sesi(int userId) {
        this();
        this.userId = userId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getUserId()                   { return userId; }
    public void setUserId(int userId)        { this.userId = userId; }

    public LocalDateTime getWaktuMulai()                       { return waktuMulai; }
    public void          setWaktuMulai(LocalDateTime w)        { this.waktuMulai = w; }

    public LocalDateTime getWaktuSelesai()                     { return waktuSelesai; }
    public void          setWaktuSelesai(LocalDateTime w)      { this.waktuSelesai = w; }

    public List<Pesan> getDaftarPesan()                        { return daftarPesan; }
    public void        setDaftarPesan(List<Pesan> list)        { this.daftarPesan = list; }

    /** Menambahkan satu pesan ke sesi ini. */
    public void tambahPesan(Pesan pesan) {
        if (daftarPesan == null) daftarPesan = new ArrayList<>();
        daftarPesan.add(pesan);
    }

    /** Menutup sesi dengan mencatat waktu selesai. */
    public void tutupSesi() {
        this.waktuSelesai = LocalDateTime.now();
    }
}
