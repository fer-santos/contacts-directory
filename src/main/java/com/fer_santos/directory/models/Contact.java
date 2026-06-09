package com.fer_santos.directory.models;

import java.io.Serializable;

public class Contact implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private String lastName;
  private String phoneNumber;
  private String email;
  private String alias;

  public Contact() {
  }

  public Contact(String name, String lastName, String phoneNumber, String email, String alias) {
    this.name = name;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.alias = alias;
  }

  public String getAlias() {
    return alias;
  }

  // ### Getters & Setters
  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Contact{");
    sb.append("name='").append(name).append('\'');
    sb.append(", lastName='").append(lastName).append('\'');
    sb.append(", phoneNumber='").append(phoneNumber).append('\'');
    sb.append(", email='").append(email).append('\'');
    sb.append(", alias='").append(alias).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
