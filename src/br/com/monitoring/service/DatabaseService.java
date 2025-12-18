package br.com.monitoring.service;

import br.com.monitoring.model.User;
import br.com.monitoring.model.Meter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:monitoring.db";
    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {
        initializeDatabase();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Make sure sqlite-jdbc.jar is in lib/");
            System.err.println("Run: ./download_sqlite.sh");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Users table
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                cpf TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                consumption_limit REAL NOT NULL
            )
        """;

        // Meters table
        String createMetersTable = """
            CREATE TABLE IF NOT EXISTS meters (
                id TEXT PRIMARY KEY,
                location TEXT NOT NULL,
                user_cpf TEXT NOT NULL,
                last_processed_image TEXT,
                accumulated_consumption REAL DEFAULT 0.0,
                last_reading_value REAL DEFAULT 0.0,
                FOREIGN KEY (user_cpf) REFERENCES users(cpf) ON DELETE CASCADE
            )
        """;

        // Consumption records table (for history)
        String createConsumptionTable = """
            CREATE TABLE IF NOT EXISTS consumption_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meter_id TEXT NOT NULL,
                value REAL NOT NULL,
                timestamp TEXT NOT NULL,
                image_path TEXT,
                FOREIGN KEY (meter_id) REFERENCES meters(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createMetersTable);
            stmt.execute(createConsumptionTable);
            
            // Migrate existing meters table if needed
            try {
                stmt.execute("ALTER TABLE meters ADD COLUMN accumulated_consumption REAL DEFAULT 0.0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE meters ADD COLUMN last_reading_value REAL DEFAULT 0.0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    // User operations
    public void insertUser(User user) throws SQLException {
        String sql = "INSERT OR REPLACE INTO users (cpf, name, address, consumption_limit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getCpf());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getAddress());
            pstmt.setDouble(4, user.getConsumptionLimit());
            pstmt.executeUpdate();
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT cpf, name, address, consumption_limit FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("name"),
                    rs.getString("cpf"),
                    rs.getString("address"),
                    rs.getDouble("consumption_limit")
                );
                users.add(user);
            }
        }
        return users;
    }

    public User findUserByCpf(String cpf) throws SQLException {
        String sql = "SELECT cpf, name, address, consumption_limit FROM users WHERE cpf = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("name"),
                        rs.getString("cpf"),
                        rs.getString("address"),
                        rs.getDouble("consumption_limit")
                    );
                }
            }
        }
        return null;
    }

    public void deleteUser(String cpf) throws SQLException {
        String sql = "DELETE FROM users WHERE cpf = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cpf);
            pstmt.executeUpdate();
        }
    }

    // Meter operations
    public void insertMeter(Meter meter) throws SQLException {
        String sql = "INSERT OR REPLACE INTO meters (id, location, user_cpf, last_processed_image, accumulated_consumption, last_reading_value) VALUES (?, ?, ?, ?, 0.0, 0.0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meter.getId());
            pstmt.setString(2, meter.getLocation());
            pstmt.setString(3, meter.getOwner().getCpf());
            pstmt.setString(4, meter.getLastProcessedImage());
            pstmt.executeUpdate();
        }
    }

    public List<Meter> getAllMeters() throws SQLException {
        List<Meter> meters = new ArrayList<>();
        String sql = """
            SELECT m.id, m.location, m.last_processed_image, m.user_cpf,
                   m.accumulated_consumption, m.last_reading_value,
                   u.name, u.address, u.consumption_limit
            FROM meters m
            JOIN users u ON m.user_cpf = u.cpf
        """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User owner = new User(
                    rs.getString("name"),
                    rs.getString("user_cpf"),
                    rs.getString("address"),
                    rs.getDouble("consumption_limit")
                );
                Meter meter = new Meter(
                    rs.getString("id"),
                    rs.getString("location"),
                    owner
                );
                meter.setLastProcessedImage(rs.getString("last_processed_image"));
                // Store accumulated consumption in meter (we'll add a field or use a map)
                meters.add(meter);
            }
        }
        return meters;
    }

    public List<Meter> getMetersByUserCpf(String userCpf) throws SQLException {
        List<Meter> meters = new ArrayList<>();
        String sql = """
            SELECT m.id, m.location, m.last_processed_image, m.user_cpf,
                   m.accumulated_consumption, m.last_reading_value,
                   u.name, u.address, u.consumption_limit
            FROM meters m
            JOIN users u ON m.user_cpf = u.cpf
            WHERE m.user_cpf = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userCpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User owner = new User(
                        rs.getString("name"),
                        rs.getString("user_cpf"),
                        rs.getString("address"),
                        rs.getDouble("consumption_limit")
                    );
                    Meter meter = new Meter(
                        rs.getString("id"),
                        rs.getString("location"),
                        owner
                    );
                    meter.setLastProcessedImage(rs.getString("last_processed_image"));
                    meters.add(meter);
                }
            }
        }
        return meters;
    }

    public void updateMeterLastProcessedImage(String meterId, String imageName) throws SQLException {
        String sql = "UPDATE meters SET last_processed_image = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, imageName);
            pstmt.setString(2, meterId);
            pstmt.executeUpdate();
        }
    }

    public double getAccumulatedConsumption(String meterId) throws SQLException {
        String sql = "SELECT accumulated_consumption FROM meters WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("accumulated_consumption");
                }
            }
        }
        return 0.0;
    }

    public double getLastReadingValue(String meterId) throws SQLException {
        String sql = "SELECT last_reading_value FROM meters WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("last_reading_value");
                }
            }
        }
        return 0.0;
    }

    public void updateMeterConsumption(String meterId, double newReading, double accumulated) throws SQLException {
        String sql = "UPDATE meters SET last_reading_value = ?, accumulated_consumption = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newReading);
            pstmt.setDouble(2, accumulated);
            pstmt.setString(3, meterId);
            pstmt.executeUpdate();
        }
    }

    public void deleteMeter(String meterId) throws SQLException {
        String sql = "DELETE FROM meters WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meterId);
            pstmt.executeUpdate();
        }
    }

    // Consumption records operations
    public void insertConsumptionRecord(String meterId, double value, String imagePath) throws SQLException {
        String sql = "INSERT INTO consumption_records (meter_id, value, timestamp, image_path) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meterId);
            pstmt.setDouble(2, value);
            pstmt.setString(3, java.time.LocalDateTime.now().toString());
            pstmt.setString(4, imagePath);
            pstmt.executeUpdate();
        }
    }

    public double getLatestConsumptionForMeter(String meterId) throws SQLException {
        String sql = "SELECT value FROM consumption_records WHERE meter_id = ? ORDER BY timestamp DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, meterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("value");
                }
            }
        }
        return 0.0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}

