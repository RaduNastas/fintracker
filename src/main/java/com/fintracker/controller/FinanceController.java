package com.fintracker.controller;

import com.fintracker.model.Transaction;
import com.fintracker.repository.DatabaseConfig;
import com.fintracker.service.TransactionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FinanceController {
    private final TransactionService service;
    private final ConsoleDisplay display;
    private final UserInputHandler inputHandler;
    private final DatabaseConfig dbConfig;

    public FinanceController() {
        this.dbConfig = new DatabaseConfig();
        this.service = new TransactionService(dbConfig);
        this.display = new ConsoleDisplay();
        this.inputHandler = new UserInputHandler(new Scanner(System.in), display);

    }

    public void start() {
        convertExistingTransactionTypesToLowerCase();

        Map<Integer, Runnable> actions = Map.of(
                1, this::addTransaction,
                2, this::displayTransactions,
                3, this::deleteTransaction,
                4, this::editTransaction,
                5, this::filterTransactions,
                6, () -> {
                    display.showMessage("Goodbye!");
                    dbConfig.closeConnection(); // Закрываем соединение при выходе
                    System.exit(0);
                }
        );

        try {
            while (true) {
                display.showMenu();
                int opt = inputHandler.readIntInput();
                inputHandler.readStringInput("");

                actions.getOrDefault(opt, () -> display
                        .showMessage("Invalid option. Please enter a number between 1 and 6.")).run();
            }
        } catch (Exception e) {
            display.showMessage("An error occurred: " + e.getMessage());
            dbConfig.closeConnection(); // Закрываем соединение в случае ошибки
        }
    }

    private void convertExistingTransactionTypesToLowerCase() {
        List<Transaction> transactions = service.getAllTransactions();
        for (Transaction transaction : transactions) {
            String type = transaction.getType();
            if (type != null && (type.equals("INCOME") || type.equals("EXPENSE"))) {
                transaction.setType(type.toLowerCase());
                service.updateTransaction(transaction);
            }
        }
        display.showMessage("Converted existing transaction types to lowercase.");
    }

    private void addTransaction() {
        String type = inputHandler.readType();
        if (type == null) return;

        Double amount = inputHandler.readAmount();
        if (amount == null) return;

        String category = inputHandler.readCategory();
        if (category == null) return;

        try {
            service.addTransaction(type, amount, category);
            display.showMessage("Transaction added successfully!");
        } catch (IllegalArgumentException e) {
            display.showError(e.getMessage());
        }
    }

    private void deleteTransaction() {
        List<Transaction> transactions = service.getAllTransactions();
        if (transactions.isEmpty()) {
            display.showMessage("No transactions available to delete.");
            return;
        }

        display.showAvailableTransactions(transactions);
        String input = inputHandler.readStringInput("Enter transaction ID to delete or 'cancel' to abort: ");
        if (input.equalsIgnoreCase("cancel")) {
            display.showMessage("Deletion cancelled.");
            return;
        }

        try {
            long id = Long.parseLong(input);
            Transaction transaction = service.getTransactionById(id);
            if (transaction == null) {
                display.showError("Transaction not found.");
                display.showMessage("Please try again or enter 'cancel' to abort.");
                deleteTransaction();
                return;
            }

            String confirmation = inputHandler.readConfirmation("Are you sure you want to delete transaction with ID " + id + "? (yes/no): ");
            if (!confirmation.equals("yes")) {
                display.showMessage("Deletion cancelled.");
                return;
            }

            service.deleteTransaction(id);
            display.showMessage("Transaction deleted successfully!");
        } catch (NumberFormatException e) {
            display.showError("Please enter a valid ID.");
            display.showMessage("Please try again or enter 'cancel' to abort.");
            deleteTransaction();
        } catch (IllegalArgumentException e) {
            display.showError(e.getMessage());
            display.showMessage("Please try again or enter 'cancel' to abort.");
            deleteTransaction();
        }
    }

    private void editTransaction() {
        List<Transaction> transactions = service.getAllTransactions();
        if (transactions.isEmpty()) {
            display.showMessage("No transactions available to edit.");
            return;
        }

        Transaction transaction = selectTransactionForEdit(transactions);
        if (transaction == null) return;

        Transaction updatedTransaction = collectUpdatedTransactionData(transaction);
        if (updatedTransaction == null) return;

        if (confirmUpdate(updatedTransaction)) {
            service.updateTransaction(updatedTransaction);
            display.showMessage("Transaction updated successfully!");
        }
    }

    private Transaction selectTransactionForEdit(List<Transaction> transactions) {
        display.showAvailableTransactions(transactions);
        String input = inputHandler.readStringInput("Enter transaction ID to edit or 'cancel' to abort: ");
        if (input.equalsIgnoreCase("cancel")) {
            display.showMessage("Edit cancelled.");
            return null;
        }

        try {
            long id = Long.parseLong(input);
            Transaction transaction = service.getTransactionById(id);
            if (transaction == null) {
                display.showError("Transaction not found.");
                display.showMessage("Please try again or enter 'cancel' to abort.");
                return selectTransactionForEdit(transactions);
            }

            String type = transaction.getType();
            if (type != null) {
                transaction.setType(type.toLowerCase());
            }

            display.showMessage("Current transaction: " + transaction);
            return transaction;
        } catch (NumberFormatException e) {
            display.showError("Please enter a valid ID.");
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return selectTransactionForEdit(transactions);
        } catch (IllegalArgumentException e) {
            display.showError(e.getMessage());
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return selectTransactionForEdit(transactions);
        }
    }

    private Transaction collectUpdatedTransactionData(Transaction transaction) {
        long id = transaction.getId();

        String typeInput = inputHandler.readStringInput("Enter new type (income/expense) or press Enter to keep current [" + transaction.getType() + "]: ");
        String type = typeInput.isEmpty() ? transaction.getType() : typeInput.toLowerCase();
        if (type.equals("cancel")) {
            display.showMessage("Edit cancelled.");
            return null;
        }
        if (!type.equals(transaction.getType()) && !type.equals("income") && !type.equals("expense")) {
            display.showError("Type must be 'income' or 'expense'.");
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return collectUpdatedTransactionData(transaction);
        }

        Double amount = inputHandler.readUpdatedAmount(transaction.getAmount());
        if (amount == null) return null;

        String category = inputHandler.readUpdatedCategory(transaction.getCategory());
        if (category == null) return null;

        LocalDateTime date = inputHandler.readDate(transaction.getDate());
        if (date == null) {
            display.showMessage("Edit cancelled.");
            return null;
        }

        return new Transaction(id, type, amount, category, date);
    }

    private boolean confirmUpdate(Transaction updatedTransaction) {
        display.showMessage("\nUpdated transaction: " + updatedTransaction);
        String confirmation = inputHandler.readConfirmation("Are you sure you want to save these changes? (yes/no): ");
        if (!confirmation.equals("yes")) {
            display.showMessage("Edit cancelled.");
            return false;
        }
        return true;
    }

    private void filterTransactions() {
        String type = inputHandler.readStringInput("Filter by type (income/expense, or press Enter to skip): ").toLowerCase();
        if (!type.isEmpty() && !type.equals("income") && !type.equals("expense")) {
            display.showError("Type must be 'income' or 'expense'.");
            return;
        }

        String category = inputHandler.readStringInput("Filter by category (ex. FOOD, or press Enter to skip): ").toUpperCase();

        LocalDateTime startDate = readFilterDate("Start date (yyyy-MM-dd HH:mm:ss, or press Enter to skip): ");
        LocalDateTime endDate = readFilterDate("End date (yyyy-MM-dd HH:mm:ss, or press Enter to skip): ");

        List<Transaction> filtered = service.getTransactionsByFilter(
                type.isEmpty() ? null : type,
                category.isEmpty() ? null : category,
                startDate,
                endDate
        );
        display.displayFilteredTransactions(filtered);
    }

    private LocalDateTime readFilterDate(String prompt) {
        String input = inputHandler.readStringInput(prompt);
        if (input.isEmpty()) return null;
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            display.showError("Invalid date format. Use yyyy-MM-dd HH:mm:ss.");
            return null;
        }
    }

    private void displayTransactions() {
        display.displayTransactions(service.getAllTransactions());
    }

}