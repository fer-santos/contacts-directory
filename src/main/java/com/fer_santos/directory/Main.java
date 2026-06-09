package com.fer_santos.directory;

import com.fer_santos.directory.models.Contact;
import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    // Scanner scanner = new Scanner(System.in);

    ArrayList<User> usersList = StorageManager.loadUsers();

    Javalin app = Javalin.create(config -> {
      config.requestLogger.http((ctx, ms) -> {
        System.out.println("Request: " + ctx.method() + " " + ctx.path() + " - " + ms + "ms");
      });
    }).start(7070);

    app.get("/", ctx -> ctx.result("Amber Minimal API is running!"));

    app.get("/api/contacts", ctx -> {
      try {
        List<Contact> allContacts = new ArrayList<>();
        for (User user : usersList) {
          allContacts.addAll(user.getContacts());
        }
        ctx.json(allContacts);
      } catch (Exception e) {
        ctx.status(500).result("Error retrieving contacts: " + e.getMessage());
      }
    });

    /*
    char opSelected;
    boolean isValid;

    do {
      do {
        System.out.println("\n1) Log In");
        System.out.println("2) Create Account");
        System.out.println("3) Exit Program");
        System.out.print("Option: ");
        opSelected = scanner.nextLine().charAt(0);
        isValid = opSelected == '1' || opSelected == '2' || opSelected == '3';
        if (!isValid) System.out.println("\n### SELECT A CORRECT OPTION ###\n");
      } while (!isValid);
      switch (opSelected) {
        case '1' -> {
          boolean isLogged = false;
          do {
            System.out.print("\n(Write \"exit\" to Exit the Menu)\nEnter Your E-mail: ");
            String email = scanner.nextLine();
            if (email.equalsIgnoreCase("exit")) break;
            System.out.print("Enter Your Password: ");
            String password = scanner.nextLine();
            User authenticatedUser = Main.authenticateUser(email, password, usersList);
            if (authenticatedUser != null) {
              System.out.println("\nWelcome " + authenticatedUser.getName() + " " + authenticatedUser.getLastName() + "!");
              showUserMenu(authenticatedUser, scanner);
              isLogged = true;
            } else System.out.println("\n### INCORRECT EMAIL OR PASSWORD ###\n");
          } while (!isLogged);
        }
        case '2' -> {
          String name;
          do {
            System.out.print("\nEnter Your Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) System.out.println("### ERROR: NAME IS MANDATORY ###");
          } while (name.isEmpty());

          System.out.print("Enter Your Last Name (Optional): ");
          String lastName = scanner.nextLine();

          String email;
          boolean isDuplicate;
          do {
            System.out.print("\n(Write \"exit\" to Cancel)\nEnter Your E-mail: ");
            email = scanner.nextLine().trim();
            if (email.equalsIgnoreCase("exit")) break;

            if (email.isEmpty()) {
              System.out.println("### ERROR: EMAIL IS MANDATORY ###");
              isDuplicate = true;
            } else if (!email.matches(".*@.*\\..*")) {
              System.out.println("### ERROR: INVALID EMAIL FORMAT (Must contain '@' and '.') ###");
              isDuplicate = true;
            } else {
              isDuplicate = isEmailRegistered(email, usersList);
              if (isDuplicate) {
                System.out.println("\n### ERROR: EMAIL ALREADY REGISTERED ###");
              }
            }
          } while (isDuplicate);

          if (email.equalsIgnoreCase("exit")) break;

          String password;
          do {
            System.out.print("Create a Password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) System.out.println("### ERROR: PASSWORD IS MANDATORY ###");
          } while (password.isEmpty());

          User user = new User(name, lastName, email, password);
          usersList.add(user);
          System.out.println("User Registered");
        }
      }
    } while (opSelected != '3');

    StorageManager.saveUsers(usersList);
    System.out.println("End Program :)");
    */
  }

  public static User authenticateUser (String email, String password, ArrayList<User> userList) {
    for (User currentUser : userList) {
      boolean isEmailCorrect = currentUser.getEmail().equals(email);
      boolean isPasswordCorrect = currentUser.getPassword().equals(password);

      if (isEmailCorrect && isPasswordCorrect) return currentUser;
    }
    return null;
  }

  public static boolean isEmailRegistered(String email, ArrayList<User> usersList) {
    for (User user : usersList) {
      if (user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    return false;
  }

  private static void showUserMenu(User user, Scanner scanner) {
    char option;
    boolean logout = false;

    do {
      System.out.println("\n--- User Menu ---");
      System.out.println("1) View my contacts");
      System.out.println("2) Add a contact");
      System.out.println("3) Delete a contact");
      System.out.println("4) Log out");
      System.out.print("Option: ");
      
      String input = scanner.nextLine();
      if (input.isEmpty()) continue;
      option = input.charAt(0);

      switch (option) {
        case '1' -> {
          System.out.println("\n--- My Contacts ---");
          user.printContacts();
        }
        case '2' -> {
          System.out.println("\n--- Add Contact ---");
          String name;
          do {
            System.out.print("Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
              System.out.println("### NAME IS MANDATORY ###");
            }
          } while (name.isEmpty());

          System.out.print("Last Name: ");
          String lastName = scanner.nextLine();
          
          String phoneNumber;
          boolean isPhoneValid;
          do {
            System.out.print("Phone Number: ");
            phoneNumber = scanner.nextLine().trim();
            isPhoneValid = phoneNumber.matches("^\\+?\\d+$");
            if (phoneNumber.isEmpty()) {
              System.out.println("### PHONE NUMBER IS MANDATORY ###");
            } else if (!isPhoneValid) {
              System.out.println("### ERROR: PHONE NUMBER MUST CONTAIN ONLY DIGITS (optional '+' at start) ###");
            }
          } while (phoneNumber.isEmpty() || !isPhoneValid);

          System.out.print("Email: ");
          String email = scanner.nextLine();
          System.out.print("Alias: ");
          String alias = scanner.nextLine();

          user.addContact(name, lastName, phoneNumber, email, alias);
          System.out.println("Contact added successfully!");
        }
        case '3' -> {
          System.out.println("\n--- Delete Contact ---");
          if (user.getContactCount() == 0) {
            System.out.println("--- Empty Contact Directory ---");
            continue;
          }
          user.printContacts();
          System.out.print("Enter index contact number to delete: ");
          try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (user.deleteContact(index)) {
              System.out.println("Contact deleted successfully!");
            } else {
              System.out.println("### INVALID CONTACT NUMBER ###");
            }
          } catch (NumberFormatException e) {
            System.out.println("### PLEASE ENTER A VALID NUMBER ###");
          }
        }
        case '4' -> logout = true;
        default -> System.out.println("### INVALID OPTION ###");
      }
    } while (!logout);
  }
}
