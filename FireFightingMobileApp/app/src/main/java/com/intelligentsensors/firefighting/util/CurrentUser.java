package com.intelligentsensors.firefighting.util;

import android.app.Application;

public class CurrentUser extends Application {

    private String email;
    private String userName;
    private boolean loggedIn;
    private boolean admin;

    public void setNewLoggedInUser(String email, String userName, boolean admin){
        this.email = email;
        this.userName = userName;
        this.admin = admin;
        loggedIn = true;
    }

    public void logout(){
        loggedIn = false;
    }

    public boolean isLoggedIn(){
        return loggedIn;
    }

    public String getCurrUserEmail(){
        return email;
    }

    public String getCurrUserUserName(){
        return userName;
    }

    public boolean isAdmin(){
        return admin;
    }
}
