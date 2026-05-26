package com.rplbo.app.rpl_wedmateassistant.database;

// Import model EntriKnowledge yang dikelola oleh DAO ini
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;

// Import kelas JDBC untuk koneksi dan eksekusi query
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Import koleksi untuk menampung hasil query
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel knowledge_base di SQLite.
 * Menyediakan operasi CRUD untuk EntriKnowledge.
 *
 * <p>Tabel ini menyimpan aturan atau data basis pengetahuan (knowledge base) 
 * yang digunakan oleh ChatbotEngine untuk memberikan balasan dinamis.</p>
 */
public class KnowledgeBaseDAO {

    /** Referensi ke DatabaseManager singleton untuk mendapatkan koneksi database */
    private final DatabaseManager dbManager;

    /**
     * Constructor — menginisialisasi DAO dengan instance DatabaseManager singleton.
     */
    public KnowledgeBaseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Mengambil semua entri knowledge base dari tabel knowledge_base.
     * 
     * @return daftar semua EntriKnowledge, atau list kosong jika tidak ada data
     */
    /** Mengembalikan semua entri knowledge base dari database. */
    public List<EntriKnowledge> findAll() {
        // Inisialisasi list kosong untuk menampung hasil query
        List<EntriKnowledge> list = new ArrayList<>();
        // Query SQL untuk mengambil semua baris dari tabel knowledge_base
        String sql = "SELECT * FROM knowledge_base";
        // Menggunakan try-with-resources agar koneksi otomatis ditutup
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            // Iterasi setiap baris hasil query dan mapping ke objek EntriKnowledge
            while (rs.next()) {
                list.add(mapToEntri(rs));
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil entri knowledge base berdasarkan kategori.
     * 
     * @param kategori kategori topik knowledge base yang dicari
     * @return daftar EntriKnowledge yang sesuai kategori, atau list kosong
     */
    /** Mengembalikan entri berdasarkan kategori. */
    public List<EntriKnowledge> findByKategori(String kategori) {
        // Inisialisasi list kosong untuk hasil query
        List<EntriKnowledge> list = new ArrayList<>();
        // Query SQL dengan filter berdasarkan kolom "kategori"
        String sql = "SELECT * FROM knowledge_base WHERE kategori = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter kategori pada prepared statement
            stmt.setString(1, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                // Iterasi setiap baris hasil dan mapping ke objek
                while (rs.next()) {
                    list.add(mapToEntri(rs));
                }
            }
        } catch (SQLException e) {
            // Cetak stack trace jika terjadi error SQL
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Menyimpan entri knowledge base baru ke database.
     * 
     * @param entri objek EntriKnowledge yang akan disimpan
     * @return true jika berhasil menyimpan (minimal 1 baris terpengaruh), false jika gagal
     */
    /** Menyimpan entri baru ke database. */
    public boolean save(EntriKnowledge entri) {
        // Query SQL INSERT untuk menambahkan data knowledge base baru
        String sql = "INSERT INTO knowledge_base (pertanyaan, jawaban, kategori, aktif) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, entri.getPertanyaan());        // Pertanyaan/keyword (pola regex)
            stmt.setString(2, entri.getJawaban());           // Jawaban yang diberikan chatbot
            stmt.setString(3, entri.getKategori());          // Kategori/topik obrolan
            stmt.setInt(4, entri.isAktif() ? 1 : 0);         // Status aktif (1=aktif, 0=nonaktif)
            // Eksekusi INSERT dan cek apakah berhasil
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Memperbarui data entri knowledge base yang sudah ada di database.
     * 
     * @param entri objek EntriKnowledge dengan data yang sudah diperbarui
     * @return true jika berhasil memperbarui, false jika gagal
     */
    /** Memperbarui entri yang sudah ada. */
    public boolean update(EntriKnowledge entri) {
        // Query SQL UPDATE untuk mengubah data berdasarkan ID
        String sql = "UPDATE knowledge_base SET pertanyaan = ?, jawaban = ?, kategori = ?, aktif = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter-parameter pada prepared statement
            stmt.setString(1, entri.getPertanyaan());        // Pertanyaan/keyword baru
            stmt.setString(2, entri.getJawaban());           // Jawaban baru
            stmt.setString(3, entri.getKategori());          // Kategori baru
            stmt.setInt(4, entri.isAktif() ? 1 : 0);         // Status aktif baru
            stmt.setInt(5, entri.getId());                   // ID entri (WHERE clause)
            // Eksekusi UPDATE dan cek apakah ada baris yang terpengaruh
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Cetak stack trace dan return false jika gagal
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Menghapus entri knowledge base dari database berdasarkan ID.
     * 
     * @param id ID entri yang akan dihapus
     * @return true jika berhasil menghapus, false jika gagal
     */
    /** Menghapus entri berdasarkan ID. */
    public boolean delete(int id) {
        // Query SQL DELETE untuk menghapus entri berdasarkan ID
        String sql = "DELETE FROM knowledge_base WHERE id = ?";
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
     * Helper method untuk mapping satu baris ResultSet menjadi objek EntriKnowledge.
     * 
     * @param rs ResultSet yang posisinya sudah berada di baris yang akan dimapping
     * @return objek EntriKnowledge yang sudah terisi data
     * @throws SQLException jika terjadi error saat membaca kolom dari ResultSet
     */
    private EntriKnowledge mapToEntri(ResultSet rs) throws SQLException {
        return new EntriKnowledge(
                rs.getInt("id"),                     // Mapping kolom "id"
                rs.getString("pertanyaan"),          // Mapping kolom "pertanyaan"
                rs.getString("jawaban"),             // Mapping kolom "jawaban"
                rs.getString("kategori"),            // Mapping kolom "kategori"
                rs.getInt("aktif") == 1              // Mapping kolom "aktif" ke boolean
        );
    }
}
