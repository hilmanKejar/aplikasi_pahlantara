// Path: app/src/main/java/com/example/uts/Pahlawan.java
package com.example.aplikasi_pahlantara;

import java.io.Serializable;

public class Pahlawan implements Serializable {
    private int id; // ID dari API atau DB lokal
    private String name;
    private String shortStory;
    private String fullStory;
    private String fotoPath; // Bisa URL atau path lokal
    private String source; // "api" atau "local"
    private String creatorUsername; // Siapa yang membuat data ini

    // Konstruktor untuk data baru (tanpa ID, ID akan dibuat oleh API/DB)
    public Pahlawan(String name, String shortStory, String fullStory, String fotoPath, String source, String creatorUsername) {
        this.name = name;
        this.shortStory = shortStory;
        this.fullStory = fullStory;
        this.fotoPath = fotoPath;
        this.source = source;
        this.creatorUsername = creatorUsername;
    }

    // Konstruktor untuk data yang sudah ada (dengan ID)
    public Pahlawan(int id, String name, String shortStory, String fullStory, String fotoPath, String source, String creatorUsername) {
        this.id = id;
        this.name = name;
        this.shortStory = shortStory;
        this.fullStory = fullStory;
        this.fotoPath = fotoPath;
        this.source = source;
        this.creatorUsername = creatorUsername;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortStory() {
        return shortStory;
    }

    public String getFullStory() {
        return fullStory;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public String getSource() {
        return source;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    // Setters (jika diperlukan, terutama untuk ID setelah POST ke API)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortStory(String shortStory) {
        this.shortStory = shortStory;
    }

    public void setFullStory(String fullStory) {
        this.fullStory = fullStory;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }
}