package com.rplbo.app.rpl_wedmateassistant.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Mengisi database dengan data awal (seed) saat pertama kali aplikasi dijalankan.
 *
 * <p>Dipanggil sekali setelah {@code DatabaseManager.initDB()} berhasil.
 * Data hanya dimasukkan jika tabel masih kosong (idempotent).</p>
 */
public class DataSeeder {

    private final DatabaseManager dbManager;

    public DataSeeder() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Menjalankan semua seed data jika tabel masih kosong. */
    public void seed() {
        try {
            Connection conn = dbManager.getConnection();
            if (isEmpty(conn, "pakaian_wedding")) seedPakaian(conn);
            if (isEmpty(conn, "paket_sewa"))      seedPaket(conn);
            if (isEmpty(conn, "knowledge_base"))  seedKnowledgeBase(conn);
            System.out.println("[DataSeeder] Seeding selesai.");
        } catch (SQLException e) {
            System.err.println("[DataSeeder] Gagal seed: " + e.getMessage());
        }
    }

    private boolean isEmpty(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    // ── Pakaian Wedding ───────────────────────────────────────────────────────

    private void seedPakaian(Connection conn) throws SQLException {
        String sql = "INSERT INTO pakaian_wedding (nama, jenis, ukuran, harga_sewa, gender, tersedia) VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] data = {
            // Busana Modern
            {"Gaun Ball Gown Ivory",          "Modern",       "S,M,L",    1_500_000, "Wanita", 1},
            {"Gaun A-Line Champagne",          "Modern",       "XS,S,M",   1_200_000, "Wanita", 1},
            {"Gaun Mermaid Ivory",             "Modern",       "S,M",      1_800_000, "Wanita", 1},
            {"Tuxedo Pria Classic Black",      "Modern",       "M,L,XL",   900_000,   "Pria",   1},
            {"Jas Formal Navy Blue",           "Modern",       "M,L,XL",   750_000,   "Pria",   1},

            // Busana Tradisional Jawa
            {"Kebaya Kutubaru Hijau Toska",    "Tradisional",  "S,M,L",    1_000_000, "Wanita", 1},
            {"Kebaya Paes Ageng Gold",         "Tradisional",  "S,M",      2_000_000, "Wanita", 1},
            {"Beskap Pria Hitam Blangkon",     "Tradisional",  "M,L,XL",   1_200_000, "Pria",   1},
            {"Kebaya Sunda Merah Marun",       "Tradisional",  "S,M,L",    950_000,   "Wanita", 1},
            {"Baju Adat Jawa Jogja Set",       "Tradisional",  "M,L",      1_500_000, "Unisex", 1},

            // Busana Muslim
            {"Gaun Muslimah Syar'i Putih",    "Muslim",       "S,M,L,XL", 1_100_000, "Wanita", 1},
            {"Kebaya Muslim Modern Blush",     "Muslim",       "S,M,L",    900_000,   "Wanita", 1},
            {"Setelan Koko Pria Putih",        "Muslim",       "M,L,XL",   600_000,   "Pria",   1},

            // Busana Internasional
            {"Hanbok Wanita Biru Muda",        "Internasional","S,M",      1_300_000, "Wanita", 1},
            {"Kimono Furisode Sakura",         "Internasional","S,M,L",    1_600_000, "Wanita", 1},
            {"Cheongsam Merah Emas",           "Internasional","S,M",      1_100_000, "Wanita", 1},

            // Busana Bertema
            {"Gaun Vintage Lace Cream",        "Bertema",      "S,M,L",    1_200_000, "Wanita", 1},
            {"Bohemian Bridal Set Flower",     "Bertema",      "S,M",      1_000_000, "Wanita", 1},
            {"Royal Ballgown Deep Blue",       "Bertema",      "S,M,L",    2_200_000, "Wanita", 1},

            // Busana Pre-Wedding
            {"Couple Set Casual Denim",        "Pre-Wedding",  "XS-XL",    500_000,   "Unisex", 1},
            {"Outdoor Bohemian Couple",        "Pre-Wedding",  "S,M,L",    700_000,   "Unisex", 1},

            // Busana Keluarga
            {"Kebaya Ibu Pengantin Biru",      "Keluarga",     "S,M,L,XL", 800_000,   "Wanita", 1},
            {"Batik Bapak Pengantin Coklat",   "Keluarga",     "M,L,XL",   650_000,   "Pria",   1},
            {"Set Pagar Ayu 5 Orang",          "Keluarga",     "S,M",      350_000,   "Wanita", 1},

            // Busana Pesta
            {"Gaun Cocktail Navy",             "Pesta",        "XS,S,M,L", 600_000,   "Wanita", 1},
            {"Blazer Elegan Pria Abu",         "Pesta",        "M,L,XL",   500_000,   "Pria",   1},
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                stmt.setString(1, (String)  row[0]);
                stmt.setString(2, (String)  row[1]);
                stmt.setString(3, (String)  row[2]);
                stmt.setDouble(4, ((Number) row[3]).doubleValue());
                stmt.setString(5, (String)  row[4]);
                stmt.setInt(6,    ((Number) row[5]).intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        System.out.println("[DataSeeder] Pakaian wedding berhasil di-seed (" + data.length + " item).");
    }

    // ── Paket Sewa ────────────────────────────────────────────────────────────

    private void seedPaket(Connection conn) throws SQLException {
        String sql = "INSERT INTO paket_sewa (nama_paket, deskripsi, harga, fasilitas, tersedia) VALUES (?, ?, ?, ?, ?)";
        Object[][] data = {
            {
                "Paket Basic",
                "Satu busana pengantin utama, ukuran standard, sewa 1 hari.",
                500_000,
                "1 busana pengantin | Aksesoris dasar | Gantungan pakaian",
                1
            },
            {
                "Paket Silver",
                "Busana pengantin + busana keluarga (2 orang), sewa 2 hari.",
                1_200_000,
                "1 busana pengantin | 2 busana keluarga | Aksesoris | Fitting gratis",
                1
            },
            {
                "Paket Gold",
                "Busana pengantin lengkap + aksesoris + busana keluarga (4 orang), sewa 2 hari.",
                2_500_000,
                "1 busana pengantin | 4 busana keluarga | Set aksesoris lengkap | Pagar ayu 4 orang | Fitting 2x",
                1
            },
            {
                "Paket Platinum",
                "Paket all-in: busana pengantin, keluarga, pagar ayu, MUA, dan dekorasi sederhana.",
                4_000_000,
                "1 busana pengantin | 6 busana keluarga | 6 pagar ayu/bagus | Make-up artist | Konsultasi busana | Dekorasi sederhana | Antar-jemput busana",
                1
            },
            {
                "Paket Pre-Wedding",
                "Paket khusus sesi foto pre-wedding: 2 outfit couple + aksesori.",
                800_000,
                "2 outfit couple | Aksesoris foto | Konsultasi stylist | Ganti 2 set kostum",
                1
            },
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                stmt.setString(1, (String)  row[0]);
                stmt.setString(2, (String)  row[1]);
                stmt.setDouble(3, ((Number) row[2]).doubleValue());
                stmt.setString(4, (String)  row[3]);
                stmt.setInt(5,    ((Number) row[4]).intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        System.out.println("[DataSeeder] Paket sewa berhasil di-seed (" + data.length + " paket).");
    }

    // ── Knowledge Base ────────────────────────────────────────────────────────

    private void seedKnowledgeBase(Connection conn) throws SQLException {
        String sql = "INSERT INTO knowledge_base (pertanyaan, jawaban, kategori, aktif) VALUES (?, ?, ?, ?)";
        Object[][] data = {

            // GREETING
            {
                "halo|hai|selamat|hello|hi|pagi|siang|sore|malam",
                "Halo! Selamat datang di WedMate Assistant.\n\n" +
                "Saya siap membantu Anda menemukan busana pernikahan impian!\n\n" +
                "Anda bisa bertanya tentang:\n" +
                "  - Koleksi busana pernikahan\n" +
                "  - Harga & paket sewa\n" +
                "  - Ketersediaan & tanggal\n" +
                "  - Cara reservasi\n" +
                "  - Lokasi & jam operasional toko\n\n" +
                "Ketik 'bantuan' untuk panduan lengkap. Ada yang bisa saya bantu?",
                "GREETING", 1
            },

            // LIHAT_BUSANA
            {
                "lihat|koleksi|tampilkan|busana|pakaian|gaun|katalog",
                "[ Koleksi Busana Pernikahan WedMate ]\n\n" +
                "Kami menyediakan 8 kategori busana:\n\n" +
                "  1. Busana Modern       - Gaun internasional & jas tuxedo\n" +
                "  2. Busana Tradisional  - Kebaya, beskap, batik daerah\n" +
                "  3. Busana Muslim       - Gaun muslimah & kopiah formal\n" +
                "  4. Busana Internasional- Hanbok, Kimono, Cheongsam, Saree\n" +
                "  5. Busana Bertema      - Vintage, Bohemian, Beach, Royal\n" +
                "  6. Busana Pre-Wedding  - Outfit sesi foto couple\n" +
                "  7. Busana Keluarga     - Orang tua, pagar ayu/bagus\n" +
                "  8. Busana Pesta        - Gaun cocktail & blazer elegan\n\n" +
                "Ketik nama kategori atau 'pilih [nomor]' untuk detail lengkap.\n" +
                "Contoh: 'lihat busana tradisional' atau 'pilih 2'",
                "LIHAT_BUSANA", 1
            },

            // BUSANA_TRADISIONAL - Jawa
            {
                "jawa|beskap|blangkon|kebaya kutubaru|paes ageng|batik jawa|adat jawa|jogja|solo",
                "[ Busana Tradisional Jawa - WedMate ]\n\n" +
                "Koleksi Busana Adat Jawa kami:\n\n" +
                "WANITA:\n" +
                "  - Kebaya Kutubaru Hijau Toska   | Rp 1.000.000/hari | S,M,L\n" +
                "  - Kebaya Paes Ageng Gold         | Rp 2.000.000/hari | S,M\n\n" +
                "PRIA:\n" +
                "  - Beskap Hitam + Blangkon        | Rp 1.200.000/hari | M,L,XL\n" +
                "  - Baju Adat Jawa Jogja Set        | Rp 1.500.000/hari | M,L\n\n" +
                "Semua busana Jawa tersedia untuk sewa.\n" +
                "Ketik 'cek ketersediaan' untuk pilih tanggal.",
                "BUSANA_TRADISIONAL", 1
            },

            // HARGA_PAKET
            {
                "harga|biaya|tarif|bayar|budget|murah|mahal|paket|sewa",
                "[ Harga & Paket Sewa WedMate ]\n\n" +
                "  Paket Basic       : Rp    500.000 / hari\n" +
                "                     1 busana pengantin + aksesoris dasar\n\n" +
                "  Paket Silver      : Rp  1.200.000 / 2 hari\n" +
                "                     1 busana + 2 busana keluarga + fitting\n\n" +
                "  Paket Gold        : Rp  2.500.000 / 2 hari\n" +
                "                     Busana + 4 keluarga + 4 pagar ayu + aksesoris\n\n" +
                "  Paket Platinum    : Rp  4.000.000 (all-in)\n" +
                "                     Semua termasuk MUA & konsultasi\n\n" +
                "  Paket Pre-Wedding : Rp    800.000 / sesi\n" +
                "                     2 outfit couple + stylist\n\n" +
                "Ketik 'reservasi' untuk mulai memesan.",
                "HARGA_PAKET", 1
            },

            // CEK_KETERSEDIAAN
            {
                "ketersediaan|tersedia|stok|cek|ada|tanggal|kapan",
                "[ Cek Ketersediaan Busana ]\n\n" +
                "Untuk pengecekan, silakan berikan:\n\n" +
                "1. Nama/jenis busana yang diminati\n" +
                "   Contoh: 'kebaya jawa', 'gaun modern', 'hanbok'\n" +
                "2. Tanggal sewa  ->  format: DD/MM/YYYY\n" +
                "3. Durasi penyewaan (dalam hari)\n\n" +
                "Contoh pesan:\n" +
                "\"Cek kebaya jawa tanggal 15/06/2025 selama 2 hari\"\n\n" +
                "Atau hubungi kami via WhatsApp: 0812-3456-7890",
                "CEK_KETERSEDIAAN", 1
            },

            // RESERVASI
            {
                "reservasi|pesan|booking|order|daftar|hubungi",
                "[ Cara Reservasi Busana WedMate ]\n\n" +
                "Langkah-langkah pemesanan:\n\n" +
                "1. Pilih busana dari koleksi kami\n" +
                "   (ketik 'lihat busana' untuk melihat pilihan)\n" +
                "2. Tentukan tanggal & durasi sewa\n" +
                "3. Isi data diri (nama, nomor HP, alamat)\n" +
                "4. Bayar DP sebesar 50% dari total biaya\n" +
                "5. Konfirmasi via WhatsApp: 0812-3456-7890\n" +
                "6. Pelunasan dilakukan H-1 sebelum acara\n\n" +
                "Bisa juga langsung kunjungi toko untuk konsultasi gratis.",
                "RESERVASI", 1
            },

            // INFO_TOKO
            {
                "alamat|lokasi|toko|telepon|kontak|jam|buka|tutup|whatsapp|email",
                "[ Informasi Toko WedMate ]\n\n" +
                "  Alamat   : Jl. Pernikahan Indah No. 1, Jakarta\n" +
                "  Telepon  : (021) 1234-5678\n" +
                "  WhatsApp : 0812-3456-7890\n" +
                "  Email    : hello@wedmate.id\n\n" +
                "Jam Operasional:\n" +
                "  Senin - Jumat  :  09.00 - 20.00 WIB\n" +
                "  Sabtu          :  08.00 - 21.00 WIB\n" +
                "  Minggu & Libur :  10.00 - 17.00 WIB\n\n" +
                "Parkir tersedia. Kunjungi juga toko online kami di wedmate.id",
                "INFO_TOKO", 1
            },
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                stmt.setString(1, (String)  row[0]);
                stmt.setString(2, (String)  row[1]);
                stmt.setString(3, (String)  row[2]);
                stmt.setInt(4,    ((Number) row[3]).intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        System.out.println("[DataSeeder] Knowledge base berhasil di-seed (" + data.length + " entri).");
    }
}
