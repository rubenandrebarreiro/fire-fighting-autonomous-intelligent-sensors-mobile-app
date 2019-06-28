package com.intelligentsensors.firefighting.util;

public class User {

    private String username;
    private String password;
    private boolean admin;

    public User(String username, String password, boolean permission){
        this.username = username;
        this.password = password;
        this.admin = permission;
    }

    public String getUsername(){
        return username;
    }

    public boolean isPassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin(){
        return admin;
    }
}
