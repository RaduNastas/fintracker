package com.fintracker.repository;

import com.fintracker.model.Transaction;
import com.fintracker.validator.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TransactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);
    private final DatabaseConfig databaseConfig;
    private final DatabaseSchemaInitializer schemaInitializer;

    public TransactionRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.schemaInitializer = new DatabaseSchemaInitializer(databaseConfig.getConnection());
        try {
            schemaInitializer.initialize();
            logger.info("Database schema initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private boolean doesTransactionExist(long id) {
        String sql = "SELECT 1 FROM transactions WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = databaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("Checked existence of transaction with ID {}: {}", id, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Failed to check transaction existence for ID: {}", id, e);
            throw new RuntimeException("Failed to check transaction existence for ID: " + id, e);
        }
    }

    public void save(Transaction transaction) {
        TransactionValidator.validateTransaction(transaction);
        String sql = "INSERT INTO transactions (type, amount, category, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = databaseConfig.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaction.getType());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getCategory());
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getDate()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        transaction.setId(id); // Устанавливаем сгенерированный ID в объект
                        logger.info("Transaction with ID {} saved: {} | {} | {} | {}",
                                id, transaction.getType(), transaction.getAmount(), transaction.getCategory(), transaction.getDate());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to save transaction: {} | {} | {} | {}",
                    transaction.getType(), transaction.getAmount(), transaction.getCategory(), transaction.getDate(), e);
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    public void delete(long id) {
        TransactionValidator.validateTransactionId(id);
        TransactionValidator.checkTransactionExists(id, doesTransactionExist(id));
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = databaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Transaction with ID {} deleted successfully", id);
            } else {
                logger.warn("No transaction with ID {} was deleted", id);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete transaction with ID: {}", id, e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    public void update(Transaction transaction) {
        TransactionValidator.validateTransaction(transaction);
        TransactionValidator.checkTransactionExists(transaction.getId(), doesTransactionExist(transaction.getId()));
        String sql = "UPDATE transactions SET type = ?, amount = ?, category = ?, date = ? WHERE id = ?";
        try (PreparedStatement stmt = databaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, transaction.getType());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getCategory());
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getDate()));
            stmt.setLong(5, transaction.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Transaction with ID {} updated: {} | {} | {} | {}",
                        transaction.getId(), transaction.getType(), transaction.getAmount(), transaction.getCategory(), transaction.getDate());
            } else {
                logger.warn("No transaction with ID {} was updated", transaction.getId());
            }
        } catch (SQLException e) {
            logger.error("Failed to update transaction with ID: {}", transaction.getId(), e);
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    public List<Transaction> findByFilter(String type, String category, LocalDateTime startDate, LocalDateTime endDate) {
        TransactionValidator.validateFilterParams(type, category, startDate, endDate);
        QueryBuilder.QueryParams queryParams = QueryBuilder.buildTransactionFilterQuery(type, category, startDate, endDate);

        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = databaseConfig.getConnection().prepareStatement(queryParams.sql())) {
            IntStream.range(0, queryParams.params().size())
                    .forEach(i -> {
                        try {
                            statement.setObject(i + 1, queryParams.params().get(i));
                        } catch (SQLException e) {
                            logger.error("Failed to set parameter at index {} for filter query", i + 1, e);
                            throw new RuntimeException("Failed to set parameter at index " + (i + 1), e);
                        }
                    });
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Transaction transaction = new Transaction(
                            resultSet.getLong("id"),
                            resultSet.getString("type"),
                            resultSet.getDouble("amount"),
                            resultSet.getString("category"),
                            resultSet.getTimestamp("date").toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
                    );
                    transactions.add(transaction);
                }
                logger.info("Found {} transactions matching filter: type={}, category={}, startDate={}, endDate={}",
                        transactions.size(), type, category, startDate, endDate);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve transactions with filter: type={}, category={}, startDate={}, endDate={}",
                    type, category, startDate, endDate, e);
            throw new RuntimeException("Failed to retrieve transactions", e);
        }
        TransactionValidator.logIfTransactionsEmpty(transactions, type, category, startDate, endDate);
        return transactions;
    }

    public List<Transaction> getAll() {
        List<Transaction> transactions = findByFilter(null, null, null, null);
        logger.info("Retrieved all transactions: {} records", transactions.size());
        return transactions;
    }

    public Transaction findById(long id) {
        TransactionValidator.validateTransactionId(id);
        TransactionValidator.checkTransactionExists(id, doesTransactionExist(id));
        String sql = "SELECT id, type, amount, category, date FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = databaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                Transaction transaction = new Transaction(
                        rs.getLong("id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getTimestamp("date").toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
                );
                TransactionValidator.validateTransaction(transaction);
                logger.info("Found transaction by ID {}: {} | {} | {} | {}",
                        id, transaction.getType(), transaction.getAmount(), transaction.getCategory(), transaction.getDate());
                return transaction;
            }
        } catch (SQLException e) {
            logger.error("Failed to find transaction by ID: {}", id, e);
            throw new RuntimeException("Failed to find transaction by ID", e);
        }
    }

}