package br.com.monitoring.service;

import br.com.monitoring.model.Meter;
import br.com.monitoring.model.User;
import java.util.List;
import java.sql.SQLException;

public class MeterService {
    private DatabaseService dbService;

    public MeterService() {
        this.dbService = DatabaseService.getInstance();
    }

    public void registerMeter(String meterId, String location, User owner) {
        try {
            Meter meter = new Meter(meterId, location, owner);
            dbService.insertMeter(meter);
            owner.addMeter(meter);
        } catch (SQLException e) {
            System.err.println("Error registering meter: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to register meter", e);
        }
    }

    public List<Meter> getAllMeters() {
        try {
            return dbService.getAllMeters();
        } catch (SQLException e) {
            System.err.println("Error getting meters: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get meters", e);
        }
    }

    public void updateMeterLastProcessedImage(String meterId, String imageName) {
        try {
            dbService.updateMeterLastProcessedImage(meterId, imageName);
        } catch (SQLException e) {
            System.err.println("Error updating meter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteMeter(String meterId) {
        try {
            dbService.deleteMeter(meterId);
        } catch (SQLException e) {
            System.err.println("Error deleting meter: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete meter", e);
        }
    }
}
