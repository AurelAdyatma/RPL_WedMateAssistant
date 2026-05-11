package com.rplbo.app.rpl_wedmateassistant.database;

import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel pakaian_wedding di SQLite.
 * Menyediakan operasi CRUD untuk PakaianWedding.
 * Foto disimpan sebagai BLOB (byte[]) di kolom foto_data.
 */
public class PakaianDAO {

    private final DatabaseManager dbManager;

    public PakaianDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Mengembalikan semua pakaian yang tersedia. */
    public List<PakaianWedding> findAll() {
        List<PakaianWedding> list = new ArrayList<>();
        String sql = "SELECT * FROM pakaian_wedding";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapToPakaian(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengembalikan pakaian berdasarkan kategori. */
    public List<PakaianWedding> findByKategori(String kategori) {
        List<PakaianWedding> list = new ArrayList<>();
        String sql = "SELECT * FROM pakaian_wedding WHERE jenis = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToPakaian(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengembalikan pakaian berdasarkan ID. */
    public PakaianWedding findById(int id) {
        String sql = "SELECT * FROM pakaian_wedding WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPakaian(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(PakaianWedding pakaian) {
        String sql = "INSERT INTO pakaian_wedding (nama, jenis, ukuran, harga_sewa, gender, tersedia, deskripsi, foto_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pakaian.getNama());
            stmt.setString(2, pakaian.getKategori());
            stmt.setString(3, pakaian.getUkuranTersedia());
            stmt.setDouble(4, pakaian.getHargaSewa());
            stmt.setString(5, pakaian.getGender() != null ? pakaian.getGender() : "Unisex");
            stmt.setInt(6, pakaian.isTersedia() ? 1 : 0);
            stmt.setString(7, pakaian.getDeskripsi());
            stmt.setBytes(8, pakaian.getImageData());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(PakaianWedding pakaian) {
        String sql = "UPDATE pakaian_wedding SET nama = ?, jenis = ?, ukuran = ?, harga_sewa = ?, gender = ?, tersedia = ?, deskripsi = ?, foto_data = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pakaian.getNama());
            stmt.setString(2, pakaian.getKategori());
            stmt.setString(3, pakaian.getUkuranTersedia());
            stmt.setDouble(4, pakaian.getHargaSewa());
            stmt.setString(5, pakaian.getGender() != null ? pakaian.getGender() : "Unisex");
            stmt.setInt(6, pakaian.isTersedia() ? 1 : 0);
            stmt.setString(7, pakaian.getDeskripsi());
            stmt.setBytes(8, pakaian.getImageData());
            stmt.setInt(9, pakaian.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM pakaian_wedding WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Mencari pakaian berdasarkan keyword di nama atau deskripsi. */
    public List<PakaianWedding> findByNamaContaining(String keyword) {
        List<PakaianWedding> list = new ArrayList<>();
        String sql = "SELECT * FROM pakaian_wedding WHERE LOWER(nama) LIKE ? OR LOWER(deskripsi) LIKE ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeParam = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, likeParam);
            stmt.setString(2, likeParam);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToPakaian(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private PakaianWedding mapToPakaian(ResultSet rs) throws SQLException {
        PakaianWedding p = new PakaianWedding();
        p.setId(rs.getInt("id"));
        p.setNama(rs.getString("nama"));
        p.setKategori(rs.getString("jenis"));
        p.setUkuranTersedia(rs.getString("ukuran"));
        p.setHargaSewa(rs.getDouble("harga_sewa"));
        try {
            p.setGender(rs.getString("gender"));
        } catch (SQLException e) {
            p.setGender("Unisex"); // fallback
        }
        try {
            p.setDeskripsi(rs.getString("deskripsi"));
        } catch (SQLException e) {
            p.setDeskripsi("");
        }
        try {
            p.setImageData(rs.getBytes("foto_data"));
        } catch (SQLException e) {
            p.setImageData(null);
        }
        p.setTersedia(rs.getInt("tersedia") == 1);
        return p;
    }
}
