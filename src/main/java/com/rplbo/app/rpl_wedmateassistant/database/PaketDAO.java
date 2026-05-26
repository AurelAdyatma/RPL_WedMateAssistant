package com.rplbo.app.rpl_wedmateassistant.database;

// Import model PaketSewa yang dikelola oleh DAO ini
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;

// Import kelas JDBC untuk koneksi dan query database
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Import ArrayList dan List untuk menyimpan hasil query
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel paket_sewa di SQLite.
 * Menyediakan operasi CRUD untuk PaketSewa.
 *
 * <p>Operasi yang tersedia:</p>
 * <ul>
 *   <li>{@link #findAll()} — mengambil semua paket sewa dari database</li>
 *   <li>{@link #findById(int)} — mengambil paket berdasarkan ID</li>
 *   <li>{@link #save(PaketSewa)} — menyimpan paket baru ke database</li>
 *   <li>{@link #update(PaketSewa)} — memperbarui data paket yang sudah ada</li>
 *   <li>{@link #delete(int)} — menghapus paket berdasarkan ID</li>
 * </ul>
 *
 * <p>Setiap method menggunakan try-with-resources untuk memastikan
 * Connection, PreparedStatement, dan ResultSet ditutup secara otomatis.</p>
 */
public class PaketDAO {

    /** Referensi ke DatabaseManager singleton untuk mendapatkan koneksi database */
    private final DatabaseManager dbManager;

    /**
     * Constructor — menginisialisasi DAO dengan instance DatabaseManager singleton.
     */
    public PaketDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Mengambil semua paket sewa dari tabel paket_sewa.
     *
     * @return daftar semua PaketSewa, atau list kosong jika tidak ada data
     */
    /** Mengembalikan semua paket sewa. */
    public List<PaketSewa> findAll() {
        // Inisialisasi list kosong untuk menampung hasil query
        List<PaketSewa> list = new ArrayList<>();
        // Query SQL untuk mengambil semua baris dari tabel paket_sewa
        String sql = "SELECT * FROM paket_sewa";
        // Menggunakan try-with-resources agar koneksi otomatis ditutup
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // Iterasi setiap baris hasil query dan mapping ke objek PaketSewa
            while (rs.next()) {
                list.add(mapToPaket(rs));
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil satu paket sewa berdasarkan ID.
     *
     * @param id ID paket yang dicari
     * @return objek PaketSewa jika ditemukan, atau null jika tidak ada
     */
    /** Mengembalikan paket berdasarkan ID. */
    public PaketSewa findById(int id) {
        // Query SQL dengan parameter WHERE untuk mencari berdasarkan ID
        String sql = "SELECT * FROM paket_sewa WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter ID pada prepared statement (index 1)
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // Jika ada baris hasil, mapping ke objek PaketSewa
                if (rs.next()) {
                    return mapToPaket(rs);
                }
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        // Return null jika paket dengan ID tersebut tidak ditemukan
        return null;
    }

    /**
     * Menyimpan paket sewa baru ke database.
     *
     * @param paket objek PaketSewa yang akan disimpan
     * @return true jika berhasil menyimpan (minimal 1 baris terpengaruh), false jika gagal
     */
    public boolean save(PaketSewa paket) {
        // Query SQL INSERT untuk menambahkan data paket baru
        String sql = "INSERT INTO paket_sewa (nama_paket, deskripsi, harga, fasilitas, tersedia) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, paket.getNamaPaket());     // Nama paket
            stmt.setString(2, paket.getDeskripsi());       // Deskripsi paket
            stmt.setDouble(3, paket.getHargaTotal());      // Harga total
            stmt.setString(4, "Fasilitas Default"); // Simplified
            stmt.setInt(5, 1);                             // Status tersedia (1 = tersedia)
            // Eksekusi INSERT dan cek apakah berhasil (baris terpengaruh > 0)
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Memperbarui data paket sewa yang sudah ada di database.
     *
     * @param paket objek PaketSewa dengan data yang sudah diperbarui (harus memiliki ID valid)
     * @return true jika berhasil memperbarui, false jika gagal atau ID tidak ditemukan
     */
    public boolean update(PaketSewa paket) {
        // Query SQL UPDATE untuk mengubah data paket berdasarkan ID
        String sql = "UPDATE paket_sewa SET nama_paket = ?, deskripsi = ?, harga = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, paket.getNamaPaket());     // Nama paket baru
            stmt.setString(2, paket.getDeskripsi());       // Deskripsi baru
            stmt.setDouble(3, paket.getHargaTotal());      // Harga baru
            stmt.setInt(4, paket.getId());                 // ID paket yang diupdate (WHERE clause)
            // Eksekusi UPDATE dan cek apakah ada baris yang terpengaruh
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Menghapus paket sewa dari database berdasarkan ID.
     *
     * @param id ID paket yang akan dihapus
     * @return true jika berhasil menghapus, false jika gagal atau ID tidak ditemukan
     */
    public boolean delete(int id) {
        // Query SQL DELETE untuk menghapus paket berdasarkan ID
        String sql = "DELETE FROM paket_sewa WHERE id = ?";
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
     * Helper method untuk mapping satu baris ResultSet menjadi objek PaketSewa.
     * Mengambil kolom-kolom dari tabel paket_sewa dan mengisi atribut objek.
     *
     * @param rs ResultSet yang posisinya sudah berada di baris yang akan dimapping
     * @return objek PaketSewa yang sudah terisi data
     * @throws SQLException jika terjadi error saat membaca kolom dari ResultSet
     */
    private PaketSewa mapToPaket(ResultSet rs) throws SQLException {
        PaketSewa p = new PaketSewa();
        p.setId(rs.getInt("id"));                    // Mapping kolom "id"
        p.setNamaPaket(rs.getString("nama_paket"));  // Mapping kolom "nama_paket"
        p.setDeskripsi(rs.getString("deskripsi"));   // Mapping kolom "deskripsi"
        p.setHargaTotal(rs.getDouble("harga"));      // Mapping kolom "harga"
        return p;
    }
}
