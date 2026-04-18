package com.rplbo.app.rpl_wedmateassistant.database;

import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel paket_sewa di SQLite.
 * Menyediakan operasi CRUD untuk PaketSewa.
 */
public class PaketDAO {

    private final DatabaseManager dbManager;

    public PaketDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Mengembalikan semua paket sewa. */
    public List<PaketSewa> findAll() {
        List<PaketSewa> list = new ArrayList<>();
        String sql = "SELECT * FROM paket_sewa";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapToPaket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengembalikan paket berdasarkan ID. */
    public PaketSewa findById(int id) {
        String sql = "SELECT * FROM paket_sewa WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPaket(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(PaketSewa paket) {
        String sql = "INSERT INTO paket_sewa (nama_paket, deskripsi, harga, fasilitas, tersedia) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paket.getNamaPaket());
            stmt.setString(2, paket.getDeskripsi());
            stmt.setDouble(3, paket.getHargaTotal());
            stmt.setString(4, "Fasilitas Default"); // Simplified
            stmt.setInt(5, 1);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(PaketSewa paket) {
        String sql = "UPDATE paket_sewa SET nama_paket = ?, deskripsi = ?, harga = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paket.getNamaPaket());
            stmt.setString(2, paket.getDeskripsi());
            stmt.setDouble(3, paket.getHargaTotal());
            stmt.setInt(4, paket.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM paket_sewa WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private PaketSewa mapToPaket(ResultSet rs) throws SQLException {
        PaketSewa p = new PaketSewa();
        p.setId(rs.getInt("id"));
        p.setNamaPaket(rs.getString("nama_paket"));
        p.setDeskripsi(rs.getString("deskripsi"));
        p.setHargaTotal(rs.getDouble("harga"));
        return p;
    }
}
