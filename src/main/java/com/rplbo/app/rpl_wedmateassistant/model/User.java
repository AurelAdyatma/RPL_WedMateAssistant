package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Representasi entitas pengguna (tamu/pelanggan) dalam sistem WedMate Assistant.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String nama;
    private String email;
    private String noTelepon;

    public User() {}

    public User(int id, String username, String password, String nama, String email, String noTelepon) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.email = email;
        this.noTelepon = noTelepon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
}
