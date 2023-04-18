package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.server.ServerService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChattingServer {
    String host;
    int port;

    public void runServer() throws IOException {
        // 创建服务端ServerSocket
        ServerSocket server = new ServerSocket();
        // 绑定在某一个端口上
        server.bind(new InetSocketAddress(this.port));// 只有端口，默认绑定本机的IP -- localhost
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        while (true) {

            Socket socket = server.accept();
            Runnable runnable = () -> {
                ObjectInputStream inputStream = null;
                try {
//                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    Message message = (Message) inputStream.readObject();
                    if (message.getSendTo() == -1) {
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        for (Message msg : ServerService.searchRealTimeChat(message.getSentBy(), message.getId(), message.getTimestamp())) {
                            outputStream.writeObject(msg);
                            outputStream.flush();
                        }
                        outputStream.writeObject(new Message(0L, 0, 0, "no data"));
                        outputStream.flush();
                        outputStream.close();
                    } else {
                        System.out.println(message.getSendTo());
                        System.out.println(message.getData());
                        boolean b = ServerService.storeChat(message.getData(), message.getSentBy(), message.getSendTo(), message.getTimestamp());
                        System.out.println(b);
                    }
                    inputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException | SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            executorService.submit(runnable);
        }

    }

    public ChattingServer(String ip, int port) {
        this.host = ip;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        ChattingServer server = new ChattingServer("0000", 9999);
        server.runServer();
    }

}
