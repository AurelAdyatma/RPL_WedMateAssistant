package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Representasi entitas Admin yang dapat mengelola knowledge base dan katalog.
 */
public class Admin {
    private int id;
    private String username;
    private String password;
    private String namaLengkap;

    public Admin() {}

    public Admin(int id, String username, String password, String namaLengkap) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
}
