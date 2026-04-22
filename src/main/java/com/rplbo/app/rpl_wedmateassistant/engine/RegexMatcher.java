package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Mencocokkan input pengguna dengan pola regex untuk mengenali intent
 * (kategori).
 *
 * <h3>Strategi dua lapis:</h3>
 * <ol>
 * <li><b>Built-in rules</b> — pola regex hardcoded per {@link Kategori}.
 * Cepat, tidak butuh DB, cocok untuk kata kunci umum.</li>
 * <li><b>Dynamic rules dari DB</b> — jika built-in tidak cocok, coba cocokkan
 * dengan field {@code pertanyaan} dari setiap {@link EntriKnowledge}
 * yang diambil {@code KnowledgeBaseDAO.findAll()}.</li>
 * </ol>
 *
 * <p>
 * Input selalu dinormalisasi (lowercase + hapus tanda baca) sebelum dicocokkan.
 * Tanda hubung (-) dikonversi ke spasi agar "pre-wedding" → "pre wedding"
 * tetap bisa dicocokkan dengan pola yang menggunakan variasi penulisan.
 * </p>
 *
 * <p>
 * Pattern DB di-cache dalam {@link ConcurrentHashMap} untuk menghindari
 * recompile berulang pada setiap panggilan {@link #cocokkan}.
 * </p>
 */
public class RegexMatcher {

    // ── Enum Kategori ─────────────────────────────────────────────────────────

    /**
     * Daftar kategori intent yang dikenali chatbot beserta pola regex-nya.
     * Pola bersifat case-insensitive dan sudah di-precompile untuk performa.
     */
    public enum Kategori {

        /** Pengguna meminta panduan / daftar perintah yang tersedia. */
        BANTUAN(
                "\\b(bantuan|help|panduan|menu|perintah|bisa\\s+apa|apa\\s+saja|fitur|info|informasi)\\b"),

        /** Sapaan awal pengguna. */
        GREETING(
                "\\b(halo|hai|hi|hei|helo|hello|hey|selamat\\s+(pagi|siang|sore|malam)" +
                "|pagi|siang|sore|malam|apa\\s+kabar|good\\s+(morning|afternoon|evening|night))\\b"),

        /** Pengguna ingin melihat koleksi busana / gaun / jas. */
        LIHAT_BUSANA(
                "\\b(busana|gaun|jas|baju|pakaian|koleksi|lihat|tampilkan|tunjukkan|show|kategori|pilihan)\\b"),

        /** Pengguna bertanya soal harga, biaya, tarif, atau paket. */
        HARGA_PAKET(
                "\\b(harga|biaya|tarif|paket|sewa|berapa|cost|bayar|budget|cicilan|dp|diskon|promo)\\b"),

        /** Pengguna ingin mengecek ketersediaan stok atau tanggal. */
        CEK_KETERSEDIAAN(
                "\\b(tersedia|ada\\s+(stok|busana|gaun)|stok|stock|tanggal|kapan|available" +
                "|ketersediaan|ready|cek\\s+(tanggal|busana|gaun)|habis|penuh)\\b"),

        /** Pengguna mencari info lokasi / jam operasional toko. */
        INFO_TOKO(
                "\\b(alamat|lokasi|jam|buka|tutup|toko|store|operasional|dimana|where|telpon|telepon" +
                "|nomor|kontak|contact|whatsapp|wa|hubungi|kunjungi|cabang)\\b"),

        /** Pengguna ingin melakukan pemesanan / booking. */
        RESERVASI(
                "\\b(pesan|booking|reservasi|book|order|daftar|minta|ingin\\s+sewa|mau\\s+sewa" +
                "|cara\\s+(pesan|booking|reservasi|sewa)|proses\\s+(pesan|sewa)|langkah)\\b"),

        // ── Sub-kategori Busana ───────────────────────────────────────────────
        // Catatan: diperiksa SETELAH kategori utama agar lebih kontekstual.
        // Angka nomor urut hanya dikenali jika diawali kata "pilih", "nomor",
        // "no", "#", "kategori", atau berdiri sendiri setelah kata "busana".

        /** Detail Busana Modern. */
        BUSANA_MODERN(
                "\\b(modern|kontemporer|kekinian|terkini)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*1\\b"),

        /** Detail Busana Tradisional. */
        BUSANA_TRADISIONAL(
                "\\b(tradisional|adat|daerah|kebaya|beskap|batik|blangkon|songket|siger|suntiang)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*2\\b"),

        /** Detail Busana Muslim/Islami. */
        BUSANA_MUSLIM(
                "\\b(muslim|muslimah|islami|syaree|syari|gamis|kopiah|hijab|islami|religius)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*3\\b"),

        /** Detail Busana Internasional / Etnik. */
        BUSANA_INTERNASIONAL(
                "\\b(internasional|etnik|mancanegara|hanbok|kimono|cheongsam|qipao|saree|lehenga|korea|jepang|tiongkok|india)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*4\\b"),

        /** Detail Busana Bertema. */
        BUSANA_BERTEMA(
                "\\b(bertema|themed|theme|vintage|retro|bohemian|garden\\s*party|beach\\s*wedding|royal|fairytale|rustic)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*5\\b"),

        /** Detail Busana Pre-Wedding. */
        BUSANA_PREWEDDING(
                "\\b(pre\\s*wedding|prewedding|prawedding|foto|sesi\\s*foto|couple|kasual|outdoor|indoor)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*6\\b"),

        /** Detail Busana Keluarga. */
        BUSANA_KELUARGA(
                "\\b(keluarga|pendamping|orang\\s*tua|pagar\\s*(ayu|bagus)|flower\\s*girl|boy|mc|wo|bridesmaid|groomsman)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*7\\b"),

        /** Detail Busana Semi-Formal / Pesta. */
        BUSANA_PESTA(
                "\\b(pesta|formal|semi\\s*formal|cocktail|midi\\s*dress|blazer|kemeja\\s*batik|gala|dinner)\\b" +
                "|(?:pilih|nomor|no|#|kategori)\\s*8\\b"),

        /** Busana Pria. */
        BUSANA_PRIA(
                "\\b(pria|laki|cowok|jas|beskap|tuxedo)\\b"),

        /** Busana Wanita. */
        BUSANA_WANITA(
                "\\b(wanita|perempuan|cewek|gaun|kebaya|dress|hanbok|kimono|cheongsam)\\b"),

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
         * @param inputNormal teks lowercase yang sudah dinormalisasi
         * @return {@code true} jika ada kecocokan
         */
        public boolean cocok(String inputNormal) {
            return pattern != null && pattern.matcher(inputNormal).find();
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    // ── Cache pattern DB ──────────────────────────────────────────────────────

    /**
     * Cache compiled {@link Pattern} untuk pola dari entri database.
     * Key = string regex mentah, Value = Pattern yang sudah di-compile.
     * Menggunakan ConcurrentHashMap agar aman jika diakses dari multi-thread.
     */
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * Pola regex dinamis yang dihasilkan secara otomatis dari nama-nama pakaian 
     * di database. Key = Kategori, Value = Pattern.
     */
    private final Map<Kategori, Pattern> dynamicPatterns = new ConcurrentHashMap<>();

    /**
     * Mengupdate pola dinamis berdasarkan daftar pakaian dari database.
     * Mengambil kata-kata unik dari nama pakaian untuk dijadikan keyword.
     */
    public void updateDynamicPatterns(List<com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding> pakaian) {
        if (pakaian == null) return;
        dynamicPatterns.clear();

        Map<Kategori, StringBuilder> keywordsMap = new HashMap<>();
        for (com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding p : pakaian) {
            String namaPakaian = normalisasi(p.getNama());
            String[] kataNama = namaPakaian.split("\\s+");
            
            Kategori kat = switch(p.getKategori().toLowerCase()) {
                case "modern" -> Kategori.BUSANA_MODERN;
                case "tradisional" -> Kategori.BUSANA_TRADISIONAL;
                case "muslim" -> Kategori.BUSANA_MUSLIM;
                case "internasional" -> Kategori.BUSANA_INTERNASIONAL;
                case "bertema" -> Kategori.BUSANA_BERTEMA;
                case "pre-wedding" -> Kategori.BUSANA_PREWEDDING;
                case "keluarga" -> Kategori.BUSANA_KELUARGA;
                case "pesta" -> Kategori.BUSANA_PESTA;
                default -> null;
            };

            if (kat != null) {
                StringBuilder sb = keywordsMap.computeIfAbsent(kat, k -> new StringBuilder());
                for (String w : kataNama) {
                    // Hindari kata umum dan nama kategori utama agar tidak overlap (karena sudah di-handle oleh built-in regex)
                    if (w.length() > 3 && !w.matches("baju|gaun|adat|pakaian|jas|sewa|untuk|modern|tradisional|muslim|muslimah|internasional|bertema|keluarga|pesta|prewedding|wedding|pengantin")) {
                        if (sb.length() > 0) sb.append("|");
                        sb.append(w);
                    }
                }
            }
        }

        for (Map.Entry<Kategori, StringBuilder> entry : keywordsMap.entrySet()) {
            if (entry.getValue().length() > 0) {
                String regex = "\\b(" + entry.getValue().toString() + ")\\b";
                dynamicPatterns.put(entry.getKey(), Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
                System.out.println("[RegexMatcher] Learned keywords for " + entry.getKey() + ": " + regex);
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Mendeteksi kategori intent dari teks pengguna menggunakan built-in regex.
     *
     * <p>
     * Urutan prioritas deteksi:
     * </p>
     * <ol>
     * <li>BANTUAN — perintah bantu / menu</li>
     * <li>GREETING — sapaan</li>
     * <li>RESERVASI — pemesanan</li>
     * <li>CEK_KETERSEDIAAN — cek stok/tanggal</li>
     * <li>HARGA_PAKET — harga & biaya</li>
     * <li>INFO_TOKO — lokasi & jam operasional</li>
     * <li>LIHAT_BUSANA — koleksi busana (umum)</li>
     * <li>Sub-kategori busana (MODERN, TRADISIONAL, MUSLIM, dst.)</li>
     * </ol>
     * Jika tidak ada yang cocok, dikembalikan {@link Kategori#TIDAK_DIKENAL}.
     *
     * @param inputPengguna teks mentah dari pengguna (belum dinormalisasi)
     * @return {@link Kategori} yang terdeteksi
     */
    public Kategori deteksiKategori(String inputPengguna) {
        String normal = normalisasi(inputPengguna);

        // ── Prioritas 1: Perintah bantuan ────────────────────────────────────
        if (Kategori.BANTUAN.cocok(normal))           return Kategori.BANTUAN;

        // ── Prioritas 2: Sapaan ───────────────────────────────────────────────
        if (Kategori.GREETING.cocok(normal))          return Kategori.GREETING;

        // ── Prioritas 3: Transaksi / Intent Tinggi ───────────────────────────
        if (Kategori.RESERVASI.cocok(normal))         return Kategori.RESERVASI;
        if (Kategori.CEK_KETERSEDIAAN.cocok(normal))  return Kategori.CEK_KETERSEDIAAN;
        if (Kategori.HARGA_PAKET.cocok(normal))       return Kategori.HARGA_PAKET;
        if (Kategori.INFO_TOKO.cocok(normal))         return Kategori.INFO_TOKO;

        // ── Prioritas 4: Sub-kategori busana (Sangat Spesifik) ───────────────
        // A. Cek built-in pattern (Kata kunci utama yang paling akurat)
        if (Kategori.BUSANA_PRIA.cocok(normal))           return Kategori.BUSANA_PRIA;
        if (Kategori.BUSANA_WANITA.cocok(normal))         return Kategori.BUSANA_WANITA;
        
        if (Kategori.BUSANA_TRADISIONAL.cocok(normal))    return Kategori.BUSANA_TRADISIONAL;
        if (Kategori.BUSANA_MUSLIM.cocok(normal))         return Kategori.BUSANA_MUSLIM;
        if (Kategori.BUSANA_INTERNASIONAL.cocok(normal))  return Kategori.BUSANA_INTERNASIONAL;
        if (Kategori.BUSANA_BERTEMA.cocok(normal))        return Kategori.BUSANA_BERTEMA;
        if (Kategori.BUSANA_PREWEDDING.cocok(normal))     return Kategori.BUSANA_PREWEDDING;
        if (Kategori.BUSANA_KELUARGA.cocok(normal))       return Kategori.BUSANA_KELUARGA;
        if (Kategori.BUSANA_PESTA.cocok(normal))          return Kategori.BUSANA_PESTA;
        if (Kategori.BUSANA_MODERN.cocok(normal))         return Kategori.BUSANA_MODERN;

        // B. Cek pola dinamis (Dari nama-nama spesifik pakaian di database)
        Kategori[] subKategori = {
            Kategori.BUSANA_TRADISIONAL, Kategori.BUSANA_MUSLIM, Kategori.BUSANA_INTERNASIONAL,
            Kategori.BUSANA_BERTEMA, Kategori.BUSANA_PREWEDDING, Kategori.BUSANA_KELUARGA,
            Kategori.BUSANA_PESTA, Kategori.BUSANA_MODERN
        };
        for (Kategori k : subKategori) {
            Pattern dp = dynamicPatterns.get(k);
            if (dp != null && dp.matcher(normal).find()) return k;
        }

        // ── Prioritas 5: Koleksi busana (Umum) ───────────────────────────────
        // Jika tidak masuk sub-kategori tapi mengandung kata "baju/gaun"
        if (Kategori.LIHAT_BUSANA.cocok(normal))      return Kategori.LIHAT_BUSANA;

        return Kategori.TIDAK_DIKENAL;
    }

    /**
     * Mencocokkan input dengan daftar entri knowledge base dari database.
     *
     * <p>
     * Digunakan sebagai fallback setelah built-in regex tidak menemukan
     * kecocokan, atau untuk mencari respons yang lebih spesifik berdasarkan
     * kategori yang sudah terdeteksi.
     * </p>
     *
     * <p>
     * Hanya entri dengan {@code aktif = true} yang dipertimbangkan.
     * Pattern DB di-cache agar tidak recompile setiap kali dipanggil.
     * Jika lebih dari satu cocok, dikembalikan entri pertama yang ditemukan.
     * </p>
     *
     * @param inputPengguna teks mentah dari pengguna
     * @param daftarEntri   semua entri knowledge base (dari
     *                      {@code KnowledgeBaseDAO.findAll()})
     * @return {@link EntriKnowledge} yang paling cocok, atau {@code null} jika
     *         tidak ada
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
                // Gunakan cache untuk menghindari recompile
                Pattern p = patternCache.computeIfAbsent(pola, key ->
                        Pattern.compile(key, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));

                if (p.matcher(normal).find()) {
                    return entri;
                }
            } catch (Exception e) {
                // Pola regex tidak valid — lewati entri ini tanpa crash
                System.err.println("[RegexMatcher] Pola regex tidak valid (id="
                        + entri.getId() + "): " + e.getMessage());
                // Hapus dari cache agar tidak disimpan pattern buruk
                patternCache.remove(pola);
            }
        }

        return null; // Tidak ada yang cocok
    }

    /**
     * Mencari entri yang kategorinya sesuai dengan hasil deteksi built-in,
     * lalu kembalikan entri pertama yang cocok kategori tersebut dari DB.
     *
     * <p>
     * Berguna saat {@link ResponseGenerator} membutuhkan teks jawaban dinamis
     * dari DB berdasarkan kategori (bukan pola per-pertanyaan).
     * </p>
     *
     * @param kategori    kategori yang sudah terdeteksi
     * @param daftarEntri semua entri dari DB
     * @return entri DB dengan kategori yang sama, atau {@code null}
     */
    public EntriKnowledge cariPerKategori(Kategori kategori,
            List<EntriKnowledge> daftarEntri) {
        if (kategori == Kategori.TIDAK_DIKENAL || daftarEntri == null)
            return null;

        String namaKategori = kategori.name(); // mis. "GREETING"
        for (EntriKnowledge entri : daftarEntri) {
            if (entri.isAktif()
                    && namaKategori.equalsIgnoreCase(entri.getKategori())) {
                return entri;
            }
        }
        return null;
    }

    /**
     * Menghapus cache compiled pattern DB.
     * Panggil ini ketika knowledge base di-reload dari database agar pattern
     * yang sudah tidak valid tidak tersimpan.
     */
    public void bersihkanCache() {
        patternCache.clear();
        System.out.println("[RegexMatcher] Pattern cache dibersihkan.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Menormalisasi teks: lowercase, konversi tanda hubung ke spasi,
     * hapus karakter non-alfanumerik lainnya, kompres spasi.
     *
     * <p>
     * Contoh: "Pre-Wedding & Foto" → "pre wedding foto"
     * </p>
     *
     * @param teks teks mentah
     * @return teks yang sudah dinormalisasi
     */
    public String normalisasi(String teks) {
        if (teks == null) return "";
        return teks.toLowerCase()
                .replaceAll("[-/]", " ")        // tanda hubung & slash → spasi
                .replaceAll("[^a-z0-9\\s#]", " ") // hapus karakter lain (pertahankan #)
                .replaceAll("\\s+", " ")         // kompres spasi ganda
                .trim();
    }
}
