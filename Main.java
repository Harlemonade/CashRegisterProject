import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    static Scanner input = new Scanner(System.in);
    static ArrayList<String> adminUsername = new ArrayList<>();
    static ArrayList<String> adminPassword = new ArrayList<>();
    static boolean isLoggedIn = false;

    public static void showMenu(ArrayList<String> productName, ArrayList<Double> productPrice) {
        System.out.println("\n------------ Diwata Pares Menu ------------");
        for (int i = 0; i < productName.size(); i++) {
            System.out.println((i + 1) + ". " + productName.get(i) + " - ₱" + productPrice.get(i));
        }
        System.out.println("-------------------------------------------\n");
    }

    public static void orderItem(ArrayList<String> productName, ArrayList<Integer> productQuantity) {
        int quantity;
        String choice;

        do {
            quantity = 0;
            boolean isInteger = false;

            System.out.print("Enter the name of the Item that you want to order: ");
            String order = input.nextLine();

            if (productName.contains(order)) {
                while (!isInteger) {
                    System.out.print("Enter quantity: ");
                    if (input.hasNextInt()) {
                        quantity = input.nextInt();
                        input.nextLine();

                        if (quantity > 0) {
                            isInteger = true;
                            int index = productName.indexOf(order);
                            productQuantity.set(index, productQuantity.get(index) + quantity);
                            System.out.println(quantity + " " + order + " added to your order.");
                        } else {
                            System.out.println("Invalid input. The number must be positive.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a number.");
                        input.next();
                    }
                }
            } else {
                System.out.println("Item not found in the menu.");
            }

            while (true) {
                System.out.print("Do you want to add another item? (y/n): ");
                choice = input.nextLine();
                if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) {
                    break;
                } else {
                    System.out.println("Invalid input. Please enter 'y' or 'n'.");
                }
            }

        } while (choice.equalsIgnoreCase("y"));
    }

    public static void removeOrder(ArrayList<String> productName, ArrayList<Integer> productQuantity) {
        System.out.print("Enter the name of the item that you want to remove from your order: ");
        String removeOrder = input.nextLine();

        if (productName.contains(removeOrder)) {
            try {
                System.out.print("Enter the quantity: ");
                int removeQuantity = input.nextInt();
                input.nextLine();

                int index = productName.indexOf(removeOrder);
                int currentQuantity = productQuantity.get(index);
                if (removeQuantity <= currentQuantity && removeQuantity > 0) {
                    productQuantity.set(index, currentQuantity - removeQuantity);
                    System.out.println(removeQuantity + " " + removeOrder + " was removed from your order.");
                } else {
                    System.out.println("Invalid quantity to remove.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a valid number.");
                input.nextLine();
            }
        } else {
            System.out.println("Item not found in your order.");
        }
    }

    public static void orderSummary(ArrayList<String> productName, ArrayList<Integer> productQuantity) {
        String choice;
        boolean orderExists = false;

        System.out.println("\nYour Orders: ");
        for (int i = 0; i < productName.size(); i++) {
            if (productQuantity.get(i) > 0) {
                System.out.println(productName.get(i) + " x " + productQuantity.get(i));
                orderExists = true;
            }
        }

        if (!orderExists) {
            System.out.println("There is no order.");
            return;
        }

        boolean validInput = false;
        do {
            System.out.print("\nDo you want to remove an order? (y/n): ");
            try {
                choice = input.nextLine();
                if (choice.equalsIgnoreCase("y")) {
                    removeOrder(productName, productQuantity);
                    validInput = true;
                } else if (choice.equalsIgnoreCase("n")) {
                    break;
                } else {
                    throw new IllegalArgumentException("Invalid input. Please enter y/n only!");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } while (!validInput);
    }

    public static void checkOut(ArrayList<String> productName, ArrayList<Integer> productQuantity, ArrayList<Double> productPrice) {
        boolean orderExists = false;
        double total = 0;

        System.out.println("------------------------------------------");
        System.out.println("              Diwata Pares");
        System.out.println("------------------------------------------");
        System.out.println("Product Name      Quantity    Price");
        System.out.println("------------------------------------------");

        for (int i = 0; i < productName.size(); i++) {
            if (productQuantity.get(i) > 0) {
                double itemTotal = productQuantity.get(i) * productPrice.get(i);
                total += itemTotal;
                System.out.printf("%-17s %5d %11.2f\n", productName.get(i), productQuantity.get(i), productPrice.get(i));
                orderExists = true;
            }
        }

        if (!orderExists) {
            System.out.println("There is no order.");
            return;
        }

        System.out.println("------------------------------------------");
        System.out.printf("Total: %28.2f \n", total);

        System.out.print("\nProceed to Payment? y/n: ");
        char paymentChoice = input.next().charAt(0);
        input.nextLine();

        switch (paymentChoice) {
            case 'y':
                boolean paymentStatus = false;
                do {
                    try {
                        System.out.print("Enter payment: ");
                        double payment = input.nextDouble();
                        input.nextLine();
                        double userPayment = payment - total;
                        if (payment >= total) {
                            System.out.println("Payment Successful!");
                            System.out.printf("Your change is %.2f\n", userPayment);

                            String cashierUsername = adminUsername.get(0);
                            logTransaction(cashierUsername, productName, productQuantity, productPrice, total);

                            for (int i = 0; i < productQuantity.size(); i++) {
                                productQuantity.set(i, 0);
                            }

                            paymentStatus = true;
                        } else {
                            System.out.println("Insufficient Payment! Please re-enter payment.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input! Please enter a number.");
                        input.nextLine();
                    }
                } while (!paymentStatus);
                break;
            case 'n':
                System.out.println("Order Cancelled!");
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    public static void signUp() {
        boolean newUsernameStatus = false;
        boolean newPasswordStatus = false;

        String usernameRegex = "[a-zA-Z0-9_]{5,15}$";
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$";

        Pattern usernamePattern = Pattern.compile(usernameRegex);
        Pattern passwordPattern = Pattern.compile(passwordRegex);

        while (!newUsernameStatus) {
            System.out.print("Please Enter a Valid Username: ");
            String newUsername = input.nextLine();
            Matcher usernameMatcher = usernamePattern.matcher(newUsername);
            if (usernameMatcher.matches()) {
                System.out.println("Valid Username");
                adminUsername.add(newUsername);
                newUsernameStatus = true;
            } else {
                System.out.println("Invalid Username. Must be 5-15 characters, letters, numbers, or underscore only.");
            }
        }

        while (!newPasswordStatus) {
            System.out.print("Please Enter a Valid Password: ");
            String newPassword = input.nextLine();
            Matcher passwordMatcher = passwordPattern.matcher(newPassword);
            if (passwordMatcher.matches()) {
                System.out.println("Valid Password\n");
                adminPassword.add(newPassword);
                newPasswordStatus = true;
            } else {
                System.out.println("Invalid Password. Must be at least 8 characters, with letters and numbers.");
            }
        }

        System.out.println("Account Successfully Created!\n");
    }

    public static void logIn() {
        int maxAttempts = 3;
        int attempts = 0;
        boolean userVerified = false;

        if (adminUsername.isEmpty() && adminPassword.isEmpty()) {
            System.out.println("Account Doesn't Exist");
        } else {
            while (attempts < maxAttempts) {
                System.out.print("Enter Username: ");
                String usernameChecker = input.nextLine();
                System.out.print("Enter Password: ");
                String passwordChecker = input.nextLine();

                if (usernameChecker.isEmpty() || passwordChecker.isEmpty()) {
                    System.out.println("Username or Password cannot be empty.");
                    continue;
                }

                for (int i = 0; i < adminUsername.size(); i++) {
                    if (usernameChecker.equals(adminUsername.get(i)) && passwordChecker.equals(adminPassword.get(i))) {
                        userVerified = true;
                        break;
                    }
                }

                if (userVerified) {
                    System.out.println("\nAccount Verified!");
                    isLoggedIn = true;
                    break;
                } else {
                    attempts++;
                    if (attempts < maxAttempts) {
                        System.out.println("Incorrect Username or Password. You have " + (maxAttempts - attempts) + " attempts remaining.");
                    } else {
                        System.out.println("Too many failed attempts. Please try again later.");
                    }
                }
            }
        }
    }

    public static void logTransaction(String username, ArrayList<String> productName, ArrayList<Integer> productQuantity, ArrayList<Double> productPrice, double totalAmount) {
        String fileName = "transactions.txt";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("Date & Time       : ").append(dtf.format(now)).append("\n");
        sb.append("Cashier Username  : ").append(username).append("\n\n");
        sb.append("Items Purchased:\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("| %-15s | %-8s | %-10s | %-10s |\n", "Item Name", "Quantity", "Price", "Subtotal"));
        sb.append("------------------------------------------------------------\n");

        for (int i = 0; i < productName.size(); i++) {
            if (productQuantity.get(i) > 0) {
                double subtotal = productQuantity.get(i) * productPrice.get(i);
                sb.append(String.format("| %-15s | %-8d | ₱%-9.2f | ₱%-9.2f |\n",
                        productName.get(i), productQuantity.get(i), productPrice.get(i), subtotal));
            }
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("Total Amount       : ₱%.2f\n", totalAmount));
        sb.append("============================================================\n\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(sb.toString());
        } catch (Exception e) {
            System.out.println("Error writing transaction to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        ArrayList<String> productName = new ArrayList<>();
        ArrayList<Integer> productQuantity = new ArrayList<>();
        ArrayList<Double> productPrice = new ArrayList<>();

        productName.add("Pares");
        productPrice.add(70.00);
        productQuantity.add(0);

        productName.add("Pares Overload");
        productPrice.add(120.00);
        productQuantity.add(0);

        productName.add("Pares Mami");
        productPrice.add(85.00);
        productQuantity.add(0);

        productName.add("Crispy Pata");
        productPrice.add(1000.00);
        productQuantity.add(0);

        int onboardingChoice = 0;

        do {
            try {
                if (isLoggedIn) break;

                System.out.println("====================================");
                System.out.println("      Welcome to Diwata Pares! ");
                System.out.println("====================================");
                System.out.println("Please select an option:\n");
                System.out.println("1. Sign Up");
                System.out.println("2. Log In");
                System.out.println("3. Exit\n");
                System.out.print("Enter your choice: ");
                onboardingChoice = input.nextInt();
                input.nextLine();

                switch (onboardingChoice) {
                    case 1:
                        signUp();
                        break;
                    case 2:
                        logIn();
                        break;
                    case 3:
                        System.out.println("Thank you and have a good day!");
                        break;
                    default:
                        System.out.println("Invalid. Please Try Again");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a valid number.\n");
                input.nextLine();
            }
        } while (onboardingChoice != 3);

        if (isLoggedIn) {
            int choice = 0;
            do {
                try {
                    System.out.println("\n~~~~~  WELCOME TO DIWATA PARES  ~~~~~\n");
                    System.out.println("MENU OPTIONS:");
                    System.out.println("1. View Full Menu");
                    System.out.println("2. Order Food");
                    System.out.println("3. Review Order");
                    System.out.println("4. Proceed to Checkout");
                    System.out.println("5. Exit\n");
                    System.out.print("Enter your choice: ");
                    choice = input.nextInt();
                    input.nextLine();

                    switch (choice) {
                        case 1:
                            showMenu(productName, productPrice);
                            break;
                        case 2:
                            orderItem(productName, productQuantity);
                            break;
                        case 3:
                            orderSummary(productName, productQuantity);
                            break;
                        case 4:
                            checkOut(productName, productQuantity, productPrice);
                            break;
                        case 5:
                            System.out.println("Thank you and have a good day!");
                            break;
                        default:
                            System.out.println("Invalid choice! Please try again.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input! Please enter a valid number.");
                    input.nextLine();
                }
            } while (choice != 5);
        }
    }
}
    