package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;

import java.util.EnumMap;
import java.util.Map;

/**
 * Menghasilkan teks respons chatbot WedMate Assistant.
 *
 * <h3>Dua sumber respons:</h3>
 * <ol>
 * <li><b>Default per kategori</b> — teks statis yang selalu tersedia
 * meskipun database kosong ({@link #generate(Kategori)}).</li>
 * <li><b>Dinamis dari knowledge base DB</b> — teks {@code jawaban} yang
 * diambil dari {@link EntriKnowledge} ({@link #generate(EntriKnowledge)}).</li>
 * </ol>
 *
 * <p>
 * Jika tidak ada yang cocok sama sekali, dikembalikan {@link #FALLBACK}.
 * </p>
 *
 * <p>
 * {@link EnumMap} digunakan sebagai pengganti {@link java.util.HashMap}
 * untuk performa dan keamanan tipe yang lebih baik.
 * </p>
 */
public class ResponseGenerator {

    // ── Pesan Fallback ────────────────────────────────────────────────────────

    /**
     * Respons ketika tidak ada kategori maupun entri DB yang cocok.
     */
    public static final String FALLBACK =
            "Maaf, saya belum bisa memahami pertanyaan Anda.\n\n" +
                    "Coba tanyakan salah satu dari ini:\n" +
                    "  - Koleksi busana & gaun\n" +
                    "  - Harga & paket sewa\n" +
                    "  - Ketersediaan pakaian\n" +
                    "  - Cara reservasi\n" +
                    "  - Info & lokasi toko\n\n" +
                    "Atau ketik 'bantuan' untuk melihat semua perintah yang tersedia.";

    // ── Respons Default per Kategori ──────────────────────────────────────────

    /**
     * Peta respons default: satu teks statis per {@link Kategori}.
     * Digunakan saat tidak ada entri spesifik di database untuk kategori tersebut.
     * Menggunakan {@link EnumMap} untuk performa O(1) yang optimal.
     */
    private static final Map<Kategori, String> RESPON_DEFAULT = new EnumMap<>(Kategori.class);

    static {
        // ── Bantuan ───────────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.BANTUAN,
                "[ Panduan WedMate Assistant ]\n\n" +
                        "Berikut hal-hal yang bisa saya bantu:\n\n" +
                        "Koleksi Busana\n" +
                        "Ketik: 'lihat busana', 'koleksi gaun', 'tampilkan pakaian'\n\n" +
                        "Harga & Paket\n" +
                        "Ketik: 'berapa harga', 'paket sewa', 'biaya'\n\n" +
                        "Cek Ketersediaan\n" +
                        "Ketik: 'cek ketersediaan', 'ada stok', 'tanggal DD/MM/YYYY'\n\n" +
                        "Reservasi / Pemesanan\n" +
                        "Ketik: 'cara reservasi', 'mau pesan', 'booking'\n\n" +
                        "Info Toko\n" +
                        "Ketik: 'alamat toko', 'jam buka', 'lokasi', 'kontak'\n\n" +
                        "Sub-kategori Busana\n" +
                        "Ketik nama kategori: 'modern', 'tradisional', 'muslim',\n" +
                        "'internasional', 'bertema', 'pre-wedding', 'keluarga', 'pesta'\n" +
                        "Atau pilih nomor: 'pilih 1' hingga 'pilih 8'");

