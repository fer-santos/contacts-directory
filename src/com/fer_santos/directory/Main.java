package com.fer_santos.directory;

import com.fer_santos.directory.models.User;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    ArrayList<User> usersList = new ArrayList<>();
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
            if (!(authenticatedUser == null)) {
              System.out.println(authenticatedUser.getName());
              System.out.println(authenticatedUser.getLastName());
              isLogged = true;
            } else System.out.println("\n### INCORRECT EMAIL OR PASSWORD ###\n");
          } while (!isLogged);
        }
        case '2' -> {
          System.out.print("\nEnter Your Name: ");
          String name = scanner.nextLine();
          System.out.print("Enter Your Last Name: ");
          String lastName = scanner.nextLine();
          System.out.print("Enter Your E-mail: ");
          String email = scanner.nextLine();
          System.out.print("Create a Password: ");
          String password = scanner.nextLine();
          User user = new User(name, lastName, email, password);
          usersList.add(user);
          System.out.println("User Registered");
        }
      }
    } while (opSelected != '3');

    System.out.println("End Program :)");
  }

  public static User authenticateUser (String email, String password, ArrayList<User> userList) {
    for (User currentUser : userList) {
      boolean isEmailCorrect = currentUser.getEmail().equals(email);
      boolean isPasswordCorrect = currentUser.getPassword().equals(password);

      if (isEmailCorrect && isPasswordCorrect) return currentUser;
    }
    return null;
  }
}
