package dataaccess;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private static final String DB_PROPERTIES = "db.properties";

    private static Properties loadProperties() throws DataAccessException {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream(DB_PROPERTIES)) {

            if (input == null) {
                throw new FileNotFoundException("Unable to find " + DB_PROPERTIES);
            }

            Properties properties = new Properties();
            properties.load(input);
            return properties;

        } catch (Exception e) {
            throw new DataAccessException("Unable to load database properties");
        }
    }

    // In DatabaseManager.java, add:
    public static void createDatabase() throws DataAccessException {
        try {
            Properties properties = loadProperties();
            String url = properties.getProperty("connection.url");
            // Connect without specifying the DB name
            String baseUrl = url.substring(0, url.lastIndexOf("/"));
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            String user = properties.getProperty("connection.user");
            String password = properties.getProperty("connection.password");

            try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
                 PreparedStatement ps = conn.prepareStatement(
                         "CREATE DATABASE IF NOT EXISTS `" + dbName + "`")) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create database");
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Properties properties = loadProperties();

            String url = properties.getProperty("connection.url");
            String user = properties.getProperty("connection.user");
            String password = properties.getProperty("connection.password");

            return DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect to database");
        }
    }
}