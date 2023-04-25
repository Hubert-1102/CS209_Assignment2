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
        userHashMap.put(4, new User("jack", 4));
        userHashMap.put(5, new User("henry", 5));
        int count = 6;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stringBuilder.append(i).append("&");
            for (int i1 = i + 1; i1 <= 5; i1++) {
                stringBuilder.append(i1).append("&");
                count++;
                userHashMap.put(count, new User(stringBuilder.toString(), count));
                for (int i2 = i1 + 1; i2 <= 5; i2++) {
                    stringBuilder.append(i2).append("&");
                    count++;
                    userHashMap.put(count, new User(stringBuilder.toString(), count));
                    for (int i3 = i2 + 1; i3 <= 5; i3++) {
                        stringBuilder.append(i3).append("&");
                        count++;
                        userHashMap.put(count, new User(stringBuilder.toString(), count));
                        for (int i4 = i3 + 1; i4 <= 5; i4++) {
                            stringBuilder.append(i4).append("&");
                            count++;
                            userHashMap.put(count, new User(stringBuilder.toString(), count));
                            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                        }
                        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                    }
                    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                }
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
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
        ArrayList<User> result = new ArrayList<>();
        for (User value : userHashMap.values()) {
            if (value.getId() <= 5)
                result.add(value);
        }
        return result;
    }

    public static ArrayList<User> getAllUsers(){
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
        if (name.contains("&")) {
            StringBuilder stringBuilder = new StringBuilder();
            String[] list = name.split("&");
            for (String s : list) {
                if (!s.equals("")) {
                    stringBuilder.append(getUserById(Integer.parseInt(s)).getName()).append("&");
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            return stringBuilder.toString();
        }
        return name.toUpperCase();
    }
}
