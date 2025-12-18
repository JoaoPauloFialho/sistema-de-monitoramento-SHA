package br.com.monitoring.facade;

import br.com.monitoring.model.User;
import br.com.monitoring.service.UserService;
import br.com.monitoring.service.MeterService;
import br.com.monitoring.service.MonitoringService;
import br.com.monitoring.service.MeterDiscoveryService;
import br.com.monitoring.observer.AlertSystem;
import br.com.monitoring.log.AuditLogger;

import java.util.List;

public class MonitoringFacade {
    private UserService userService;
    private MeterService meterService;
    private MonitoringService monitoringService;
    private MeterDiscoveryService discoveryService;
    private AlertSystem alertSystem;
    private Thread monitoringThread;
    private volatile boolean isMonitoringActive = false;

    public MonitoringFacade() {
        this.userService = new UserService();
        this.meterService = new MeterService();
        this.monitoringService = new MonitoringService();
        this.discoveryService = new MeterDiscoveryService();
        this.alertSystem = new AlertSystem();

        // Connect meterService to monitoringService
        this.monitoringService.setMeterService(this.meterService);

        // Setup default observers
        this.monitoringService.addObserver(alertSystem);
    }

    public void setEmailNotificationsEnabled(boolean enabled) {
        alertSystem.setEmailEnabled(enabled);
    }

    public List<MeterDiscoveryService.DetectedMeter> discoverMeters() {
        return discoveryService.discoverMeters();
    }

    public void registerUser(String name, String cpf, String address, double limit) {
        User user = new User(name, cpf, address, limit);
        userService.addUser(user);
        AuditLogger.getInstance().log("Registered user: " + name);
    }

    public void registerMeter(String meterId, String imagePath, String userCpf) {
        User user = userService.findByCpf(userCpf);
        if (user == null) {
            throw new IllegalArgumentException("User not found with CPF: " + userCpf);
        }
        meterService.registerMeter(meterId, imagePath, user);
        AuditLogger.getInstance().log("Registered meter " + meterId + " for user " + user.getName());
    }

    public int runMonitoringCycle() {
        monitoringService.checkMeters(meterService.getAllMeters());
        return meterService.getAllMeters().size();
    }

    public List<User> listUsers() {
        return userService.getAllUsers();
    }

    public List<br.com.monitoring.model.Meter> listMeters() {
        return meterService.getAllMeters();
    }

    public void startMonitoring() {
        if (isMonitoringActive) {
            return; // Already monitoring
        }
        
        isMonitoringActive = true;
        monitoringThread = new Thread(() -> {
            while (isMonitoringActive) {
                try {
                    runMonitoringCycle();
                    Thread.sleep(2000); // Check every 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in monitoring cycle: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        monitoringThread.setDaemon(true);
        monitoringThread.start();
        AuditLogger.getInstance().log("Monitoring started");
    }

    public void stopMonitoring() {
        if (!isMonitoringActive) {
            return; // Already stopped
        }
        
        isMonitoringActive = false;
        if (monitoringThread != null) {
            monitoringThread.interrupt();
        }
        AuditLogger.getInstance().log("Monitoring stopped");
    }

    public boolean isMonitoringActive() {
        return isMonitoringActive;
    }

    public void deleteUser(String cpf) {
        try {
            userService.deleteUser(cpf);
            AuditLogger.getInstance().log("Deleted user with CPF: " + cpf);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public double getCurrentConsumption(User user) {
        return monitoringService.getCurrentConsumption(user);
    }

    public boolean isLimitExceeded(User user) {
        return monitoringService.isLimitExceeded(user);
    }

    public void exportDatabase(String exportPath) {
        try {
            java.io.File sourceDb = new java.io.File("monitoring.db");
            if (!sourceDb.exists()) {
                throw new RuntimeException("Database file not found: monitoring.db");
            }
            
            java.io.File destFile = new java.io.File(exportPath);
            java.nio.file.Files.copy(
                sourceDb.toPath(),
                destFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            AuditLogger.getInstance().log("Database exported to: " + exportPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export database: " + e.getMessage(), e);
        }
    }

    public void setAlertSystemParentFrame(javax.swing.JFrame frame) {
        alertSystem.addParentFrame(frame);
    }
}
