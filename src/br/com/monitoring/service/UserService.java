package br.com.monitoring.service;

import br.com.monitoring.model.User;
import java.util.List;
import java.sql.SQLException;

public class UserService {
    private DatabaseService dbService;

    public UserService() {
        this.dbService = DatabaseService.getInstance();
    }

    public void addUser(User user) {
        try {
            dbService.insertUser(user);
            // Load meters for this user from database
            loadUserMeters(user);
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add user", e);
        }
    }

    public List<User> getAllUsers() {
        try {
            List<User> users = dbService.getAllUsers();
            // Load meters for each user
            for (User user : users) {
                loadUserMeters(user);
            }
            return users;
        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get users", e);
        }
    }

    public User findByCpf(String cpf) {
        try {
            User user = dbService.findUserByCpf(cpf);
            if (user != null) {
                loadUserMeters(user);
            }
            return user;
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to find user", e);
        }
    }

    public void deleteUser(String cpf) {
        try {
            dbService.deleteUser(cpf);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private void loadUserMeters(User user) {
        try {
            List<br.com.monitoring.model.Meter> meters = dbService.getMetersByUserCpf(user.getCpf());
            for (br.com.monitoring.model.Meter meter : meters) {
                user.addMeter(meter);
            }
        } catch (SQLException e) {
            System.err.println("Error loading meters for user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
