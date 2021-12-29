package it.unipi.dii.lsmd.paperraterapp.model;

import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String picture;
    private int age;
    private List<ReadingList> readingLists;
    private int type;

    public User(String username, String email, String password, String firstName, String lastName,
                String picture, int age, List<ReadingList> readingLists, int type) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.picture = picture;
        this.age = age;
        this.readingLists = readingLists;
        this.type = type;
    }

    public User(String username, String email) {
        this(username, email, null, null, null, null, -1, null, -1);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPicture() {
        return picture;
    }

    public int getAge() {
        return age;
    }

    public List<ReadingList> getReadingLists() {
        return readingLists;
    }

    public int getType() { return this.type; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setType(int numFollower) { this.type = type; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", picture='" + picture + '\'' +
                ", age=" + age +
                ", readingLists=" + readingLists +
                '}';
    }
}
