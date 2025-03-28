package com.fintracker.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_DB_PATH = "fintracker.db.path";
    private static final String DEFAULT_USER = "fintracker.db.user";
    private static final String DEFAULT_PASSWORD = "fintracker.db.password";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private Connection connection;

    static {
        loadConfiguration();
    }

    public DatabaseConfig() {
        getConnection();
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Loaded database configuration from {}", CONFIG_FILE);
            } else {
                logger.warn("{} not found in classpath. Using default values.", CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.warn("Failed to load {}. Using default values.", CONFIG_FILE, e);
        }

        dbUrl = properties.getProperty("fintracker.db.path", DEFAULT_DB_PATH);
        dbUser = properties.getProperty("fintracker.db.user", DEFAULT_USER);
        dbPassword = properties.getProperty("fintracker.db.password", DEFAULT_PASSWORD);
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                logger.info("Database connection established successfully");
            }
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw new RuntimeException("Failed to connect to database: ", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
            throw new RuntimeException("Failed to close database connection", e);
        }
    }

}