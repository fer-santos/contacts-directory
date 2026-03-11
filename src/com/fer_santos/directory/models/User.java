package com.fer_santos.directory.models;

import java.util.ArrayList;

public class User {
  private ArrayList<Contact> contacts = new ArrayList<>();

  private String name;
  private String lastName;
  private String email;
  private String password;

  public User(String password, String email, String lastName, String name) {
    this.password = password;
    this.email = email;
    this.lastName = lastName;
    this.name = name;
  }

  public void addContact() {

  }

  public void deleteContact() {

  }

  public void modifyContact() {

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
