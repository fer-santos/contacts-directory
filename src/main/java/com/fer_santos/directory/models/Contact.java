package com.fer_santos.directory.models;

import java.util.UUID;

public class Contact {
  private String id;
  private String name;
  private String lastName;
  private String phoneNumber;
  private String email;
  private String alias;
  private boolean favorite;
  private boolean trashed;

  public Contact() {
    this.id = UUID.randomUUID().toString();
  }

  public Contact(String name, String lastName, String phoneNumber, String email, String alias) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.alias = alias;
    this.favorite = false;
    this.trashed = false;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public boolean isFavorite() { return favorite; }
  public void setFavorite(boolean favorite) { this.favorite = favorite; }
  public boolean isTrashed() { return trashed; }
  public void setTrashed(boolean trashed) { this.trashed = trashed; }
  public String getAlias() { return alias; }
  public void setAlias(String alias) { this.alias = alias; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  @Override
  public String toString() {
    return "Contact{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", lastName='" + lastName + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", email='" + email + '\'' +
            ", alias='" + alias + '\'' +
            ", favorite=" + favorite +
            ", trashed=" + trashed +
            '}';
  }
}
