package com.fer_santos.directory.models;

import java.util.UUID;

public class User {
  private String id;
  private String name;
  private String lastName;
  private String email;
  private String password;

  public User() {
    this.id = UUID.randomUUID().toString();
  }

  public User(String name, String lastName, String email, String password) {
    if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is mandatory");
    if (email == null || !email.matches(".*@.*\\..*")) throw new IllegalArgumentException("Invalid email format");
    if (password == null || password.trim().isEmpty()) throw new IllegalArgumentException("Password is mandatory");
    
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}
