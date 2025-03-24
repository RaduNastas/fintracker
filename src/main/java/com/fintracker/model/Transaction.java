package com.fintracker.model;

import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private String type; // INCOME or EXPENSE
    private double amount;
    private String category;
    private LocalDateTime date;

    public Transaction(Long id, String type, double amount, String category, LocalDateTime date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDateTime getDate() { return date; }

    @Override
    public String toString() {
        return "[" + id + "] " + type + " | " + amount + " | " + category + " | " + date;
    }
}
