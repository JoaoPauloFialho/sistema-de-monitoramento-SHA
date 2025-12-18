package br.com.monitoring.service;

import br.com.monitoring.model.Meter;
import br.com.monitoring.model.User;
import br.com.monitoring.model.ConsumptionRecord;
import br.com.monitoring.ocr.OCRAdapter;
import br.com.monitoring.ocr.TesseractCLIAdapter;
import br.com.monitoring.observer.ConsumptionObserver;
import br.com.monitoring.log.AuditLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.sql.SQLException;

public class MonitoringService {
    private OCRAdapter ocrAdapter;
    private List<ConsumptionObserver> observers = new ArrayList<>();
    private DatabaseService dbService;
    private MeterService meterService;

    public MonitoringService() {
        // Using CLI Adapter by default for simplicity as requested
        // Requires 'tesseract' installed on the OS
        this.ocrAdapter = new TesseractCLIAdapter();
        this.dbService = DatabaseService.getInstance();
    }

    public void setMeterService(MeterService meterService) {
        this.meterService = meterService;
    }

    public void addObserver(ConsumptionObserver observer) {
        observers.add(observer);
    }

    public void checkMeters(List<Meter> meters) {
        System.out.println("[MONITORING] Checking " + meters.size() + " meters...");
        for (Meter meter : meters) {
            System.out.println("[MONITORING] Meter ID: " + meter.getId() + ", Location: " + meter.getLocation() + ", Owner: " + meter.getOwner().getName());
            processMeter(meter);
        }
    }

    private void processMeter(Meter meter) {
        // Store meter identification at the start to ensure consistency
        String meterId = meter.getId();
        String meterLocation = meter.getLocation();
        String ownerCpf = meter.getOwner().getCpf();
        String ownerName = meter.getOwner().getName();
        
        // Log which meter we're processing
        System.out.println("[MONITORING] Processing meter: " + meterId + " for user: " + ownerName + " (CPF: " + ownerCpf + ")");
        System.out.println("[MONITORING] Meter location: " + meterLocation);
        
        File dir = new File(meterLocation);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("[MONITORING] Meter location invalid for " + meterId + ": " + meterLocation);
            return;
        }

        // Find the latest image
        File[] images = dir
                .listFiles((d, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".jpg"));
        if (images == null || images.length == 0)
            return;

        // Get latest
        Arrays.sort(images, Comparator.comparingLong(File::lastModified).reversed());
        File latestImage = images[0];

        // CHECK: If we already processed this image, skip
        String lastProcessed = meter.getLastProcessedImage();
        if (lastProcessed != null && latestImage.getName().equals(lastProcessed)) {
            return;
        }

