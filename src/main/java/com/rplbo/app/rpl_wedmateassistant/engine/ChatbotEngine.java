package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;

import java.util.ArrayList;
import java.util.List;

/**
 * Orkestrator utama chatbot WedMate Assistant.
 *
 * <p>Alur pemrosesan per pesan pengguna:</p>
 * <pre>
 *   Input teks
 *      ↓
 *   RegexMatcher.deteksiKategori()   ← built-in regex per kategori
 *      ↓
 *   RegexMatcher.cocokkan()          ← cari entri spesifik dari DB
 *      ↓
 *   ResponseGenerator.generate()    ← prioritas: DB > default > fallback
 *      ↓
 *   Pesan (bot) → disimpan ke Sesi
 * </pre>
 *
 * <p>Data knowledge base di-inject dari luar (Controller) via
 * {@link #setDaftarEntri(List)} agar engine tidak langsung bergantung pada DAO.</p>
 */
public class ChatbotEngine {

    // ── Dependensi ────────────────────────────────────────────────────────────

    private final RegexMatcher      regexMatcher;
    private final ResponseGenerator responseGenerator;

    /**
     * Cache entri knowledge base dari database.
     * Di-set oleh Controller setelah load dari {@code KnowledgeBaseDAO.findAll()}.
     */
    private List<EntriKnowledge> daftarEntri = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ChatbotEngine() {
        this.regexMatcher      = new RegexMatcher();
        this.responseGenerator = new ResponseGenerator();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Memproses satu pesan pengguna dan mengembalikan balasan dari bot.
     *
     * <ol>
     *   <li>Normalisasi teks input</li>
     *   <li>Deteksi kategori via built-in regex</li>
     *   <li>Cari entri spesifik di knowledge base DB</li>
     *   <li>Generate teks respons (DB &gt; default per kategori &gt; fallback)</li>
     *   <li>Bungkus dalam objek {@link Pesan} dan tambahkan ke {@link Sesi}</li>
     * </ol>
     *
     * @param inputPengguna teks mentah yang dikirim pengguna
     * @param sesi          sesi percakapan aktif (boleh null jika tidak di-track)
     * @return {@link Pesan} balasan dari bot, tidak pernah null
     */
    public Pesan prosesPesan(String inputPengguna, Sesi sesi) {
        // ── 1. Guard: input kosong ────────────────────────────────────────────
        if (inputPengguna == null || inputPengguna.isBlank()) {
            return buatPesanBot("Silakan ketik pertanyaan Anda 😊", sesi);
        }

        // ── 2. Deteksi kategori via built-in regex ────────────────────────────
        Kategori kategori = regexMatcher.deteksiKategori(inputPengguna);

        // ── 3. Cari entri spesifik di knowledge base DB ───────────────────────
        //    Coba cocokkan pola pertanyaan dari entri DB
        EntriKnowledge entriDB = regexMatcher.cocokkan(inputPengguna, daftarEntri);

        //    Jika tidak cocok pola per-pertanyaan, cari berdasarkan kategori
        if (entriDB == null && kategori != Kategori.TIDAK_DIKENAL) {
            entriDB = regexMatcher.cariPerKategori(kategori, daftarEntri);
        }

        // ── 4. Generate respons ───────────────────────────────────────────────
        //    Prioritas: jawaban DB > default kategori > fallback
        String teksRespons = responseGenerator.generate(entriDB, kategori);

        // ── 5. Log kategori untuk debugging ──────────────────────────────────
        System.out.printf("[ChatbotEngine] Input: \"%s\" → Kategori: %s | Entri DB: %s%n",
                inputPengguna,
                kategori,
                entriDB != null ? "id=" + entriDB.getId() : "null");

        // ── 6. Buat dan kembalikan Pesan bot ──────────────────────────────────
        return buatPesanBot(teksRespons, sesi);
    }

    /**
     * Menginjeksikan daftar entri knowledge base dari database.
     * Dipanggil oleh {@code ChatController} setelah data berhasil dimuat dari DAO.
     *
     * @param daftarEntri daftar {@link EntriKnowledge} yang aktif
     */
    public void setDaftarEntri(List<EntriKnowledge> daftarEntri) {
        this.daftarEntri = (daftarEntri != null) ? daftarEntri : new ArrayList<>();
        System.out.println("[ChatbotEngine] Knowledge base dimuat: "
                + this.daftarEntri.size() + " entri.");
    }

    /** Mengembalikan jumlah entri knowledge base yang saat ini di-cache. */
    public int getJumlahEntri() { return daftarEntri.size(); }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Membuat objek {@link Pesan} dari teks bot dan menambahkannya ke sesi aktif.
     *
     * @param teks teks balasan bot
     * @param sesi sesi percakapan (boleh null)
     * @return objek Pesan yang sudah dibuat
     */
    private Pesan buatPesanBot(String teks, Sesi sesi) {
        Pesan pesan = new Pesan();
        pesan.setIsiPesan(teks);
        pesan.setDariBot(true);

        if (sesi != null) {
            pesan.setSesiId(sesi.getId());
            sesi.getDaftarPesan().add(pesan);
        }

        return pesan;
    }
}
