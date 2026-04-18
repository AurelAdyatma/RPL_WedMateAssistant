package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;

import java.util.HashMap;
import java.util.Map;

/**
 * Menghasilkan teks respons chatbot WedMate Assistant.
 *
 * <h3>Dua sumber respons:</h3>
 * <ol>
 *   <li><b>Default per kategori</b> — teks statis yang selalu tersedia
 *       meskipun database kosong ({@link #generate(Kategori)}).</li>
 *   <li><b>Dinamis dari knowledge base DB</b> — teks {@code jawaban} yang
 *       diambil dari {@link EntriKnowledge} ({@link #generate(EntriKnowledge)}).</li>
 * </ol>
 *
 * <p>Jika tidak ada yang cocok sama sekali, dikembalikan {@link #FALLBACK}.</p>
 */
public class ResponseGenerator {

    // ── Pesan Fallback ────────────────────────────────────────────────────────

    /** Respons ketika tidak ada kategori maupun entri DB yang cocok. */
    public static final String FALLBACK =
            "Maaf, saya belum bisa memahami pertanyaan Anda. 😊\n" +
            "Coba tanyakan tentang:\n" +
            "  • Koleksi busana & gaun 👗\n" +
            "  • Harga & paket sewa 💰\n" +
            "  • Ketersediaan pakaian 📅\n" +
            "  • Cara reservasi 📋\n" +
            "  • Info & lokasi toko 📍\n" +
            "Atau ketik 'bantuan' untuk panduan lengkap.";

    // ── Respons Default per Kategori ──────────────────────────────────────────

    /**
     * Peta respons default: satu teks statis per {@link Kategori}.
     * Digunakan saat tidak ada entri spesifik di database untuk kategori tersebut.
     */
    private static final Map<Kategori, String> RESPON_DEFAULT = new HashMap<>();

    static {
        RESPON_DEFAULT.put(Kategori.GREETING,
                "Halo! Selamat datang di WedMate Assistant 💍\n" +
                "Saya siap membantu Anda menemukan busana pernikahan impian!\n" +
                "Ada yang bisa saya bantu hari ini?");

        RESPON_DEFAULT.put(Kategori.LIHAT_BUSANA,
                "Kami memiliki koleksi busana pernikahan yang beragam! 👗✨\n" +
                "Tersedia:\n" +
                "  • Gaun Pengantin (Modern & Tradisional)\n" +
                "  • Jas Pria Elegan\n" +
                "  • Kebaya Pernikahan\n" +
                "  • Beskap & Adat Nusantara\n\n" +
                "Ketik nama kategori untuk melihat koleksi lengkap beserta harga.");

        RESPON_DEFAULT.put(Kategori.HARGA_PAKET,
                "Berikut gambaran harga sewa di WedMate: 💰\n" +
                "  • Paket Basic   : mulai Rp 500.000 / hari\n" +
                "  • Paket Silver  : mulai Rp 1.200.000 / 2 hari\n" +
                "  • Paket Gold    : mulai Rp 2.500.000 (termasuk aksesoris)\n" +
                "  • Paket Platinum: mulai Rp 4.000.000 (all-in)\n\n" +
                "Harga bisa berbeda tergantung item yang dipilih.\n" +
                "Ketik 'paket [nama]' untuk detail lengkap.");

        RESPON_DEFAULT.put(Kategori.CEK_KETERSEDIAAN,
                "Untuk mengecek ketersediaan busana pada tanggal tertentu,\n" +
                "silakan berikan informasi berikut:\n" +
                "  1. Nama / jenis busana yang diminati\n" +
                "  2. Tanggal sewa (format: DD/MM/YYYY)\n" +
                "  3. Durasi penyewaan (hari)\n\n" +
                "Contoh: \"Cek gaun modern tanggal 15/06/2025 selama 2 hari\"");

        RESPON_DEFAULT.put(Kategori.INFO_TOKO,
                "📍 Informasi Toko WedMate:\n" +
                "  Alamat : Jl. Pernikahan Indah No. 1, Kota\n" +
                "  Telepon: (021) 1234-5678\n" +
                "  WhatsApp: 0812-3456-7890\n\n" +
                "🕐 Jam Operasional:\n" +
                "  Senin – Sabtu : 09.00 – 20.00 WIB\n" +
                "  Minggu        : 10.00 – 17.00 WIB\n\n" +
                "  (Tutup pada hari libur nasional)");

        RESPON_DEFAULT.put(Kategori.RESERVASI,
                "Untuk melakukan reservasi busana, berikut langkahnya: 📋\n" +
                "  1. Pilih busana yang diinginkan dari koleksi kami\n" +
                "  2. Tentukan tanggal & durasi sewa\n" +
                "  3. Isi data diri Anda\n" +
                "  4. Lakukan pembayaran DP (50%)\n" +
                "  5. Konfirmasi via WhatsApp\n\n" +
                "Atau langsung kunjungi toko kami. Mau saya bantu mulai reservasi?");

        RESPON_DEFAULT.put(Kategori.TIDAK_DIKENAL, FALLBACK);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Menghasilkan respons default berdasarkan kategori yang terdeteksi.
     *
     * <p>Digunakan ketika tidak ada entri spesifik di database, atau sebagai
     * respons awal sebelum data DB dimuat.</p>
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
     * <p>Jika {@code entri} adalah {@code null}, dikembalikan {@link #FALLBACK}.</p>
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
     *   <li>Jawaban dari entri DB (jika tidak null)</li>
     *   <li>Respons default kategori (jika entri null tapi kategori dikenal)</li>
     *   <li>Pesan fallback</li>
     * </ol>
     *
     * <p>Ini adalah method utama yang sebaiknya dipanggil oleh {@code ChatbotEngine}.</p>
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
