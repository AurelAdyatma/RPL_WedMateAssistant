package com.rplbo.app.rpl_wedmateassistant.database;

import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel knowledge_base di SQLite.
 * Menyediakan operasi CRUD untuk EntriKnowledge.
 */
public class KnowledgeBaseDAO {

    private final DatabaseManager dbManager;

    public KnowledgeBaseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Mengembalikan semua entri knowledge base dari database. */
    public List<EntriKnowledge> findAll() {
        List<EntriKnowledge> list = new ArrayList<>();
        String sql = "SELECT * FROM knowledge_base";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapToEntri(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengembalikan entri berdasarkan kategori. */
    public List<EntriKnowledge> findByKategori(String kategori) {
        List<EntriKnowledge> list = new ArrayList<>();
        String sql = "SELECT * FROM knowledge_base WHERE kategori = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToEntri(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Menyimpan entri baru ke database. */
    public boolean save(EntriKnowledge entri) {
        String sql = "INSERT INTO knowledge_base (pertanyaan, jawaban, kategori, aktif) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entri.getPertanyaan());
            stmt.setString(2, entri.getJawaban());
            stmt.setString(3, entri.getKategori());
            stmt.setInt(4, entri.isAktif() ? 1 : 0);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Memperbarui entri yang sudah ada. */
    public boolean update(EntriKnowledge entri) {
        String sql = "UPDATE knowledge_base SET pertanyaan = ?, jawaban = ?, kategori = ?, aktif = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entri.getPertanyaan());
            stmt.setString(2, entri.getJawaban());
            stmt.setString(3, entri.getKategori());
            stmt.setInt(4, entri.isAktif() ? 1 : 0);
            stmt.setInt(5, entri.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Menghapus entri berdasarkan ID. */
    public boolean delete(int id) {
        String sql = "DELETE FROM knowledge_base WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private EntriKnowledge mapToEntri(ResultSet rs) throws SQLException {
        return new EntriKnowledge(
                rs.getInt("id"),
                rs.getString("pertanyaan"),
                rs.getString("jawaban"),
                rs.getString("kategori"),
                rs.getInt("aktif") == 1
        );
    }
}
