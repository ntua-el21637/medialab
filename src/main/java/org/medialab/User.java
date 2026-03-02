package org.medialab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String firstName;
    private String lastName;
    private UserRole role;
    private String username;
    private String password;

    private List<String> accessibleCategories;

    //map, for versioning
    private Map<String, Integer> watchedDocuments;

    public User(String firstName, String lastName, UserRole role, String username, String password, List<String> accessibleCategories) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.username = username;
        this.password = password;
        this.accessibleCategories = accessibleCategories;
        this.watchedDocuments = new HashMap<>();
    }

    //getters
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public UserRole getRole() {return role;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public List<String> getAccessibleCategories() {return accessibleCategories;}
    public Map<String, Integer> getWatchedDocuments() {return watchedDocuments;}

}