        try {
            double value = 0.0;
            boolean usedHeuristic = false;

            // DEMO HEURISTIC:
            // Rodrigues simulator: files named "00.jpeg", "01.jpeg", etc. where the number
            // represents m³ directly (m3Atual % 100). So "56.jpeg" = 56 m³ (or 156, 256, etc)
            // Since it's modulo 100, we can't know the exact value if > 100, but we use the file number
            // JoaoPaulo simulator: files named "1.jpeg", "2.jpeg", etc. are sequential counters.
            // The display shows volume in format "010156" which means 101.56 m³ (first 4 digits = integer, last 2 = decimal)
            // For JoaoPaulo, we need to use OCR to read the actual display value
            if (latestImage.getName().matches("^\\d+\\.(jpeg|jpg)$")) {
                try {
                    String numberPart = latestImage.getName().replaceAll("[^0-9]", "");
                    int fileNumber = Integer.parseInt(numberPart);
                    
                    // Check if it's a 2-digit file (Rodrigues format: 00-99)
                    // Rodrigues uses modulo 100, so files are 00-99 representing m³
                    if (fileNumber <= 99 && numberPart.length() <= 2) {
                        // Rodrigues format: number represents m³ directly (modulo 100)
                        // We use the file number as the m³ value
                        value = (double) fileNumber;
                        usedHeuristic = true;
                    } else {
                        // JoaoPaulo format: sequential counter (1, 2, 3...)
                        // Can't use filename, must use OCR to read display
                        usedHeuristic = false;
                    }
                } catch (NumberFormatException e) {
                    usedHeuristic = false;
                }
            }

            // If we didn't use the heuristic (or it failed), try OCR
            if (!usedHeuristic) {
                value = ocrAdapter.readConsumption(latestImage);
            }

            // Calculate accumulated consumption (handle reset detection)
            // IMPORTANT: Each meter has its own independent consumption tracking
            // We use meterId (defined at method start) to uniquely identify which meter we're updating
            
            double accumulatedConsumption = 0.0;
            try {
                // Get consumption data for THIS SPECIFIC METER (by meter ID)
                // This ensures we're updating the correct meter's consumption
                double lastAccumulated = dbService.getAccumulatedConsumption(meterId);
                double lastReading = dbService.getLastReadingValue(meterId);
                
                System.out.println("[MONITORING]   Processing meter: " + meterId);
                System.out.println("[MONITORING]   Location: " + meterLocation);
                System.out.println("[MONITORING]   Owner: " + ownerName + " (CPF: " + ownerCpf + ")");
                System.out.println("[MONITORING]   Last reading: " + String.format("%.2f", lastReading) + " m³");
                System.out.println("[MONITORING]   Last accumulated: " + String.format("%.2f", lastAccumulated) + " m³");
                System.out.println("[MONITORING]   New reading from image: " + String.format("%.2f", value) + " m³");
                
                // Detect reset: Rodrigues simulator uses modulo 100 (00-99)
                // If value decreased significantly (e.g., from 99 to 0-10), it's a reset
                boolean isReset = false;
                if (lastReading > 0) {
                    // Reset detection: value dropped significantly (more than 50 units)
                    // and new value is small (< 50), indicating modulo 100 reset
                    if (value < lastReading - 50.0 && value < 50.0 && lastReading >= 50.0) {
                        isReset = true;
                    }
                }
                
                if (isReset) {
                    // Reset detected - Rodrigues passed 100, add 100 to accumulated
                    // The new value (0-99) represents the new cycle
                    accumulatedConsumption = lastAccumulated + 100.0 + value;
                } else if (value >= lastReading) {
                    // Normal increment (value increased)
                    double increment = value - lastReading;
                    accumulatedConsumption = lastAccumulated + increment;
                } else {
                    // Value decreased slightly (might be OCR error or JoaoPaulo)
                    // For JoaoPaulo, consumption should only increase, so this is likely an error
                    // Keep the accumulated value and use the higher reading
                    if (value > lastReading - 10.0) {
                        // Small decrease, likely OCR error - keep accumulated
                        accumulatedConsumption = lastAccumulated;
                    } else {
                        // Significant decrease but not a reset - use current value as new base
                        accumulatedConsumption = value;
                    }
                }
                
                // Update meter consumption tracking FOR THIS SPECIFIC METER
                // Using meterId ensures we update the correct meter in the database
                dbService.updateMeterConsumption(meterId, value, accumulatedConsumption);
                
                System.out.println("[MONITORING]   Updated accumulated for meter " + meterId + ": " + String.format("%.2f", accumulatedConsumption) + " m³");
                
                // Save consumption record with accumulated value (linked to this specific meter by ID)
                dbService.insertConsumptionRecord(meterId, accumulatedConsumption, latestImage.getName());
            } catch (SQLException e) {
                System.err.println("Error updating consumption: " + e.getMessage());
                e.printStackTrace();
                // Fallback: use current value as accumulated (using meterId)
                try {
                    dbService.updateMeterConsumption(meterId, value, value);
                    accumulatedConsumption = value;
                } catch (SQLException e2) {
                    accumulatedConsumption = value;
                }
            }

            ConsumptionRecord record = new ConsumptionRecord(meter, accumulatedConsumption, latestImage.getName());

            // Notify observers (using the meter's owner)
            notifyObservers(meter.getOwner(), record);

            // Mark as processed and update in database (using meterId for consistency)
            meter.setLastProcessedImage(latestImage.getName());
            if (meterService != null) {
                meterService.updateMeterLastProcessedImage(meterId, latestImage.getName());
            }

            AuditLogger.getInstance().log(
                    "Read meter " + meterId + " (owner: " + ownerName + "): " + String.format("%.2f", value) + " m³ -> accumulated: " + String.format("%.2f", accumulatedConsumption) + " m³ " + (usedHeuristic ? "(Heuristic)" : "(OCR)"));
        } catch (Exception e) {
            AuditLogger.getInstance().log("Error reading meter " + meterId + " (location: " + meterLocation + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyObservers(User user, ConsumptionRecord record) {
        for (ConsumptionObserver observer : observers) {
            observer.onNewReading(user, record);
        }
    }

    public double getCurrentConsumption(User user) {
        double total = 0.0;
        try {
            // Sum consumption from ALL meters belonging to THIS user
            for (Meter meter : user.getMeters()) {
                // Get accumulated consumption for THIS SPECIFIC METER
                double accumulated = dbService.getAccumulatedConsumption(meter.getId());
                System.out.println("[CONSUMPTION] User " + user.getName() + " - Meter " + meter.getId() + " consumption: " + String.format("%.2f", accumulated) + " m³");
                total += accumulated;
            }
            System.out.println("[CONSUMPTION] User " + user.getName() + " - Total consumption: " + String.format("%.2f", total) + " m³");
        } catch (SQLException e) {
            System.err.println("Error getting current consumption for user " + user.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public boolean isLimitExceeded(User user) {
        return getCurrentConsumption(user) > user.getConsumptionLimit();
    }
}
