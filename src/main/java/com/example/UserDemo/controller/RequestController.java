package com.example.UserDemo.controller;

import com.example.UserDemo.model.Users;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class RequestController {
    private final File file = new File("C:\\Users\\John\\Documents\\coding\\All java\\Xerde-Springboot\\SpringDemo\\src\\main\\java\\com\\" +
            "example\\UserDemo\\controller\\log.txt");
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/users_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private List<Users> getUsersFromResultSet(ResultSet resultSet) throws SQLException {
        List<Users> users = new ArrayList<>();
        while (resultSet.next()) {
            // Retrieve and process data from the result set
            String userName = resultSet.getString("name");
            int age = resultSet.getInt("age");
            long account = resultSet.getLong("accountBalance");
            String location = resultSet.getString("location");
            Users user = new Users(userName, age, account, location);
            users.add(user);
        }
        return users;
    }

    public void logger(File file, String logMessage) {
        if (!file.exists()) {
            System.out.println("File does not exist");
            return;
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new java.io.FileWriter(file, true))) {
            bufferedWriter.append(logMessage);
        } catch (IOException e) {
            System.out.println("Could not write to file: " + e.getMessage());
        }
    }

    @PostMapping("/user")
    public Users saveUser(@RequestBody Users user) {
        try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users(name varchar(255), age Integer, accountBalance Integer," + " location TEXT)");

            String insertQuery = "INSERT INTO users(name, age, accountBalance, location) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, user.getName());
                preparedStatement.setInt(2, user.getAge());
                preparedStatement.setLong(3, user.getAccountBalance());
                preparedStatement.setString(4, user.getLocation());
                preparedStatement.executeUpdate();
            }

            String logMessage = String.format("User with name: %s, has been successfully added. \n", user.getName());
            logger(file, logMessage);

            return user;

        } catch (SQLException exception) {
            String logMessage = "Operation unsuccessful: " + exception.getMessage() + " \n";
            logger(file, logMessage);

            return null;
        }
    }

    @GetMapping("/users/{name}")
    public List<Users> getUsersByName(@PathVariable("name") String name) {
        try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
            String val = String.format("SELECT * FROM users WHERE name = \"%s\"", name);
            ResultSet resultSet = statement.executeQuery(val);

            if (!resultSet.isBeforeFirst()) {
                throw new SQLException("User " + name + " not found");

            } else {
                String logMessage = String.format("All users with name: %s, successfully retrieved. \n", name);
                logger(file, logMessage);
                // Process the result set here
                return getUsersFromResultSet(resultSet);
            }

        } catch (SQLException exception) {
            String logText = "Operation unsuccessful: " + exception.getMessage() + " \n";
            logger(file, logText);

            return null;
        }
    }

    @GetMapping("/user/{name}")
    public List<Users> getUserByName(@PathVariable("name") String name) {
        try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
            String val = String.format("SELECT * FROM users WHERE name =\"%s\" LIMIT 1", name);
            ResultSet resultSet = statement.executeQuery(val);

            if (!resultSet.isBeforeFirst()) {
                throw new SQLException("User " + name + " not found");
            }

            logger(file, String.format("User with name: %s, successfully retrieved. \n", name));
            return getUsersFromResultSet(resultSet);

        } catch (SQLException exception) {
            String logMessage = "Operation unsuccessful: " + exception.getMessage() + " \n";
            logger(file, logMessage);

            return null;
        }
    }

    @GetMapping("/users")
    public List<Users> getAllUsers() {
        try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");

            logger(file, "All Users successfully retrieved. \n");
            return getUsersFromResultSet(resultSet);

        } catch (SQLException exception) {
            String logText = "Operation unsuccessful: " + exception.getMessage() + " \n";
            logger(file, logText);

            return null;
        }
    }

    @GetMapping("/users/sorted")
    public List<Users> getAllSortedUsers() {
        if (getAllUsers() != null) {
            logger(file, "All Users have been sorted by their names and successfully retrieved. \n");
            return getAllUsers().stream().sorted(Comparator.comparing(Users::getName)).toList();

        } else {
            String logText = "Operation unsuccessful: no users found \n";
            logger(file, logText);

            return null;
        }
    }
}