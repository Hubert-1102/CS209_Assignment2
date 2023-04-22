package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class User implements Serializable {
    private final String name;
    private final int id;

    public static final HashMap<Integer, User> userHashMap;

    static {
        userHashMap = new HashMap<>();
        userHashMap.put(1, new User("hubert", 1));
        userHashMap.put(2, new User("mike", 2));
        userHashMap.put(3, new User("alice", 3));
    }

    public static User getUserById(int id) {
        return userHashMap.get(id);
    }

    public static User getUserByName(String name) {
        for (User user : userHashMap.values()) {
            if (user.getName().equals(name))
                return user;
        }
        return null;
    }

    public static ArrayList<User> getUsers() {
        return new ArrayList<>(userHashMap.values());
    }

    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
