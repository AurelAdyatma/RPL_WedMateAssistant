package com.rplbo.app.rpl_wedmateassistant;

// Import engine chatbot untuk memproses pesan pengguna
import com.rplbo.app.rpl_wedmateassistant.engine.ChatbotEngine;
// Import model Sesi untuk menyimpan percakapan
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;
// Import model Pesan untuk merepresentasikan pesan individual
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
// Import model PakaianWedding untuk data pakaian pernikahan
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
// Import model PaketSewa untuk data paket sewa
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;
// Import ArrayList untuk membuat list dinamis
import java.util.ArrayList;
// Import List sebagai tipe data koleksi
import java.util.List;

/**
 * Kelas pengujian (test) untuk ChatbotEngine secara langsung via console.
 *
 * <p>Kelas ini digunakan untuk menguji respons chatbot tanpa menjalankan
 * GUI JavaFX. Data pakaian dan paket di-hardcode langsung sebagai data dummy
 * untuk mensimulasikan kondisi database yang terisi.</p>
 *
 * <p>Cara penggunaan: jalankan method {@code main()} langsung dari IDE
 * untuk melihat output chatbot di console/terminal.</p>
 */
public class TestChatbot {
    /**
     * Method main — menjalankan serangkaian tes input terhadap ChatbotEngine.
     * Membuat data dummy pakaian dan paket, lalu mengirimkan berbagai input
     * untuk menguji kemampuan chatbot dalam mengenali intent pengguna.
     *
     * @param args argumen command-line (tidak digunakan)
     */
    public static void main(String[] args) {
        // Membuat instance ChatbotEngine untuk memproses pesan
        ChatbotEngine engine = new ChatbotEngine();
        
        // ── Menyiapkan data dummy pakaian ────────────────────────────────────
        List<PakaianWedding> pakaian = new ArrayList<>();
        // Menambahkan contoh pakaian tradisional: Kebaya Sunda Merah Marun
        pakaian.add(new PakaianWedding(1, "Kebaya Sunda Merah Marun", "Tradisional", "Kebaya Sunda bordir motif khas dengan aksen payet", 950000, "S, M, L", null, "Wanita", true));
        // Menambahkan contoh pakaian modern: Jas Formal Hitam
        pakaian.add(new PakaianWedding(2, "Jas Formal Hitam", "Modern", "Jas formal", 500000, "M, L, XL", null, "Pria", true));
        // Menginjeksikan daftar pakaian ke engine
        engine.setDaftarPakaian(pakaian);
        
        // ── Menyiapkan data dummy paket sewa ─────────────────────────────────
        List<PaketSewa> paket = new ArrayList<>();
        // Menambahkan contoh paket sewa: Paket Platinum
        paket.add(new PaketSewa(1, "Paket Platinum", "1 busana pengantin, 6 keluarga, MUA", 4000000, 2, new ArrayList<>()));
        // Menginjeksikan daftar paket ke engine
        engine.setDaftarPaket(paket);
        
        // ── Menyiapkan sesi percakapan ───────────────────────────────────────
        // Membuat sesi baru untuk menyimpan riwayat percakapan tes
        Sesi sesi = new Sesi();
        sesi.setId(1);
        // Inisialisasi daftar pesan kosong untuk sesi ini
        sesi.setDaftarPesan(new ArrayList<>());
        
        // ── Daftar input tes untuk diuji ─────────────────────────────────────
        // Array berisi berbagai jenis pertanyaan yang mungkin diajukan pengguna
        String[] inputs = {
            "Sewa baju adat Sunda ukuran L budget 2 juta tanggal 12 Juli, masih tersedia?",  // Tes: pencarian kombinasi (adat, ukuran, budget, ketersediaan)
            "Budget 5 juta untuk 2 acara, 4 pasang keluarga",                                 // Tes: rekomendasi paket berdasarkan budget
            "berapa denda kalau telat kembalikan?",                                            // Tes: kebijakan denda keterlambatan
            "estimasi biaya sewa gaun 2 hari 3 item",                                          // Tes: estimasi biaya sewa
            "tema garden party outdoor",                                                        // Tes: rekomendasi tema
            "gimana kalau DP?",                                                                 // Tes: kebijakan DP/pembayaran
            "mau konsep adat jawa",                                                             // Tes: rekomendasi tema tradisional
            "diskon member ada?"                                                                // Tes: kebijakan diskon/promo
        };
        
        // ── Menjalankan setiap input tes ─────────────────────────────────────
        // Loop melalui setiap input, kirim ke engine, dan cetak hasilnya
        for (String input : inputs) {
            System.out.println("==========================================");
            System.out.println("INPUT: " + input);
            // Memproses pesan melalui ChatbotEngine dan mendapatkan respons bot
            Pesan pesan = engine.prosesPesan(input, sesi);
            // Menampilkan isi pesan balasan dari bot
            System.out.println("OUTPUT:\n" + pesan.getIsiPesan());
        }
    }
}