        // ── Greeting ──────────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.GREETING,
                "Halo! Selamat datang di WedMate Assistant.\n\n" +
                        "Saya siap membantu Anda menemukan busana pernikahan impian!\n\n" +
                        "Anda bisa bertanya tentang:\n" +
                        "  - Koleksi busana pernikahan\n" +
                        "  - Harga & paket sewa\n" +
                        "  - Ketersediaan & tanggal\n" +
                        "  - Cara reservasi\n" +
                        "  - Lokasi & jam operasional toko\n\n" +
                        "Ketik 'bantuan' untuk panduan lengkap. Ada yang bisa saya bantu?");

        // ── Lihat Busana ──────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.LIHAT_BUSANA,
                "[ Koleksi Busana Pernikahan WedMate ]\n\n" +
                        "Kami menyediakan 8 kategori busana:\n\n" +
                        "  1. Busana Modern (gaun internasional & jas tuxedo)\n" +
                        "  2. Busana Tradisional (kebaya, beskap, batik daerah)\n" +
                        "  3. Busana Muslim/Islami (gaun muslimah & kopiah)\n" +
                        "  4. Busana Internasional (Hanbok, Kimono, Cheongsam, Saree)\n" +
                        "  5. Busana Bertema (Vintage, Bohemian, Beach, Royal)\n" +   // BUG FIX: tambah ) penutup
                        "  6. Busana Pre-Wedding (outfit sesi foto couple)\n" +        // BUG FIX: tambah ) penutup
                        "  7. Busana Keluarga (orang tua, pagar ayu/bagus)\n" +       // BUG FIX: tambah ) penutup
                        "  8. Busana Semi-Formal/Pesta (gaun cocktail & blazer elegan)\n\n" +
                        "Ketik nama kategori atau 'pilih [nomor]' untuk detail lengkap.");

        // ── Harga & Paket ─────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.HARGA_PAKET,
                "[ Harga & Paket Sewa WedMate ]\n\n" +
                        "  Paket Basic     : mulai Rp   500.000 / hari\n" +
                        "  Paket Silver    : mulai Rp 1.200.000 / 2 hari\n" +
                        "  Paket Gold      : mulai Rp 2.500.000 (termasuk aksesoris)\n" +
                        "  Paket Platinum  : mulai Rp 4.000.000 (all-in, termasuk MUA)\n\n" +
                        "Catatan: Harga dapat berbeda tergantung item yang dipilih.\n" +
                        "Tersedia diskon untuk pemesanan jauh hari (min. 3 bulan).\n\n" +
                        "Ketik 'reservasi' untuk mulai memesan, atau tanyakan detail paket tertentu.");

        // ── Cek Ketersediaan ──────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.CEK_KETERSEDIAAN,
                "[ Cek Ketersediaan Busana ]\n\n" +
                        "Untuk membantu pengecekan, silakan berikan:\n\n" +
                        "1. Nama / jenis busana yang diminati\n" +
                        "   (contoh: 'gaun modern A-Line', 'kebaya sunda')\n" +
                        "2. Tanggal sewa  ->  format: DD/MM/YYYY\n" +
                        "3. Durasi penyewaan (dalam hari)\n\n" +
                        "Contoh pesan:\n" +
                        "\"Cek gaun modern tanggal 15/06/2025 selama 2 hari\"\n\n" +
                        "Atau hubungi kami via WhatsApp: 0812-3456-7890 untuk pengecekan cepat.");

        // ── Info Toko ─────────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.INFO_TOKO,
                "[ Informasi Toko WedMate ]\n\n" +
                        "  Alamat   : Jl. Pernikahan Indah No. 1, Jakarta\n" +
                        "  Telepon  : (021) 1234-5678\n" +
                        "  WhatsApp : 0812-3456-7890\n" +
                        "  Email    : hello@wedmate.id\n\n" +
                        "Jam Operasional:\n" +
                        "  Senin - Jumat  :  09.00 - 20.00 WIB\n" +
                        "  Sabtu          :  08.00 - 21.00 WIB\n" +
                        "  Minggu & Libur :  10.00 - 17.00 WIB\n\n" +
                        "Parkir tersedia. Kunjungi juga toko online kami di wedmate.id");

        // ── Reservasi ─────────────────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.RESERVASI,
                "[ Cara Reservasi Busana WedMate ]\n\n" +
                        "Langkah-langkah pemesanan:\n\n" +
                        "1. Pilih busana dari koleksi kami\n" +
                        "   (ketik 'lihat busana' untuk melihat pilihan)\n" +
                        "2. Tentukan tanggal & durasi sewa\n" +
                        "3. Isi data diri (nama, nomor HP, alamat pengiriman)\n" +
                        "4. Bayar DP sebesar 50% dari total biaya\n" +
                        "5. Konfirmasi via WhatsApp: 0812-3456-7890\n" +
                        "6. Pelunasan dilakukan H-1 sebelum tanggal acara\n\n" +
                        "Bisa juga langsung kunjungi toko kami untuk konsultasi gratis.");

        // ── Sub-kategori Busana ───────────────────────────────────────────────
        RESPON_DEFAULT.put(Kategori.BUSANA_MODERN,
                "[ Busana Modern - Kategori 1 ]\n\n" +
                        "Model yang tersedia:\n\n" +
                        "• Gaun Pengantin Internasional\n" +
                        "  Ball Gown | A-Line | Mermaid | Sheath | Tea-Length\n\n" +
                        "• Gaun Bridesmaid\n" +
                        "  Berbagai model & pilihan warna\n\n" +
                        "• Setelan Pria Modern\n" +
                        "  Tuxedo | Jas Formal | Vest | Bow-Tie\n\n" +
                        "Harga mulai Rp 800.000/hari\n" +
                        "Ketik 'cek ketersediaan' untuk cek tanggal pilihan Anda.");  // BUG FIX: tambah ); penutup

        RESPON_DEFAULT.put(Kategori.BUSANA_TRADISIONAL,
                "[ Busana Tradisional - Kategori 2 ]\n\n" +
                        "Koleksi dari 4 daerah utama:\n\n" +
                        "• Jawa\n" +
                        "  Kebaya Kutubaru | Beskap + Blangkon | Paes Ageng\n\n" +
                        "• Sunda\n" +
                        "  Kebaya Sunda | Mahkota Siger | Batik Mega Mendung\n\n" +
                        "• Minangkabau\n" +
                        "  Sulaman Benang Emas | Suntiang | Songket\n\n" +
                        "• Bali\n" +
                        "  Kebaya Endek | Payas Agung | Udeng Pria\n\n" +
                        "Harga mulai Rp 1.000.000/hari\n" +
                        "Ketik 'cek ketersediaan' untuk memastikan stok.");

        RESPON_DEFAULT.put(Kategori.BUSANA_MUSLIM,
                "[ Busana Muslim/Islami - Kategori 3 ]\n\n" +
                        "Pilihan busana islami kami:\n\n" +
                        "• Wanita\n" +
                        "  Kebaya Muslim | Gaun Muslimah Full Covered | Syaree\n" +
                        "  Gamis Modern | Gaun Berhijab Elegan\n\n" +
                        "• Pria\n" +
                        "  Beskap + Kopiah | Kemeja Koko Formal | Setelan Baju Koko\n\n" +
                        "Harga mulai Rp 700.000/hari\n" +
                        "Ketik 'cek ketersediaan' untuk pilih tanggal.");

        RESPON_DEFAULT.put(Kategori.BUSANA_INTERNASIONAL,
                "[ Busana Internasional / Etnik - Kategori 4 ]\n\n" +
                        "Pilihan busana etnik mancanegara:\n\n" +
                        "  - Hanbok (Korea)\n" +
                        "  - Kimono / Furisode (Jepang)\n" +
                        "  - Cheongsam / Qipao (Tionghoa)\n" +
                        "  - Saree (India)\n" +
                        "  - Sangeet / Lehenga (India-Muslim)\n\n" +
                        "Harga mulai Rp 900.000/hari\n" +
                        "Tersedia paket couple untuk pasangan.");

        RESPON_DEFAULT.put(Kategori.BUSANA_BERTEMA,
                "[ Busana Bertema (Themed Wedding) - Kategori 5 ]\n\n" +
                        "Tema pernikahan yang kami dukung:\n\n" +
                        "  - Vintage / Retro\n" +
                        "  - Bohemian / Garden Party\n" +
                        "  - Beach Wedding\n" +
                        "  - Royal / Fairytale\n" +
                        "  - Rustic / Earthy\n\n" +
                        "Harga mulai Rp 850.000/hari\n" +
                        "Tersedia konsultasi tema gratis di toko kami.");

        RESPON_DEFAULT.put(Kategori.BUSANA_PREWEDDING,
                "[ Busana Pre-Wedding & Sesi Foto - Kategori 6 ]\n\n" +
                        "Outfit yang tersedia:\n\n" +
                        "  - Outfit Kasual Couple (matching)\n" +
                        "  - Konsep Outdoor (casual, bohemian, rustic)\n" +
                        "  - Konsep Indoor (formal, studio)\n" +
                        "  - Busana Ganti Kedua (2nd outfit)\n\n" +
                        "Harga mulai Rp 600.000/sesi\n" +
                        "Kami juga bermitra dengan fotografer profesional.");

        RESPON_DEFAULT.put(Kategori.BUSANA_KELUARGA,
                "[ Busana Keluarga & Pendamping - Kategori 7 ]\n\n" +
                        "Tersedia untuk:\n\n" +
                        "  - Orang Tua (kebaya ibu, jas/batik bapak)\n" +
                        "  - Pagar Ayu & Pagar Bagus\n" +
                        "  - Flower Girl & Ring Bearer\n" +
                        "  - MC & Wedding Organizer\n" +
                        "  - Bridesmaid & Groomsman\n\n" +
                        "Harga mulai Rp 350.000/orang/hari\n" +
                        "Diskon khusus untuk paket keluarga (min. 5 orang).");

        RESPON_DEFAULT.put(Kategori.BUSANA_PESTA,
                "[ Busana Semi-Formal / Pesta - Kategori 8 ]\n\n" +
                        "Pilihan busana pesta & non-wedding:\n\n" +
                        "• Wanita\n" +
                        "  Gaun Cocktail | Midi Dress | Maxi Dress | Blazer Wanita\n\n" +
                        "• Pria\n" +
                        "  Kemeja Batik Elegan | Jas Casual | Setelan Semi-Formal\n\n" +
                        "Cocok untuk: gala dinner, wisuda, ulang tahun mewah\n\n" +
                        "Harga mulai Rp 450.000/hari\n" +
                        "Ketik 'cek ketersediaan' untuk tanggal pilihan Anda.");

        RESPON_DEFAULT.put(Kategori.TIDAK_DIKENAL, FALLBACK);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Menghasilkan respons default berdasarkan kategori yang terdeteksi.
     *
     * <p>
     * Digunakan ketika tidak ada entri spesifik di database, atau sebagai
     * respons awal sebelum data DB dimuat.
     * </p>
     *
     * @param kategori intent yang terdeteksi oleh {@link RegexMatcher}
     * @return teks respons yang siap ditampilkan ke pengguna
     */
    public String generate(Kategori kategori) {
        if (kategori == null) return FALLBACK;
        return RESPON_DEFAULT.getOrDefault(kategori, FALLBACK);
    }

    /**
     * Menghasilkan respons dari entri knowledge base yang ditemukan di database.
     *
     * <p>
     * Jika {@code entri} adalah {@code null}, dikembalikan {@link #FALLBACK}.
     * </p>
     *
     * @param entri entri knowledge base hasil pencocokan (boleh null)
     * @return teks {@code jawaban} dari entri, atau pesan fallback
     */
    public String generate(EntriKnowledge entri) {
        if (entri == null || entri.getJawaban() == null || entri.getJawaban().isBlank()) {
            return FALLBACK;
        }
        return entri.getJawaban();
    }

    /**
     * Menghasilkan respons dengan prioritas:
     * <ol>
     * <li>Jawaban dari entri DB (jika tidak null dan tidak kosong)</li>
     * <li>Respons default kategori (jika entri null tapi kategori dikenal)</li>
     * <li>Pesan fallback</li>
     * </ol>
     *
     * <p>
     * Ini adalah method utama yang sebaiknya dipanggil oleh {@code ChatbotEngine}.
     * </p>
     *
     * @param entri    entri DB hasil {@code RegexMatcher.cocokkan()} (boleh null)
     * @param kategori kategori hasil {@code RegexMatcher.deteksiKategori()}
     * @return teks respons final
     */
    public String generate(EntriKnowledge entri, Kategori kategori) {
        // Prioritas 1: jawaban spesifik dari DB
        if (entri != null
                && entri.getJawaban() != null
                && !entri.getJawaban().isBlank()) {
            return entri.getJawaban();
        }

        // Prioritas 2: respons default per kategori
        if (kategori != null && kategori != Kategori.TIDAK_DIKENAL) {
            return RESPON_DEFAULT.getOrDefault(kategori, FALLBACK);
        }

        // Prioritas 3: fallback umum
        return FALLBACK;
    }

    /**
     * Mengembalikan pesan fallback standar.
     *
     * @return teks fallback
     */
    public String getFallback() {
        return FALLBACK;
    }
}