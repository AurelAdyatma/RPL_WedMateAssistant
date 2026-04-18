package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Mencocokkan input pengguna dengan pola regex untuk mengenali intent (kategori).
 *
 * <h3>Strategi dua lapis:</h3>
 * <ol>
 *   <li><b>Built-in rules</b> — pola regex hardcoded per {@link Kategori}.
 *       Cepat, tidak butuh DB, cocok untuk kata kunci umum.</li>
 *   <li><b>Dynamic rules dari DB</b> — jika built-in tidak cocok, coba cocokkan
 *       dengan field {@code pertanyaan} dari setiap {@link EntriKnowledge}
 *       yang diambil {@code KnowledgeBaseDAO.findAll()}.</li>
 * </ol>
 *
 * <p>Input selalu dinormalisasi (lowercase + hapus tanda baca) sebelum dicocokkan.</p>
 */
public class RegexMatcher {

    // ── Enum Kategori ─────────────────────────────────────────────────────────

    /**
     * Daftar kategori intent yang dikenali chatbot beserta pola regex-nya.
     * Pola bersifat case-insensitive dan sudah di-precompile untuk performa.
     */
    public enum Kategori {

        /** Pengguna ingin melihat koleksi busana / gaun / jas. */
        LIHAT_BUSANA(
            "\\b(busana|gaun|jas|baju|koleksi|lihat|tampilkan|show)\\b"
        ),

        /** Pengguna bertanya soal harga, biaya, tarif, atau paket. */
        HARGA_PAKET(
            "\\b(harga|biaya|tarif|paket|sewa|berapa|cost|bayar|budget)\\b"
        ),

        /** Pengguna ingin mengecek ketersediaan stok atau tanggal. */
        CEK_KETERSEDIAAN(
            "\\b(tersedia|ada|stok|stock|tanggal|kapan|available|ketersediaan)\\b"
        ),

        /** Pengguna mencari info lokasi / jam operasional toko. */
        INFO_TOKO(
            "\\b(alamat|lokasi|jam|buka|tutup|toko|store|operasional|dimana|where)\\b"
        ),

        /** Pengguna ingin melakukan pemesanan / booking. */
        RESERVASI(
            "\\b(pesan|booking|reservasi|sewa|book|order|daftar|minta)\\b"
        ),

        /** Sapaan awal pengguna. */
        GREETING(
            "\\b(halo|hai|hi|hei|helo|selamat|pagi|siang|sore|malam|hello|hey)\\b"
        ),

        /** Tidak ada kategori yang cocok. */
        TIDAK_DIKENAL(null);

        // ── Internal ──────────────────────────────────────────────────────────

        private final Pattern pattern;

        Kategori(String regex) {
            // Precompile pattern; TIDAK_DIKENAL tidak punya pola
            this.pattern = (regex != null)
                    ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                    : null;
        }

        /**
         * Memeriksa apakah teks yang sudah dinormalisasi cocok dengan kategori ini.
         *
         * @param inputNormal teks lowercase yang sudah dihapus tanda bacanya
         * @return {@code true} jika ada kecocokan
         */
        public boolean cocok(String inputNormal) {
            return pattern != null && pattern.matcher(inputNormal).find();
        }

