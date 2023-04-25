package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ServerService {
    private static String Driver = "com.mysql.cj.jdbc.Driver";
    private static String Url = "jdbc:mysql://43.139.12.74:3306/java2";
    private static String UserName = "hubert";
    private static String Password = "h021102..";

    public static boolean storeChat(String msg, User sendBy, User sendTo, long time) throws SQLException {
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        System.out.println("connection get");
        String sql = "insert into chat (data,sendBy,sendTo,date,sendByName,sendToName) values (?,?,?,?,?,?)";
        PreparedStatement pstate = con.prepareStatement(sql);
        pstate.setString(1, msg);
        pstate.setInt(2, sendBy.getId());
        pstate.setInt(3, sendTo.getId());
        pstate.setTimestamp(4, new Timestamp(time));
        pstate.setString(5, sendBy.getName());
        pstate.setString(6, sendTo.getName());
        int re = pstate.executeUpdate();
        con.close();
        return re > 0;
    }

    public static ArrayList<Message> searchRealTimeChat(int idSend, int idTo, int chatId, long time) throws SQLException {
        if (idSend > 5)
            return searchRealTimeChatGroup(idSend, time);
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        String sql = "select * from chat where sendTo = ? and sendBy = ?";
        PreparedStatement pstate = con.prepareStatement(sql);
        pstate.setInt(1, idTo);
        pstate.setInt(2, idSend);
        ResultSet resultSet = pstate.executeQuery();

        ArrayList<Message> arrayList = new ArrayList<>();
        while (resultSet.next()) {
            if (resultSet.getTimestamp("date").getTime() > time)
                arrayList.add(new Message(resultSet.getTimestamp("date").getTime(),
                        User.getUserById(resultSet.getInt("sendBy")),
                        User.getUserById(resultSet.getInt("sendTo")), resultSet.getString("data"),
                        resultSet.getInt("id")));
        }
        con.close();
        return arrayList;
    }

    public static ArrayList<Message> searchRealTimeChatGroup(int idTo, long time) throws SQLException {
        Connection con = DriverManager.getConnection(Url, UserName, Password);
        String sql = "select * from chat where sendTo = ?";
        PreparedStatement pstate = con.prepareStatement(sql);
        pstate.setInt(1, idTo);
        ResultSet resultSet = pstate.executeQuery();

        ArrayList<Message> arrayList = new ArrayList<>();
        while (resultSet.next()) {
            if (resultSet.getTimestamp("date").getTime() > time)
                arrayList.add(new Message(resultSet.getTimestamp("date").getTime(),
                        User.getUserById(resultSet.getInt("sendBy")),
                        User.getUserById(resultSet.getInt("sendTo")), resultSet.getString("data"),
                        resultSet.getInt("id")));
        }
        con.close();
        return arrayList;
    }


    public static ArrayList<Message> searchHistoricalChat(int id1, int id2) throws SQLException {
        if (id2 > 5) {
            ArrayList<Message> arrayList1 = searchRealTimeChatGroup(id2, 0);
            arrayList1.sort((o1, o2) -> {
                long result = o1.getTimestamp() - o2.getTimestamp();
                if (result == 0)
                    return 0;
                return result < 0 ? -1 : 1;
            });
            return arrayList1;
        }
        ArrayList<Message> arrayList1 = searchRealTimeChat(id1, id2, 0, 0);
        ArrayList<Message> arrayList2 = searchRealTimeChat(id2, id1, 0, 0);
        arrayList2.addAll(arrayList1);
        arrayList2.sort((o1, o2) -> {
            long result = o1.getTimestamp() - o2.getTimestamp();
            if (result == 0)
                return 0;
            return result < 0 ? -1 : 1;
        });
        return arrayList2;
    }



}
