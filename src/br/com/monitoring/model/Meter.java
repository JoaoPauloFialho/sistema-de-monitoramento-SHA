package br.com.monitoring.model;

public class Meter {
    private String id;
    private String location; // Path to where images are generated
    private User owner;

    public Meter(String id, String location, User owner) {
        this.id = id;
        this.location = location;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public User getOwner() {
        return owner;
    }

    private String lastProcessedImage;

    public String getLastProcessedImage() {
        return lastProcessedImage;
    }

    public void setLastProcessedImage(String lastProcessedImage) {
        this.lastProcessedImage = lastProcessedImage;
    }

    @Override
    public String toString() {
        return "Meter{id='" + id + "', location='" + location + "'}";
    }
}
