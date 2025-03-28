package com.fintracker.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchemaInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    private final Connection connection;

    public DatabaseSchemaInitializer(Connection connection) {
        this.connection = connection;
    }

    public void initialize() throws SQLException {
        initializeTransactionsTable();
        // Aici poți adăuga inițializarea altor tabele în viitor
        // Ex: initializeTable("users", "sql/create_users_table.sql");
    }

    private void initializeTransactionsTable() throws SQLException {
        initializeTable("TRANSACTIONS", "sql/create_transactions_table.sql");
    }

    protected void initializeTable(String tableName, String sqlFilePath) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName.toUpperCase() + "'");
            if (rs.next() && rs.getInt(1) == 0) {
                String createTableSql = readSqlFile(sqlFilePath);
                stmt.execute(createTableSql);
                logger.info("Table {} initialized with SQL from {}", tableName, sqlFilePath);
            } else {
                logger.debug("Table {} already exists, skipping initialization", tableName);
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize table {}", tableName, e);
            throw e;
        }
    }

    private String readSqlFile(String sqlFilePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(sqlFilePath)) {
            if (inputStream == null) {
                logger.error("SQL file not found in resources: {}", sqlFilePath);
                throw new RuntimeException("SQL file not found: " + sqlFilePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }
                logger.debug("Successfully read SQL file: {}", sqlFilePath);
                return sql.toString();
            }
        } catch (IOException e) {
            logger.error("Failed to read SQL file: {}", sqlFilePath, e);
            throw new RuntimeException("Failed to read SQL file: " + sqlFilePath, e);
        }
    }

}