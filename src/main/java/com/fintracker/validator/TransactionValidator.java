package com.fintracker.validator;

import com.fintracker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionValidator {
    private static final Logger logger = LoggerFactory.getLogger(TransactionValidator.class);

    public static void checkTransactionExists(long id, boolean exists) {
        if (!exists) {
            logger.warn("No transaction found with ID: {}", id);
            throw new IllegalArgumentException("No transaction found with ID: " + id);
        }
    }

    public static void validateTransactionId(long id) {
        if (id <= 0) {
            logger.warn("Invalid transaction ID: ID must be positive, but was {}", id);
            throw new IllegalArgumentException("Invalid ID: ID must be positive");
        }
    }

    public static void validateFilterParams(String type, String category, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            logger.warn("Invalid filter parameters: startDate {} is after endDate {}", startDate, endDate);
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
        if (type != null && !type.matches("^(income|expense)$")) {
            logger.warn("Invalid filter parameter: type must be 'income' or 'expense', but was '{}'", type);
            throw new IllegalArgumentException("Type must be 'income' or 'expense'");
        }
        if (category != null && category.trim().isEmpty()) {
            logger.warn("Invalid filter parameter: category is empty");
            throw new IllegalArgumentException("Category cannot be empty");
        }
    }

    public static void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            logger.warn("Invalid transaction: transaction is null");
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (transaction.getType() == null || !transaction.getType().matches("^(income|expense)$")) {
            logger.warn("Invalid transaction: type must be 'income' or 'expense', but was '{}'", transaction.getType());
            throw new IllegalArgumentException("Transaction type must be 'income' or 'expense'");
        }
        if (transaction.getAmount() < 0) {
            logger.warn("Invalid transaction: amount cannot be negative, but was {}", transaction.getAmount());
            throw new IllegalArgumentException("Transaction amount cannot be negative");
        }
        if (transaction.getCategory() == null) {
            logger.warn("Invalid transaction: category is null");
            throw new IllegalArgumentException("Transaction category cannot be null");
        } else if (transaction.getCategory().trim().isEmpty()) {
            logger.warn("Invalid transaction: category is empty");
            throw new IllegalArgumentException("Transaction category cannot be empty");
        }
        if (transaction.getDate() == null) {
            logger.warn("Invalid transaction: date is null");
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
    }

    public static void logIfTransactionsEmpty(List<Transaction> transactions, String type, String category, LocalDateTime startDate, LocalDateTime endDate) {
        if (transactions == null || transactions.isEmpty()) {
            logger.info("No transactions found for the given filters: type={}, category={}, startDate={}, endDate={}",
                    type, category, startDate, endDate);
        }
    }

}