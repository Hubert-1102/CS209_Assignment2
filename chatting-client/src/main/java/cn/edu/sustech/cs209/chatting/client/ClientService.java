package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.User;

import java.sql.*;
import java.util.ArrayList;

public class ClientService {
    private static String Driver = "com.mysql.cj.jdbc.Driver";
    private static String Url = "jdbc:mysql://43.139.12.74:3306/java2";
    private static String UserName = "hubert";
    private static String Password = "h021102..";

    public static User login(String name, String password) throws SQLException {
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        String sql = "select * from user where name=?";
        PreparedStatement pstate = con.prepareStatement(sql);
        pstate.setString(1, name);

        ResultSet resultSet = pstate.executeQuery();
        while (resultSet.next()) {
            String pwd = resultSet.getString("password");
            System.out.print(pwd + " ");
            System.out.println();
            if (pwd.equals(password)) {
                int id = resultSet.getInt("id");
                String userName = resultSet.getString("name");
                con.close();
                return new User(userName, id);
            }
        }
        con.close();
        return null;
    }

    public static ArrayList<User> searchOnline() throws SQLException {
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        String sql = "select id from user where isOnline = 1";
        PreparedStatement pstate = con.prepareStatement(sql);
        ResultSet resultSet = pstate.executeQuery();
        ArrayList<User> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(User.getUserById(resultSet.getInt("id")));
        }
        return result;
    }


    public static int updateOnline(int id, int state) throws SQLException {
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        String sql = "update user set isOnline = ? where id = ?";
        PreparedStatement pstate = con.prepareStatement(sql);
        pstate.setInt(1, state);
        pstate.setInt(2, id);
        return pstate.executeUpdate();
    }

}
