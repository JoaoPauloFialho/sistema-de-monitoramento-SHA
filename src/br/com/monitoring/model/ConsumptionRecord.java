package br.com.monitoring.model;

import java.time.LocalDateTime;

public class ConsumptionRecord {
    private Meter meter;
    private double value;
    private LocalDateTime timestamp;
    private String imagePath;

    public ConsumptionRecord(Meter meter, double value, String imagePath) {
        this.meter = meter;
        this.value = value;
        this.imagePath = imagePath;
        this.timestamp = LocalDateTime.now();
    }

    public Meter getMeter() {
        return meter;
    }

    public double getValue() {
        return value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return String.format("[%s] Meter %s: %.2f m3", timestamp, meter.getId(), value);
    }
}
