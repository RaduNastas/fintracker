package com.fintracker.controller;

import com.fintracker.service.TransactionService;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class FinanceController {
    private final TransactionService service = new TransactionService();
    private final Scanner scanner = new Scanner(System.in);
    private final List<String> predefinedCategories = Arrays.asList(
            "FOOD", "TRANSPORT", "ENTERTAINMENT", "BILLS", "SALARY", "OTHER"
    );

    public void start() {
        try {
            while (true) {
                System.out.println("\n--- Personal Financial Tracker ---");
                System.out.println("1. Add transaction");
                System.out.println("2. Show all transactions");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                int opt = readIntInput();
                scanner.nextLine();

                switch (opt) {
                    case 1 -> addTransaction();
                    case 2 -> displayTransactions();
                    case 3 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please enter a number between 1 and 3.");
                }
            }
        } finally {
            scanner.close();
        }
    }

    private void addTransaction() {
        String type = null;
        Double amount = null;
        String category = null;

        while (true) {
            System.out.print("Type (INCOME/EXPENSE) or 'cancel' to abort: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("CANCEL")) {
                System.out.println("Transaction cancelled.");
                return;
            }
            if (input.equals("INCOME") || input.equals("EXPENSE")) {
                type = input;
                break;
            }
            System.out.println("Error: Type must be 'INCOME' or 'EXPENSE'.");
        }

        while (true) {
            System.out.print("Sum or 'cancel' to abort: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("Transaction cancelled.");
                return;
            }
            try {
                amount = Double.parseDouble(input);
                if (amount < 0) {
                    System.out.println("Error: Amount cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }

        while (true) {
            System.out.println("Available categories: " + predefinedCategories);
            System.out.print("Category (choose from list or enter custom) or 'cancel' to abort: ");
            category = scanner.nextLine().trim().toUpperCase();
            if (category.equalsIgnoreCase("cancel")) {
                System.out.println("Transaction cancelled.");
                return;
            }
            if (category.isEmpty()) {
                System.out.println("Error: Category cannot be empty.");
                continue;
            }
            if (!predefinedCategories.contains(category)) {
                System.out.print("Category '" + category + "' is not in the predefined list. Use it anyway? (yes/no): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if (confirmation.equals("yes")) {
                    break;
                }
                System.out.println("Please choose a category from the list or enter a valid custom one.");
            } else {
                break;
            }
        }

        try {
            service.addTransaction(type, amount, category);
            System.out.println("Transaction added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void displayTransactions() {
        System.out.println("\n--- Transaction List ---");
        var transactions = service.getAllTransactions();
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            transactions.forEach(System.out::println);
        }
    }

    private int readIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Error: Please enter a valid number: ");
                scanner.nextLine();
            }
        }
    }

}