package cn.edu.sustech.cs209.chatting.client.controller;

import cn.edu.sustech.cs209.chatting.common.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import cn.edu.sustech.cs209.chatting.client.ClientService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() throws IOException, SQLException {
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        User user = ClientService.login(username, password);
        if (user != null) {
            int id = user.getId();

            Parent chatRoot = FXMLLoader.load((getClass().getResource("../chat.fxml")));
            Scene chatScene = new Scene(chatRoot);
            Stage primaryStage = (Stage) usernameTextField.getScene().getWindow();
            primaryStage.setScene(chatScene);
        }
    }
}
