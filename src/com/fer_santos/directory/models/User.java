package com.fer_santos.directory.models;

import java.util.ArrayList;

public class User {
  private ArrayList<Contact> contacts = new ArrayList<>();

  private String name;
  private String lastName;
  private String email;
  private String password;

  public User(String name, String lastName, String email, String password) {
    this.name = name;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }

  public void addContact(String name, String lastName, String phoneNumber, String email, String alias) {
    Contact contact = new Contact(name, lastName, phoneNumber, email, alias);
    contacts.add(contact);
  }

  public boolean deleteContact(String alias) {
    return contacts.removeIf(contact -> contact.getAlias().equalsIgnoreCase(alias));
  }

  public void modifyContact() {

  }

  public void printContacts() {
    if (contacts.isEmpty()) {
      System.out.println("--- Empty Contact Directory ---");
    } else {
      for (Contact currenContact : contacts) {
        System.out.println(currenContact);
      }
    }
  }

  public int getContactCount() {
    return contacts.size();
  }

  // ### Getters & Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
