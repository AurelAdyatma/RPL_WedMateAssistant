package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Representasi entitas pengguna (tamu/pelanggan) dalam sistem WedMate Assistant.
 *
 * <p>Kelas ini memetakan data dari tabel {@code users} di database SQLite.
 * Setiap objek merepresentasikan satu akun pengguna yang dapat melakukan
 * reservasi pakaian dan berkomunikasi dengan chatbot.</p>
 *
 * <p>Field yang tersedia:</p>
 * <ul>
 *   <li>{@code id} — primary key auto-increment</li>
 *   <li>{@code username} — nama pengguna untuk identifikasi</li>
 *   <li>{@code password} — kata sandi akun</li>
 *   <li>{@code nama} — nama lengkap pengguna</li>
 *   <li>{@code email} — alamat email pengguna</li>
 *   <li>{@code noTelepon} — nomor telepon pengguna untuk komunikasi</li>
 * </ul>
 */
public class User {
    /** ID unik pengguna (primary key dari database, auto-increment) */
    private int id;
    /** Nama pengguna (username) untuk login atau identifikasi */
    private String username;
    /** Kata sandi akun pengguna */
    private String password;
    /** Nama lengkap pengguna */
    private String nama;
    /** Alamat email pengguna */
    private String email;
    /** Nomor telepon pengguna untuk komunikasi (WhatsApp, SMS, dll.) */
    private String noTelepon;

    /** Constructor default tanpa parameter (diperlukan untuk mapping dari ResultSet) */
    public User() {}

    /**
     * Constructor lengkap dengan semua atribut.
     *
     * @param id         ID unik pengguna
     * @param username   nama pengguna
     * @param password   kata sandi
     * @param nama       nama lengkap
     * @param email      alamat email
     * @param noTelepon  nomor telepon
     */
    public User(int id, String username, String password, String nama, String email, String noTelepon) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.email = email;
        this.noTelepon = noTelepon;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    /** Mengembalikan ID unik pengguna */
    public int getId() { return id; }
    /** Mengatur ID unik pengguna */
    public void setId(int id) { this.id = id; }
    /** Mengembalikan username pengguna */
    public String getUsername() { return username; }
    /** Mengatur username pengguna */
    public void setUsername(String username) { this.username = username; }
    /** Mengembalikan password pengguna */
    public String getPassword() { return password; }
    /** Mengatur password pengguna */
    public void setPassword(String password) { this.password = password; }
    /** Mengembalikan nama lengkap pengguna */
    public String getNama() { return nama; }
    /** Mengatur nama lengkap pengguna */
    public void setNama(String nama) { this.nama = nama; }
    /** Mengembalikan alamat email pengguna */
    public String getEmail() { return email; }
    /** Mengatur alamat email pengguna */
    public void setEmail(String email) { this.email = email; }
    /** Mengembalikan nomor telepon pengguna */
    public String getNoTelepon() { return noTelepon; }
    /** Mengatur nomor telepon pengguna */
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
}
