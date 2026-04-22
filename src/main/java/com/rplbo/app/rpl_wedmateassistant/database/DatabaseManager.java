package com.rplbo.app.rpl_wedmateassistant.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Mengelola koneksi tunggal ke database SQLite embedded (Singleton, thread-safe).
 *
 * <p>File database {@code wedmate.db} dibuat otomatis di direktori kerja aplikasi
 * saat pertama kali {@link #initDB()} dipanggil. Tidak memerlukan server database
 * terpisah karena menggunakan SQLite via driver org.xerial.sqlite-jdbc.</p>
 *
 * <p>Urutan pemakaian yang disarankan di {@code main} / {@code Application.start()}:</p>
 * <pre>
 *   DatabaseManager db = DatabaseManager.getInstance();
 *   db.initDB();
 *   Connection conn = db.getConnection();
 * </pre>
 */
public class DatabaseManager {

    // ── Konstanta ────────────────────────────────────────────────────────────

    /** Path file SQLite. Bisa diganti menjadi path absolut jika perlu. */
    private static final String DB_URL = "jdbc:sqlite:wedmate.db";

    // ── Singleton (double-checked locking, thread-safe) ──────────────────────

    private static volatile DatabaseManager instance;
    private Connection connection;

    /** Konstruktor privat — akses hanya melalui {@link #getInstance()}. */
    private DatabaseManager() {}

    /**
     * Mengembalikan instance tunggal {@code DatabaseManager}.
     * Aman dipanggil dari beberapa thread secara bersamaan.
     *
     * @return instance {@code DatabaseManager}
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // ── Koneksi ──────────────────────────────────────────────────────────────

    /**
     * Mengembalikan koneksi aktif ke database SQLite.
     * Jika koneksi belum ada atau sudah ditutup, koneksi baru akan dibuat.
     *
     * @return objek {@link Connection} yang siap dipakai
     * @throws SQLException jika driver tidak ditemukan atau file DB tidak bisa dibuat
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Aktifkan foreign key support di SQLite
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    /**
     * Menutup koneksi database dengan aman.
     * Dipanggil saat aplikasi ditutup (misalnya di {@code Application.stop()}).
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DatabaseManager] Koneksi SQLite ditutup.");
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Gagal menutup koneksi: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Memeriksa apakah koneksi database saat ini aktif.
     *
     * @return {@code true} jika koneksi terbuka dan valid
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Inisialisasi Skema ───────────────────────────────────────────────────

    /**
     * Menginisialisasi database: membuat semua tabel jika belum ada.
     *
     * <p>Tabel yang dibuat:</p>
     * <ul>
     *   <li>{@code users}           — data akun pengguna</li>
     *   <li>{@code pakaian_wedding} — katalog pakaian yang tersedia untuk sewa</li>
     *   <li>{@code paket_sewa}      — paket bundel layanan pernikahan</li>
     *   <li>{@code knowledge_base}  — basis pengetahuan chatbot (Q&amp;A)</li>
     *   <li>{@code reservasi}       — transaksi pemesanan pakaian oleh user</li>
     * </ul>
     *
     * <p>Method ini aman dipanggil berulang kali karena menggunakan
     * {@code CREATE TABLE IF NOT EXISTS}.</p>
     */
    public void initDB() {
        // DDL untuk setiap tabel
        String[] ddlStatements = {

            // ── 1. Tabel users ──────────────────────────────────────────────
            """
            CREATE TABLE IF NOT EXISTS users (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                nama           TEXT    NOT NULL,
                email          TEXT    NOT NULL UNIQUE,
                password       TEXT    NOT NULL,
                nomor_telepon  TEXT
            );
            """,

            // ── 2. Tabel pakaian_wedding ─────────────────────────────────────
            """
            CREATE TABLE IF NOT EXISTS pakaian_wedding (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                nama           TEXT    NOT NULL,
                jenis          TEXT    NOT NULL,
                ukuran         TEXT    NOT NULL,
                harga_sewa     REAL    NOT NULL DEFAULT 0,
                gender         TEXT    NOT NULL DEFAULT 'Unisex',
                tersedia       INTEGER NOT NULL DEFAULT 1,
                tgl_tersedia   TEXT
            );
            """,

            // ── 3. Tabel paket_sewa ──────────────────────────────────────────
            """
            CREATE TABLE IF NOT EXISTS paket_sewa (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                nama_paket     TEXT    NOT NULL,
                deskripsi      TEXT,
                harga          REAL    NOT NULL DEFAULT 0,
                fasilitas      TEXT,
                tersedia       INTEGER NOT NULL DEFAULT 1
            );
            """,

            // ── 4. Tabel knowledge_base ──────────────────────────────────────
            """
            CREATE TABLE IF NOT EXISTS knowledge_base (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                pertanyaan     TEXT    NOT NULL,
                jawaban        TEXT    NOT NULL,
                kategori       TEXT,
                aktif          INTEGER NOT NULL DEFAULT 1
            );
            """,

            // ── 5. Tabel reservasi ───────────────────────────────────────────
            """
            CREATE TABLE IF NOT EXISTS reservasi (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id        INTEGER NOT NULL,
                pakaian_id     INTEGER NOT NULL,
                tgl_sewa       TEXT    NOT NULL,
                tgl_kembali    TEXT    NOT NULL,
                status         TEXT    NOT NULL DEFAULT 'pending',
                FOREIGN KEY (user_id)    REFERENCES users(id)           ON DELETE CASCADE,
                FOREIGN KEY (pakaian_id) REFERENCES pakaian_wedding(id) ON DELETE RESTRICT
            );
            """
        };

        try (Statement stmt = getConnection().createStatement()) {
            // Jalankan semua DDL dalam satu transaksi agar atomik
            getConnection().setAutoCommit(false);

            for (String ddl : ddlStatements) {
                stmt.execute(ddl);
            }
            
            // Migrasi untuk database yang sudah ada
            try {
                stmt.execute("ALTER TABLE pakaian_wedding ADD COLUMN gender TEXT NOT NULL DEFAULT 'Unisex'");
            } catch (SQLException ignore) {
                // Kolom sudah ada
            }

            getConnection().commit();
            getConnection().setAutoCommit(true);

            System.out.println("[DatabaseManager] Database berhasil diinisialisasi. File: wedmate.db");

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Gagal menginisialisasi database: " + e.getMessage());
            // Rollback jika ada yang gagal di tengah jalan
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException rollbackEx) {
                System.err.println("[DatabaseManager] Rollback gagal: " + rollbackEx.getMessage());
            }
        }
    }
}
