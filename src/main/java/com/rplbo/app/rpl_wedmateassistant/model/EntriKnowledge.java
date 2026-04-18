package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Merepresentasikan satu entri dalam knowledge base chatbot.
 *
 * <p>Field {@code pertanyaan} menyimpan pola regex yang digunakan oleh
 * {@code RegexMatcher} untuk mengenali intent pengguna.
 * Field {@code jawaban} adalah teks respons yang akan ditampilkan bot.</p>
 *
 * <p>Sesuai skema tabel {@code knowledge_base}:
 * id, pertanyaan, jawaban, kategori, aktif.</p>
 */
public class EntriKnowledge {

    private int     id;
    private String  pertanyaan;   // pola regex untuk mengenali intent pengguna
    private String  jawaban;      // teks respons yang dikembalikan bot
    private String  kategori;     // mis: GREETING, HARGA_PAKET, RESERVASI, dll.
    private boolean aktif;        // hanya entri aktif yang diproses chatbot

    // ── Constructors ──────────────────────────────────────────────────────────

    public EntriKnowledge() {}

    public EntriKnowledge(int id, String pertanyaan, String jawaban,
                          String kategori, boolean aktif) {
        this.id         = id;
        this.pertanyaan = pertanyaan;
        this.jawaban    = jawaban;
        this.kategori   = kategori;
        this.aktif      = aktif;
    }

    /** Constructor ringkas tanpa ID (untuk insert baru). */
    public EntriKnowledge(String pertanyaan, String jawaban, String kategori) {
        this.pertanyaan = pertanyaan;
        this.jawaban    = jawaban;
        this.kategori   = kategori;
        this.aktif      = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public String getPertanyaan()                    { return pertanyaan; }
    public void   setPertanyaan(String pertanyaan)   { this.pertanyaan = pertanyaan; }

    public String getJawaban()                 { return jawaban; }
    public void   setJawaban(String jawaban)   { this.jawaban = jawaban; }

    public String getKategori()                  { return kategori; }
    public void   setKategori(String kategori)   { this.kategori = kategori; }

    public boolean isAktif()               { return aktif; }
    public void    setAktif(boolean aktif) { this.aktif = aktif; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "EntriKnowledge{id=" + id +
               ", kategori='" + kategori + '\'' +
               ", pertanyaan='" + pertanyaan + '\'' +
               ", aktif=" + aktif + '}';
    }
}
