package com.intelligentsensors.firefighting.util;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private Map<String, User> repository;

    public UserRepository(){
        repository = new HashMap<>();
        repository.put("Admin", new User("Admin", "123456", true));
    }

    public void addUser(String user, String password){
        if(!repository.containsKey(user))
            repository.put(user, new User(user,password, false));
        else System.err.println("User already exists");
    }

    public void deleteUser(String user) {
        repository.remove(user);
    }

    public boolean login(String user, String password){
        User auxUser = repository.get(user);
        if(auxUser != null)
            return auxUser.isPassword(password);
        else {
            System.err.println("No such user");
            return false;
        }
    }

}
