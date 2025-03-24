package com.fintracker.service;

import com.fintracker.model.Transaction;
import com.fintracker.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionService {
    private final TransactionRepository repository = new TransactionRepository();

    public void addTransaction(String type, double amount, String category) {
        if (!type.equalsIgnoreCase("INCOME") && !type.equalsIgnoreCase("EXPENSE")) {
            throw new IllegalArgumentException("Invalid type! Use INCOME or EXPENSE.");
        }
        Transaction transaction = new Transaction(null, type.toUpperCase(), amount, category, LocalDateTime.now());
        repository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return repository.getAll();
    }

}