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

    /** ID unik entri (primary key dari database, auto-increment) */
    private int     id;
    /** Pola regex atau kata kunci untuk mengenali intent pengguna (pertanyaan) */
    private String  pertanyaan;   // pola regex untuk mengenali intent pengguna
    /** Teks respons yang dikembalikan oleh chatbot jika input cocok dengan pertanyaan */
    private String  jawaban;      // teks respons yang dikembalikan bot
    /** Kategori/topik pembicaraan, mis: GREETING, HARGA_PAKET, RESERVASI, dll. */
    private String  kategori;     // mis: GREETING, HARGA_PAKET, RESERVASI, dll.
    /** Status aktif entri. Hanya entri yang aktif (true) yang diproses oleh ChatbotEngine */
    private boolean aktif;        // hanya entri aktif yang diproses chatbot

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Constructor default tanpa parameter (diperlukan untuk mapping dari ResultSet) */
    public EntriKnowledge() {}

    /**
     * Constructor lengkap dengan semua atribut.
     *
     * @param id          ID unik entri knowledge
     * @param pertanyaan  pola regex/keyword yang dicari di input pengguna
     * @param jawaban     teks balasan dari chatbot
     * @param kategori    kategori intent dari pertanyaan ini
     * @param aktif       status apakah entri ini aktif untuk dicocokkan
     */
    public EntriKnowledge(int id, String pertanyaan, String jawaban,
                          String kategori, boolean aktif) {
        this.id         = id;
        this.pertanyaan = pertanyaan;
        this.jawaban    = jawaban;
        this.kategori   = kategori;
        this.aktif      = aktif;
    }

    /** 
     * Constructor ringkas tanpa ID (untuk insert baru). 
     * Mengatur status 'aktif' menjadi true secara default.
     * 
     * @param pertanyaan pola regex/keyword pertanyaan
     * @param jawaban teks balasan chatbot
     * @param kategori kategori intent
     */
    /** Constructor ringkas tanpa ID (untuk insert baru). */
    public EntriKnowledge(String pertanyaan, String jawaban, String kategori) {
        this.pertanyaan = pertanyaan;
        this.jawaban    = jawaban;
        this.kategori   = kategori;
        this.aktif      = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengembalikan ID unik entri */
    public int getId()                   { return id; }
    /** Mengatur ID unik entri */
    public void setId(int id)            { this.id = id; }

    /** Mengembalikan pola regex/keyword pertanyaan */
    public String getPertanyaan()                    { return pertanyaan; }
    /** Mengatur pola regex/keyword pertanyaan */
    public void   setPertanyaan(String pertanyaan)   { this.pertanyaan = pertanyaan; }

    /** Mengembalikan teks respons chatbot */
    public String getJawaban()                 { return jawaban; }
    /** Mengatur teks respons chatbot */
    public void   setJawaban(String jawaban)   { this.jawaban = jawaban; }

    /** Mengembalikan kategori/topik obrolan */
    public String getKategori()                  { return kategori; }
    /** Mengatur kategori/topik obrolan */
    public void   setKategori(String kategori)   { this.kategori = kategori; }

    /** Mengecek apakah entri ini aktif untuk digunakan */
    public boolean isAktif()               { return aktif; }
    /** Mengatur status aktif/nonaktif entri ini */
    public void    setAktif(boolean aktif) { this.aktif = aktif; }

    // ── toString ──────────────────────────────────────────────────────────────

    /**
     * Menghasilkan representasi string dari objek EntriKnowledge untuk debugging.
     *
     * @return String representasi EntriKnowledge
     */
    @Override
    public String toString() {
        return "EntriKnowledge{id=" + id +
               ", kategori='" + kategori + '\'' +
               ", pertanyaan='" + pertanyaan + '\'' +
               ", aktif=" + aktif + '}';
    }
}
