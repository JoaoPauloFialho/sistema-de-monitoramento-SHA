package br.com.monitoring.ui;

import br.com.monitoring.facade.MonitoringFacade;
import br.com.monitoring.model.User;
import br.com.monitoring.model.Meter;

import java.util.Scanner;

public class CLI {
    private MonitoringFacade facade;
    private Scanner scanner;

    public CLI() {
        this.facade = new MonitoringFacade();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Water Meter Monitoring System ===");
        System.out.println("Note: Monitoring must be started manually (option 8)");
        
        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Register User");
            System.out.println("2. Register Meter");
            System.out.println("3. List Users");
            System.out.println("4. List Meters");
            System.out.println("5. Delete User");
            System.out.println("6. Discover Available Meters");
            System.out.println("7. Show User Details");
            System.out.println("8. Start Monitoring");
            System.out.println("9. Stop Monitoring");
            System.out.println("0. Exit");
            System.out.print("Select: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        registerUser();
                        break;
                    case "2":
                        registerMeter();
                        break;
                    case "3":
                        listUsers();
                        break;
                    case "4":
                        listMeters();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        discoverMeters();
                        break;
                    case "7":
                        showUserDetails();
                        break;
                    case "8":
                        startMonitoring();
                        break;
                    case "9":
                        stopMonitoring();
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void startDemoSetup() {
        System.out.println(">>> Initializing Demo Setup...");

        try {
            // Registering Rodrigues Simulator
            // Path relative to project root (MonitoringSystem is a sibling folder)
            String pathRodrigues = "../rodrigues-hidrometro/Medicoes_199911250009";
            String cpfRodrigues = "11122233344";

            System.out.println(">>> Registering User for Rodrigues Simulator...");
            facade.registerUser("Rodrigues Consumer", cpfRodrigues, "Simulator Ave, 1", 1000.0);

            // Check if directory exists, if not warn but proceed
            java.io.File dirRodrigues = new java.io.File(pathRodrigues);
            if (!dirRodrigues.exists()) {
                System.out.println("Warning: Simulator directory not found: " + dirRodrigues.getAbsolutePath());
                System.out.println("Make sure to run './run_simulators.sh' from the project root first!");
            }

            facade.registerMeter("SHA-ROD-01", dirRodrigues.getAbsolutePath(), cpfRodrigues);

            // Registering JoaoPaulo Simulator
            String pathJoao = "../joaopaulo-hidrometro/Medições_202211250019";
            String cpfJoao = "55566677788";

            System.out.println(">>> Registering User for JoaoPaulo Simulator...");
            facade.registerUser("Joao Consumer", cpfJoao, "Simulator Blvd, 2", 1000.0);

            java.io.File dirJoao = new java.io.File(pathJoao);
            if (!dirJoao.exists()) {
                System.out.println("Warning: Simulator directory not found: " + dirJoao.getAbsolutePath());
            }

            facade.registerMeter("SHA-JOAO-01", dirJoao.getAbsolutePath(), cpfJoao);

            System.out.println(">>> Demo Setup Complete. Users and Meters registered.");

        } catch (Exception e) {
            System.out.println("Error during demo setup: " + e.getMessage());
        }
    }

    private void registerUser() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Limit (m3): ");
        double limit = Double.parseDouble(scanner.nextLine());

        facade.registerUser(name, cpf, address, limit);
        System.out.println("User registered.");
    }

    private void registerMeter() {
        System.out.print("Meter ID: ");
        String id = scanner.nextLine();
        System.out.print("Images Path (Absolute): ");
        String path = scanner.nextLine();
        System.out.print("User CPF: ");
        String cpf = scanner.nextLine();

        facade.registerMeter(id, path, cpf);
        System.out.println("Meter registered.");
    }

    private void listUsers() {
        java.util.List<User> users = facade.listUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered.");
            return;
        }
        
        System.out.println("\n=== Registered Users ===");
        for (User u : users) {
            double consumption = facade.getCurrentConsumption(u);
            boolean limitExceeded = facade.isLimitExceeded(u);
            System.out.println("\nUser: " + u.getName());
            System.out.println("  CPF: " + u.getCpf());
            System.out.println("  Address: " + u.getAddress());
            System.out.println("  Consumption: " + String.format("%.2f m³", consumption));
            System.out.println("  Limit: " + String.format("%.2f m³", u.getConsumptionLimit()));
            System.out.println("  Status: " + (limitExceeded ? "⚠️ LIMIT EXCEEDED" : "✓ OK"));
            System.out.println("  Meters:");
            for (Meter m : u.getMeters()) {
                System.out.println("    - " + m.getId() + " @ " + m.getLocation());
            }
        }
    }

    private void listMeters() {
        java.util.List<Meter> meters = facade.listMeters();
        if (meters.isEmpty()) {
            System.out.println("No meters registered.");
            return;
        }
        
        System.out.println("\n=== Registered Meters ===");
        for (Meter m : meters) {
            System.out.println("ID: " + m.getId());
            System.out.println("  Location: " + m.getLocation());
            System.out.println("  Owner: " + m.getOwner().getName() + " (CPF: " + m.getOwner().getCpf() + ")");
            System.out.println("  Last Processed: " + (m.getLastProcessedImage() != null ? m.getLastProcessedImage() : "None"));
        }
    }

    private void deleteUser() {
        System.out.print("Enter CPF to delete: ");
        String cpf = scanner.nextLine();
        
        try {
            facade.deleteUser(cpf);
            System.out.println("User deleted successfully.");
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    private void discoverMeters() {
        java.util.List<br.com.monitoring.service.MeterDiscoveryService.DetectedMeter> meters = facade.discoverMeters();
        if (meters.isEmpty()) {
            System.out.println("No meters discovered. Make sure simulators are running.");
            return;
        }
        
        System.out.println("\n=== Discovered Meters ===");
        for (br.com.monitoring.service.MeterDiscoveryService.DetectedMeter m : meters) {
            System.out.println("Label: " + m.getLabel());
            System.out.println("  Suggested ID: " + m.getSuggestedId());
            System.out.println("  Path: " + m.getPath());
            System.out.println();
        }
    }

    private void showUserDetails() {
        System.out.print("Enter CPF: ");
        String cpf = scanner.nextLine();
        
        java.util.List<User> users = facade.listUsers();
        User found = users.stream()
            .filter(u -> u.getCpf().equals(cpf))
            .findFirst()
            .orElse(null);
        
        if (found == null) {
            System.out.println("User not found.");
            return;
        }
        
        double consumption = facade.getCurrentConsumption(found);
        boolean limitExceeded = facade.isLimitExceeded(found);
        
        System.out.println("\n=== User Details ===");
        System.out.println("Name: " + found.getName());
        System.out.println("CPF: " + found.getCpf());
        System.out.println("Address: " + found.getAddress());
        System.out.println("Consumption: " + String.format("%.2f m³", consumption));
        System.out.println("Limit: " + String.format("%.2f m³", found.getConsumptionLimit()));
        System.out.println("Status: " + (limitExceeded ? "⚠️ LIMIT EXCEEDED" : "✓ OK"));
        System.out.println("Meters: " + found.getMeters().size());
    }

    private void startMonitoring() {
        if (facade.isMonitoringActive()) {
            System.out.println("Monitoring is already active.");
            return;
        }
        facade.startMonitoring();
        System.out.println("Monitoring started. Checking meters every 1 seconds...");
    }

    private void stopMonitoring() {
        if (!facade.isMonitoringActive()) {
            System.out.println("Monitoring is already stopped.");
            return;
        }
        facade.stopMonitoring();
        System.out.println("Monitoring stopped.");
    }
}
