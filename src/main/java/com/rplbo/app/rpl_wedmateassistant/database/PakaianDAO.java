package com.rplbo.app.rpl_wedmateassistant.database;

// Import model PakaianWedding yang dikelola oleh DAO ini
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;

// Import kelas JDBC untuk koneksi dan query database
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Import ArrayList dan List untuk menyimpan hasil query
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel pakaian_wedding di SQLite.
 * Menyediakan operasi CRUD untuk PakaianWedding.
 * Foto disimpan sebagai BLOB (byte[]) di kolom foto_data.
 *
 * <p>Operasi yang tersedia:</p>
 * <ul>
 *   <li>{@link #findAll()} — mengambil semua pakaian</li>
 *   <li>{@link #findByKategori(String)} — mencari pakaian berdasarkan jenis/kategori</li>
 *   <li>{@link #findById(int)} — mengambil pakaian berdasarkan ID</li>
 *   <li>{@link #findByNamaContaining(String)} — mencari pakaian berdasarkan keyword di nama/deskripsi</li>
 *   <li>{@link #save(PakaianWedding)} — menyimpan pakaian baru</li>
 *   <li>{@link #update(PakaianWedding)} — memperbarui data pakaian</li>
 *   <li>{@link #delete(int)} — menghapus pakaian berdasarkan ID</li>
 * </ul>
 */
public class PakaianDAO {

    /** Referensi ke DatabaseManager singleton untuk mendapatkan koneksi database */
    private final DatabaseManager dbManager;

    /**
     * Constructor — menginisialisasi DAO dengan instance DatabaseManager singleton.
     */
    public PakaianDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Mengambil semua pakaian dari tabel pakaian_wedding.
     *
     * @return daftar semua PakaianWedding, atau list kosong jika tidak ada data
     */
    /** Mengembalikan semua pakaian yang tersedia. */
    public List<PakaianWedding> findAll() {
        // Inisialisasi list kosong untuk menampung hasil query
        List<PakaianWedding> list = new ArrayList<>();
        // Query SQL untuk mengambil semua baris dari tabel pakaian_wedding
        String sql = "SELECT * FROM pakaian_wedding";
        // Menggunakan try-with-resources agar koneksi otomatis ditutup
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // Iterasi setiap baris hasil query dan mapping ke objek PakaianWedding
            while (rs.next()) {
                list.add(mapToPakaian(rs));
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil pakaian berdasarkan kategori/jenis (misalnya "Modern", "Tradisional").
     *
     * @param kategori jenis pakaian yang dicari
     * @return daftar PakaianWedding yang sesuai kategori, atau list kosong
     */
    /** Mengembalikan pakaian berdasarkan kategori. */
    public List<PakaianWedding> findByKategori(String kategori) {
        // Inisialisasi list kosong untuk hasil query
        List<PakaianWedding> list = new ArrayList<>();
        // Query SQL dengan filter berdasarkan kolom "jenis"
        String sql = "SELECT * FROM pakaian_wedding WHERE jenis = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter kategori pada prepared statement
            stmt.setString(1, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                // Iterasi setiap baris hasil dan mapping ke objek
                while (rs.next()) {
                    list.add(mapToPakaian(rs));
                }
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil satu pakaian berdasarkan ID.
     *
     * @param id ID pakaian yang dicari
     * @return objek PakaianWedding jika ditemukan, atau null jika tidak ada
     */
    /** Mengembalikan pakaian berdasarkan ID. */
    public PakaianWedding findById(int id) {
        // Query SQL dengan parameter WHERE untuk mencari berdasarkan ID
        String sql = "SELECT * FROM pakaian_wedding WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter ID pada prepared statement
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // Jika ada baris hasil, mapping ke objek PakaianWedding
                if (rs.next()) {
                    return mapToPakaian(rs);
                }
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        // Return null jika pakaian tidak ditemukan
        return null;
    }

    /**
     * Menyimpan pakaian baru ke database termasuk data foto (BLOB).
     *
     * @param pakaian objek PakaianWedding yang akan disimpan
     * @return true jika berhasil menyimpan, false jika gagal
     */
    public boolean save(PakaianWedding pakaian) {
        // Query SQL INSERT dengan 8 parameter termasuk foto_data (BLOB)
        String sql = "INSERT INTO pakaian_wedding (nama, jenis, ukuran, harga_sewa, gender, tersedia, deskripsi, foto_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, pakaian.getNama());                                             // Nama pakaian
            stmt.setString(2, pakaian.getKategori());                                         // Jenis/kategori pakaian
            stmt.setString(3, pakaian.getUkuranTersedia());                                   // Ukuran yang tersedia
            stmt.setDouble(4, pakaian.getHargaSewa());                                        // Harga sewa per hari
            stmt.setString(5, pakaian.getGender() != null ? pakaian.getGender() : "Unisex");  // Gender (default "Unisex" jika null)
            stmt.setInt(6, pakaian.isTersedia() ? 1 : 0);                                    // Status tersedia (1/0)
            stmt.setString(7, pakaian.getDeskripsi());                                        // Deskripsi detail
            stmt.setBytes(8, pakaian.getImageData());                                         // Data foto sebagai BLOB
            // Eksekusi INSERT dan cek apakah berhasil
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Memperbarui data pakaian yang sudah ada di database termasuk foto.
     *
     * @param pakaian objek PakaianWedding dengan data yang sudah diperbarui
     * @return true jika berhasil memperbarui, false jika gagal
     */
    public boolean update(PakaianWedding pakaian) {
        // Query SQL UPDATE untuk mengubah semua kolom pakaian berdasarkan ID
        String sql = "UPDATE pakaian_wedding SET nama = ?, jenis = ?, ukuran = ?, harga_sewa = ?, gender = ?, tersedia = ?, deskripsi = ?, foto_data = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, pakaian.getNama());                                             // Nama pakaian baru
            stmt.setString(2, pakaian.getKategori());                                         // Jenis/kategori baru
            stmt.setString(3, pakaian.getUkuranTersedia());                                   // Ukuran baru
            stmt.setDouble(4, pakaian.getHargaSewa());                                        // Harga baru
            stmt.setString(5, pakaian.getGender() != null ? pakaian.getGender() : "Unisex");  // Gender baru
            stmt.setInt(6, pakaian.isTersedia() ? 1 : 0);                                    // Status tersedia baru
            stmt.setString(7, pakaian.getDeskripsi());                                        // Deskripsi baru
            stmt.setBytes(8, pakaian.getImageData());                                         // Foto baru (BLOB)
            stmt.setInt(9, pakaian.getId());                                                  // ID pakaian (WHERE clause)
            // Eksekusi UPDATE dan cek apakah ada baris yang terpengaruh
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Menghapus pakaian dari database berdasarkan ID.
     *
     * @param id ID pakaian yang akan dihapus
     * @return true jika berhasil menghapus, false jika gagal
     */
    public boolean delete(int id) {
        // Query SQL DELETE untuk menghapus pakaian berdasarkan ID
        String sql = "DELETE FROM pakaian_wedding WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter ID pada prepared statement
            stmt.setInt(1, id);
            // Eksekusi DELETE dan cek apakah ada baris yang terhapus
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mencari pakaian berdasarkan keyword di nama atau deskripsi.
     * Pencarian bersifat case-insensitive menggunakan LIKE SQL.
     *
     * @param keyword kata kunci pencarian
     * @return daftar PakaianWedding yang nama atau deskripsinya mengandung keyword
     */
    /** Mencari pakaian berdasarkan keyword di nama atau deskripsi. */
    public List<PakaianWedding> findByNamaContaining(String keyword) {
        // Inisialisasi list kosong untuk hasil pencarian
        List<PakaianWedding> list = new ArrayList<>();
        // Query SQL dengan LIKE pada kolom nama dan deskripsi (case-insensitive via LOWER)
        String sql = "SELECT * FROM pakaian_wedding WHERE LOWER(nama) LIKE ? OR LOWER(deskripsi) LIKE ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Buat parameter LIKE dengan wildcard di awal dan akhir
            String likeParam = "%" + keyword.toLowerCase() + "%";
            // Set parameter untuk pencarian di kolom nama
            stmt.setString(1, likeParam);
            // Set parameter untuk pencarian di kolom deskripsi
            stmt.setString(2, likeParam);
            try (ResultSet rs = stmt.executeQuery()) {
                // Iterasi setiap baris hasil dan mapping ke objek
                while (rs.next()) {
                    list.add(mapToPakaian(rs));
                }
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Helper method untuk mapping satu baris ResultSet menjadi objek PakaianWedding.
     * Kolom gender, deskripsi, dan foto_data di-handle dengan try-catch terpisah
     * untuk backward compatibility dengan database lama yang mungkin belum memiliki kolom tersebut.
     *
     * @param rs ResultSet yang posisinya sudah berada di baris yang akan dimapping
     * @return objek PakaianWedding yang sudah terisi data
     * @throws SQLException jika terjadi error saat membaca kolom utama dari ResultSet
     */
    private PakaianWedding mapToPakaian(ResultSet rs) throws SQLException {
        PakaianWedding p = new PakaianWedding();
        p.setId(rs.getInt("id"));                          // Mapping kolom "id"
        p.setNama(rs.getString("nama"));                   // Mapping kolom "nama"
        p.setKategori(rs.getString("jenis"));              // Mapping kolom "jenis" ke field kategori
        p.setUkuranTersedia(rs.getString("ukuran"));       // Mapping kolom "ukuran"
        p.setHargaSewa(rs.getDouble("harga_sewa"));        // Mapping kolom "harga_sewa"
        // Mapping kolom "gender" — kolom ini mungkin belum ada di database lama
        try {
            p.setGender(rs.getString("gender"));
        } catch (SQLException e) {
            p.setGender("Unisex"); // fallback
        }
        // Mapping kolom "deskripsi" — kolom ini mungkin belum ada di database lama
        try {
            p.setDeskripsi(rs.getString("deskripsi"));
        } catch (SQLException e) {
            p.setDeskripsi("");
        }
        // Mapping kolom "foto_data" — kolom BLOB yang mungkin belum ada di database lama
        try {
            p.setImageData(rs.getBytes("foto_data"));
        } catch (SQLException e) {
            p.setImageData(null);
        }
        // Mapping kolom "tersedia" — konversi dari int (0/1) ke boolean
        p.setTersedia(rs.getInt("tersedia") == 1);
        return p;
    }
}
