package com.fintracker.controller;

import com.fintracker.model.Transaction;

import java.util.List;

public class ConsoleDisplay {
    public void showMenu() {
        System.out.println("\n--- Personal Financial Tracker ---");
        System.out.println("1. Add transaction");
        System.out.println("2. Show all transactions");
        System.out.println("3. Delete transaction");
        System.out.println("4. Edit transaction");
        System.out.println("5. Filter transactions");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    public void displayTransactions(List<Transaction> transactions) {
        System.out.println("\n--- Transaction List ---");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            transactions.forEach(System.out::println);
        }
    }

    public void displayFilteredTransactions(List<Transaction> transactions) {
        System.out.println("\n--- Filtered Transactions ---");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            transactions.forEach(System.out::println);
        }
    }

    public void showAvailableTransactions(List<Transaction> transactions) {
        System.out.println("\nAvailable transactions:");
        transactions.forEach(System.out::println);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String errorMessage) {
        System.out.println("Error: " + errorMessage);
    }

}