package com.rplbo.app.rpl_wedmateassistant.model;

import java.time.LocalDateTime;

/**
 * Merepresentasikan satu pesan dalam percakapan (baik dari user maupun bot).
 */
public class Pesan {
    private int           id;
    private int           sesiId;
    private String        isiPesan;
    private boolean       dariBot;      // true = pesan dari bot, false = dari user
    private LocalDateTime waktuKirim;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Pesan() {
        this.waktuKirim = LocalDateTime.now();
    }

    public Pesan(int sesiId, String isiPesan, boolean dariBot) {
        this.sesiId    = sesiId;
        this.isiPesan  = isiPesan;
        this.dariBot   = dariBot;
        this.waktuKirim = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public int getSesiId()                   { return sesiId; }
    public void setSesiId(int sesiId)        { this.sesiId = sesiId; }

    public String getIsiPesan()                    { return isiPesan; }
    public void   setIsiPesan(String isiPesan)     { this.isiPesan = isiPesan; }

    public boolean isDariBot()               { return dariBot; }
    public void    setDariBot(boolean b)     { this.dariBot = b; }

    public LocalDateTime getWaktuKirim()                     { return waktuKirim; }
    public void          setWaktuKirim(LocalDateTime w)      { this.waktuKirim = w; }

    @Override
    public String toString() {
        return (dariBot ? "[BOT] " : "[USER] ") + isiPesan;
    }
}