        public Pattern getPattern() { return pattern; }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Mendeteksi kategori intent dari teks pengguna menggunakan built-in regex.
     *
     * <p>Urutan prioritas deteksi (dari atas ke bawah):</p>
     * <ol>
     *   <li>GREETING</li>
     *   <li>RESERVASI</li>
     *   <li>CEK_KETERSEDIAAN</li>
     *   <li>HARGA_PAKET</li>
     *   <li>LIHAT_BUSANA</li>
     *   <li>INFO_TOKO</li>
     * </ol>
     * Jika tidak ada yang cocok, dikembalikan {@link Kategori#TIDAK_DIKENAL}.
     *
     * @param inputPengguna teks mentah dari pengguna (belum dinormalisasi)
     * @return {@link Kategori} yang terdeteksi
     */
    public Kategori deteksiKategori(String inputPengguna) {
        String normal = normalisasi(inputPengguna);

        // Urutan penting: lebih spesifik di atas
        if (Kategori.GREETING.cocok(normal))          return Kategori.GREETING;
        if (Kategori.RESERVASI.cocok(normal))          return Kategori.RESERVASI;
        if (Kategori.CEK_KETERSEDIAAN.cocok(normal))  return Kategori.CEK_KETERSEDIAAN;
        if (Kategori.HARGA_PAKET.cocok(normal))        return Kategori.HARGA_PAKET;
        if (Kategori.LIHAT_BUSANA.cocok(normal))       return Kategori.LIHAT_BUSANA;
        if (Kategori.INFO_TOKO.cocok(normal))          return Kategori.INFO_TOKO;

        return Kategori.TIDAK_DIKENAL;
    }

    /**
     * Mencocokkan input dengan daftar entri knowledge base dari database.
     *
     * <p>Digunakan sebagai fallback setelah built-in regex tidak menemukan
     * kecocokan, atau untuk mencari respons yang lebih spesifik berdasarkan
     * kategori yang sudah terdeteksi.</p>
     *
     * <p>Hanya entri dengan {@code aktif = true} yang dipertimbangkan.
     * Jika lebih dari satu cocok, dikembalikan entri pertama yang ditemukan.</p>
     *
     * @param inputPengguna teks mentah dari pengguna
     * @param daftarEntri   semua entri knowledge base (dari {@code KnowledgeBaseDAO.findAll()})
     * @return {@link EntriKnowledge} yang paling cocok, atau {@code null} jika tidak ada
     */
    public EntriKnowledge cocokkan(String inputPengguna, List<EntriKnowledge> daftarEntri) {
        if (inputPengguna == null || inputPengguna.isBlank() || daftarEntri == null) {
            return null;
        }

        String normal = normalisasi(inputPengguna);

        for (EntriKnowledge entri : daftarEntri) {
            // Lewati entri yang tidak aktif
            if (!entri.isAktif()) continue;

            String pola = entri.getPertanyaan();
            if (pola == null || pola.isBlank()) continue;

            try {
                Pattern p = Pattern.compile(pola,
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                if (p.matcher(normal).find()) {
                    return entri;
                }
            } catch (Exception e) {
                // Pola regex tidak valid — lewati entri ini tanpa crash
                System.err.println("[RegexMatcher] Pola regex tidak valid (id="
                        + entri.getId() + "): " + e.getMessage());
            }
        }

        return null; // Tidak ada yang cocok
    }

    /**
     * Mencari entri yang kategorinya sesuai dengan hasil deteksi built-in,
     * lalu kembalikan entri pertama yang cocok kategori tersebut dari DB.
     *
     * <p>Berguna saat {@link ResponseGenerator} membutuhkan teks jawaban dinamis
     * dari DB berdasarkan kategori (bukan pola per-pertanyaan).</p>
     *
     * @param kategori    kategori yang sudah terdeteksi
     * @param daftarEntri semua entri dari DB
     * @return entri DB dengan kategori yang sama, atau {@code null}
     */
    public EntriKnowledge cariPerKategori(Kategori kategori,
                                          List<EntriKnowledge> daftarEntri) {
        if (kategori == Kategori.TIDAK_DIKENAL || daftarEntri == null) return null;

        String namaKategori = kategori.name(); // mis. "GREETING"
        for (EntriKnowledge entri : daftarEntri) {
            if (entri.isAktif()
                    && namaKategori.equalsIgnoreCase(entri.getKategori())) {
                return entri;
            }
        }
        return null;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Menormalisasi teks: lowercase, hapus tanda baca, kompres spasi.
     *
     * @param teks teks mentah
     * @return teks yang sudah dinormalisasi
     */
    public String normalisasi(String teks) {
        if (teks == null) return "";
        return teks.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", " ") // hapus tanda baca
                   .replaceAll("\\s+", " ")          // kompres spasi ganda
                   .trim();
    }
}
