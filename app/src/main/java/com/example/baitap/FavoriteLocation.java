package com.example.baitap;

public class FavoriteLocation {
    private String name;
    private String address;
    private String description;
    private double latitude;
    private double longitude;
    private int imageUrl;

    public FavoriteLocation(String name, String address, String description, double latitude, double longitude, int imageUrl) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getImageUrl() {
        return imageUrl;
    }
}
