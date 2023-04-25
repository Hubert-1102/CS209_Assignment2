package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.controller.ChatController;
import cn.edu.sustech.cs209.chatting.client.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(this::handleClose);
        stage.setTitle("Chatting Login");
        stage.show();
    }

    private void handleClose(WindowEvent event) {
        try {
            ChatController.shutdown();

        }catch (Exception e){
            System.out.println("error close");
        }
    }


}
