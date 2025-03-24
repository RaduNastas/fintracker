package com.fintracker.repository;

import com.fintracker.model.Transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class TransactionRepository {

    private static final String DEFAULT_DB_PATH = "jdbc:h2:./data/fintarcker";
    private static final String DB_URL = System.getProperty("fintarcker.db.path", DEFAULT_DB_PATH);
    private static final String DB_USER = "Rambo";
    private static final String DB_PASSWORD = "";
    private final Connection connection;

    public TransactionRepository() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + DB_URL, e);
        }
    }

    public void initializeDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TRANSACTIONS'");
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                statement.execute("CREATE TABLE transactions (id IDENTITY, type VARCHAR(255), amount DOUBLE, category VARCHAR(255), date TIMESTAMP)");
            }
        }
    }

    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (type, amount, category, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getType());
            statement.setDouble(2, transaction.getAmount());
            statement.setString(3, transaction.getCategory());
            statement.setTimestamp(4, Timestamp.valueOf(transaction.getDate()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("No transaction found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

//    private void initializeDatabase() {
//        try (Statement stmt = connection.createStatement()) {
//
//            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TRANSACTIONS'");
//            if (rs.next() && rs.getInt(1) == 0) {
//                String schemaSql = new String(Files.readAllBytes(Paths.get("src/main/resources/schema.sql")));
//                stmt.execute(schemaSql);
//            }
//        } catch (SQLException | IOException e) {
//            throw new RuntimeException("Failed to initialize database schema", e);
//        }
//    }
//
//    public void save(Transaction transaction) {
//        Objects.requireNonNull(transaction, "Transaction cannot be null");
//        String sql = "INSERT INTO transactions (type, amount, category, date) VALUES (?, ?, ?, ?)";
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setString(1, transaction.getType());
//            stmt.setDouble(2, transaction.getAmount());
//            stmt.setString(3, transaction.getCategory());
//            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getDate()));
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to save transaction", e);
//        }
//    }
//
//    public List<Transaction> getAll() {
//        List<Transaction> transactions = new ArrayList<>();
//        String sql = "SELECT id, type, amount, category, date FROM transactions";
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//            while (rs.next()) {
//                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime()
//                        .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
//                transactions.add(new Transaction(
//                        rs.getLong("id"),
//                        rs.getString("type"),
//                        rs.getDouble("amount"),
//                        rs.getString("category"),
//                        date
//                ));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to retrieve transactions", e);
//        }
//        return transactions;
//    }
//
//    public void close() {
//        try {
//            if (connection != null && !connection.isClosed()) {
//                connection.close();
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to close database connection", e);
//        }
    //  }
}