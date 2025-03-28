package com.fintracker.service;

import com.fintracker.model.Transaction;
import com.fintracker.repository.DatabaseConfig;
import com.fintracker.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(DatabaseConfig databaseConfig) {
        this.repository = new TransactionRepository(databaseConfig);
    }

    public void addTransaction(String type, double amount, String category) {
        Transaction transaction = new Transaction(0L, type, amount, category,
                LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        repository.save(transaction);
    }

    public void deleteTransaction(long id) {
        repository.delete(id);
    }

    public void updateTransaction(Transaction transaction) {
        repository.update(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return repository.getAll();
    }

    public Transaction getTransactionById(long id) {
        return repository.findById(id);
    }

    public List<Transaction> getTransactionsByFilter(
            String type, String category, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByFilter(type, category, startDate, endDate);
    }

}