package com.fintracker.model;

import java.time.LocalDateTime;

public class Transaction {
    private long id;
    private String type;
    private double amount;
    private String category;
    private LocalDateTime date;

    public Transaction(long id, String type, double amount, String category, LocalDateTime date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %.2f | %s | %s",
                id, type, amount, category, date);
    }

}