package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.User;
import java.sql.*;

public class ClientService {
    private static String Driver = "com.mysql.cj.jdbc.Driver";
    private static String Url = "jdbc:mysql://43.139.12.74:3306/java2";
    private static String User = "hubert";
    private static String Password = "h021102..";

    public static User login(String name, String password) throws SQLException {
        Connection con = DriverManager.getConnection(Url, User, Password);
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

}
