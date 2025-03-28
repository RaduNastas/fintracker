package com.fintracker.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class UserInputHandler {
    private final Scanner scanner;
    private final List<String> predefinedCategories = Arrays.asList("FOOD", "TRANSPORT", "ENTERTAINMENT", "BILLS", "SALARY", "OTHER");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ConsoleDisplay display;

    public UserInputHandler(Scanner scanner, ConsoleDisplay display) {
        this.scanner = scanner;
        this.display = display;
    }

    public String readType() {
        while (true) {
            System.out.print("Type (income/expense) or 'cancel' to abort: ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("cancel")) return null;
            if (input.equals("income") || input.equals("expense")) return input;
            System.out.println("Error: Type must be 'income' or 'expense'.");
        }
    }

    public Double readAmount() {
        while (true) {
            System.out.print("Sum or 'cancel' to abort: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) return null;
            try {
                double amount = Double.parseDouble(input);
                if (amount < 0) {
                    System.out.println("Error: Amount cannot be negative.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
    }

    public String readCategory() {
        while (true) {
            System.out.println("Available categories: " + predefinedCategories);
            System.out.print("Category (choose from list or enter custom) or 'cancel' to abort: ");
            String category = scanner.nextLine().trim().toUpperCase();
            if (category.equalsIgnoreCase("cancel")) return null;
            if (category.isEmpty()) {
                System.out.println("Error: Category cannot be empty.");
                continue;
            }
            if (!predefinedCategories.contains(category)) {
                System.out.print("Category '" + category + "' is not in the predefined list. Use it anyway? (yes/no): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if (confirmation.equals("yes")) return category;
            } else {
                return category;
            }
        }
    }

    public String readUpdatedCategory(String currentCategory) {
        String categoryInput = readStringInput("Enter new category or press Enter to keep current [" + currentCategory + "]: ");
        if (categoryInput.isEmpty()) return currentCategory;
        if (categoryInput.equalsIgnoreCase("cancel")) {
            display.showMessage("Edit cancelled.");
            return null;
        }
        categoryInput = categoryInput.toUpperCase();
        if (categoryInput.isEmpty()) {
            display.showError("Category cannot be empty.");
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return readUpdatedCategory(currentCategory);
        }
        if (!predefinedCategories.contains(categoryInput)) {
            String confirmation = readConfirmation("Category '" + categoryInput + "' is not in the predefined list. Use it anyway? (yes/no): ");
            if (confirmation.equals("yes")) return categoryInput;
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return readUpdatedCategory(currentCategory);
        }
        return categoryInput;
    }

    public LocalDateTime readDate(LocalDateTime currentDate) {
        while (true) {
            System.out.print("Enter new date (yyyy-MM-dd HH:mm:ss) or press Enter to keep current [" + currentDate.format(dateFormatter) + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return currentDate;
            if (input.equalsIgnoreCase("cancel")) return null;
            try {
                return LocalDateTime.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format. Use yyyy-MM-dd HH:mm:ss.");
            }
        }
    }

    public int readIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Error: Please enter a valid number: ");
                scanner.nextLine();
            }
        }
    }

    public String readStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readConfirmation(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim().toLowerCase();
    }

    public Double readUpdatedAmount(double currentAmount) {
        String amountInput = readStringInput("Enter new amount or press Enter to keep current [" + currentAmount + "]: ");
        if (amountInput.isEmpty()) return currentAmount;
        if (amountInput.equalsIgnoreCase("cancel")) {
            display.showMessage("Edit cancelled.");
            return null;
        }
        try {
            double amount = Double.parseDouble(amountInput);
            if (amount < 0) {
                display.showError("Amount cannot be negative.");
                display.showMessage("Please try again or enter 'cancel' to abort.");
                return readUpdatedAmount(currentAmount);
            }
            return amount;
        } catch (NumberFormatException e) {
            display.showError("Please enter a valid number.");
            display.showMessage("Please try again or enter 'cancel' to abort.");
            return readUpdatedAmount(currentAmount);
        }
    }

}