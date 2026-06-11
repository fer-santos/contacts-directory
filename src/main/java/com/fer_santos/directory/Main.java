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

    ArrayList<User> loadedUsers = StorageManager.loadUsers();
    final ArrayList<User> usersList = (loadedUsers != null) ? loadedUsers : new ArrayList<>();

    if (usersList.isEmpty()) {
      User admin = new User("Admin", "System", "admin@amber.com", "admin123");
      usersList.add(admin);
      StorageManager.saveUsers(usersList);
      System.out.println("Seed user created: admin@amber.com");
    }

    Javalin app = Javalin.create(config -> {
      config.staticFiles.add("/public");
      config.requestLogger.http((ctx, ms) -> {
        System.out.println("Request: " + ctx.method() + " " + ctx.path() + " - " + ms + "ms");
      });
    }).start(7070);

    app.get("/api/contacts", ctx -> {
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }
      try {
        List<Contact> allContacts = new ArrayList<>();
        for (User user : currentUsers) {
          allContacts.addAll(user.getContacts());
        }
        ctx.json(allContacts);
      } catch (Exception e) {
        ctx.status(500).result("Error retrieving contacts: " + e.getMessage());
      }
    });

    app.post("/api/contacts", ctx -> {
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      String ownerEmail = ctx.queryParam("ownerEmail");
      if (ownerEmail == null || ownerEmail.isBlank()) {
        ctx.status(400).result("Query parameter 'ownerEmail' is mandatory.");
        return;
      }

      try {
        Contact newContact = ctx.bodyAsClass(Contact.class);

        if (newContact.getName() == null || newContact.getName().isBlank() ||
            newContact.getPhoneNumber() == null || newContact.getPhoneNumber().isBlank()) {
          ctx.status(400).result("Fields 'name' and 'phoneNumber' cannot be null or empty.");
          return;
        }

        User owner = null;
        for (User user : currentUsers) {
          if (user.getEmail().equalsIgnoreCase(ownerEmail)) {
            owner = user;
            break;
          }
        }

        if (owner == null) {
          ctx.status(404).result("User with email " + ownerEmail + " not found.");
          return;
        }

        owner.getContacts().add(newContact);
        
        try {
          StorageManager.saveUsers(currentUsers);
          ctx.status(201).json(newContact);
        } catch (Exception e) {
          ctx.status(500).result("Error during data persistence.");
        }

      } catch (Exception e) {
        ctx.status(400).result("Invalid JSON body or format.");
      }
    });

    app.put("/api/contacts", ctx -> {
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      String email = ctx.queryParam("ownerEmail");
      String indexStr = ctx.queryParam("contactIndex");

      if (email == null || email.isBlank() || indexStr == null || indexStr.isBlank()) {
        ctx.status(400).result("Parameters 'ownerEmail' and 'contactIndex' are mandatory.");
        return;
      }

      try {
        int index = Integer.parseInt(indexStr);
        User user = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);

        if (user == null) {
          ctx.status(404).result("User not found.");
          return;
        }

        if (index < 0 || index >= user.getContacts().size()) {
          ctx.status(400).result("Invalid contact index.");
          return;
        }

        Contact updatedContact = ctx.bodyAsClass(Contact.class);
        if (updatedContact.getName() == null || updatedContact.getName().isBlank() ||
            updatedContact.getPhoneNumber() == null || updatedContact.getPhoneNumber().isBlank()) {
          ctx.status(400).result("Fields 'name' and 'phoneNumber' are mandatory.");
          return;
        }

        user.getContacts().set(index, updatedContact);
        StorageManager.saveUsers(currentUsers);
        ctx.status(200).json(updatedContact);

      } catch (NumberFormatException e) {
        ctx.status(400).result("Parameter 'contactIndex' must be a number.");
      } catch (Exception e) {
        ctx.status(500).result("Persistence error.");
      }
    });

    app.delete("/api/contacts", ctx -> {
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      String email = ctx.queryParam("ownerEmail");
      String indexStr = ctx.queryParam("contactIndex");

      if (email == null || email.isBlank() || indexStr == null || indexStr.isBlank()) {
        ctx.status(400).result("Parameters 'ownerEmail' and 'contactIndex' are mandatory.");
        return;
      }

      try {
        int index = Integer.parseInt(indexStr);
        User user = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);

        if (user == null) {
          ctx.status(404).result("User not found.");
          return;
        }

        if (index < 0 || index >= user.getContacts().size()) {
          ctx.status(400).result("Invalid contact index.");
          return;
        }

        user.getContacts().remove(index);
        StorageManager.saveUsers(currentUsers);
        ctx.status(200).result("Contact deleted successfully.");

      } catch (NumberFormatException e) {
        ctx.status(400).result("Parameter 'contactIndex' must be a number.");
      } catch (Exception e) {
        ctx.status(500).result("Persistence error.");
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
            isPhoneValid = phoneNumber.matches("^[0-9\\+\\-\\s\\(\\)]+$");
            if (phoneNumber.isEmpty()) {
              System.out.println("### PHONE NUMBER IS MANDATORY ###");
            } else if (!isPhoneValid) {
              System.out.println("### ERROR: PHONE NUMBER MUST CONTAIN ONLY DIGITS AND + - ( ) ###");
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
