package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChattingClient {
    String host;
    int port;

    public ChattingClient(String ip, int port) {
        this.host = ip;
        this.port = port;
    }

    private User loginUser() throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input your userName:");
        String name = sc.next();
        System.out.println("Please input your password:");
        String pwd = sc.next();
        return ClientService.login(name, pwd);
    }

    private void run() throws IOException, SQLException {
        User user = loginUser();
        if (user == null) {
            System.out.println("wrong userName or password");
            return;
        } else
            System.out.println("login successfully!");
        int id = user.getId();
        Scanner sc = new Scanner(System.in);
        new Thread(() -> {
            int currentChatId = 0;
            long currentChatTime = 0;
            while (true) {
                try {
                    Socket socket = new Socket();
                    String content = "Need data";
                    socket.connect(new InetSocketAddress(host, port));
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(new Message(currentChatTime, User.getUserById(id), new User("", -1), content, currentChatId));
                    outputStream.flush();
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    Message message;
                    while (!(message = ((Message) inputStream.readObject())).getData().equals("no data")) {
                        currentChatId = message.getId();
                        currentChatTime = message.getTimestamp();
                        System.out.println(message.getData() + " send from " + message.getSentBy());
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (Exception e) {
                    System.out.println("Some error");
                }
            }
        }).start();
        while (true) {
            System.out.println("Your message:");
            String input = sc.next();
            System.out.println("Id of receiver:");
            int receiver = sc.nextInt();
            this.sendMessage(input, id, receiver);
        }
    }

    public void sendMessage(String message, int sendBy, int sendTo) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(this.host, this.port));
        InputStream inputStream = socket.getInputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new Message(System.currentTimeMillis(), User.getUserById(sendBy), User.getUserById(sendTo), message));
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    long currentChatTime = 0;
    int currentChatId = 0;

    public ArrayList<Message> getHistoryMessage(int id1, int id2) {
        /*
        -1 表示获取历史记录
         */
        ArrayList<Message> list = new ArrayList<>();
        try {
            Socket socket = new Socket();
            String content = id1 + "&" + id2;
            socket.connect(new InetSocketAddress(host, port));
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Message(currentChatTime, new User("", -1), new User("", -1), content, currentChatId));
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Message message;
            while (!(message = ((Message) inputStream.readObject())).getData().equals("no data")) {
                currentChatId = message.getId();
                currentChatTime = message.getTimestamp();
                list.add(message);
                System.out.println(message.getData() + " send from " + message.getSentBy());
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.println("Some error");
        }
        return list;
    }

    public ArrayList<Message> getRealTimeMessage(int id1,int id2) {
        /*
        -2 表示获取实时记录
         */
        ArrayList<Message> list = new ArrayList<>();
        try {
            Socket socket = new Socket();
            String content = id1 +"&"+ id2;
            socket.connect(new InetSocketAddress(host, port));
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Message(currentChatTime, new User("", -2), new User("", -2), content, currentChatId));
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Message message;
            while (!(message = ((Message) inputStream.readObject())).getData().equals("no data")) {
                currentChatId = message.getId();
                currentChatTime = message.getTimestamp();
                list.add(message);
                System.out.println(message.getData() + " send from " + message.getSentBy());
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.println("Some error");
        }
        return list;
    }

    public static void main(String[] args) throws IOException, SQLException {
        ChattingClient chattingClient = new ChattingClient("127.0.0.1", 9999);
        chattingClient.run();
    }
}
