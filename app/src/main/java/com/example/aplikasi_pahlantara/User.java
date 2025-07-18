// Path: app/src/main/java/com/example/uts/User.java
package com.example.aplikasi_pahlantara;

import java.io.Serializable; // Penting untuk dikirim via Intent

public class User implements Serializable {
    private String id; // Ubah menjadi String untuk kompatibilitas API MockAPI.io
    private String username;
    private String password;
    private String role; // "admin", "penerbit", "user"
    private boolean accPenerbit; // Status persetujuan untuk penerbit

    // Konstruktor untuk data yang sudah ada (dengan ID dari API)
    public User(String id, String username, String password, String role, boolean accPenerbit) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.accPenerbit = accPenerbit;
    }

    // Konstruktor untuk data baru (tanpa ID, ID akan dibuat oleh API)
    public User(String username, String password, String role, boolean accPenerbit) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accPenerbit = accPenerbit;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean isAccPenerbit() {
        return accPenerbit;
    }

    public void setId(String id) { // Setter untuk String ID
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAccPenerbit(boolean accPenerbit) {
        this.accPenerbit = accPenerbit;
    }
}