package cn.edu.sustech.cs209.chatting.client.controller;

import cn.edu.sustech.cs209.chatting.client.ChattingClient;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ChatController implements Initializable {
    @FXML

    public TextArea inputArea;
    @FXML
    ListView<Message> chatContentList;
    private ScheduledExecutorService scheduledExecutorService;
    @FXML
    ListView<User> chatList;

    @FXML
    private Label username;

    int id;

    int currentChatId = 0;

    int changeChat = 0;

    ChattingClient chattingClient;
    String password;

    ObservableList<User> users;

    ObservableList<Message> messages;
    @FXML
    private VBox chatArea;


    public void setUsername(String username) {
        this.username.setText("Hello! " + username + " ");
    }

    public void setId(int id) {
        this.id = id;
    }

    @FXML
    private void doSendMessage() throws IOException {
        String content = inputArea.getText();
        System.out.println(content);
        chattingClient.sendMessage(content, id, currentChatId);
        messages.add(new Message(System.currentTimeMillis(), User.getUserById(id), User.getUserById(currentChatId), content));
        chatContentList.setItems(messages);
        inputArea.setText("");
    }


    @FXML
    public void createPrivateChat() {
//        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        ArrayList<User> arrayList = User.getUsers();
        for (User user1 : arrayList) {
            if (user1.getId() != id)
                userSel.getItems().add(user1.getName());
        }
        // FIXME: get the user list from server, the current user's name should be filtered out
//        userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            if (users.contains(User.getUserByName(userSel.getSelectionModel().getSelectedItem()))) {
                if (currentChatId != 0)
                    changeChat = 1;
                currentChatId = Objects.requireNonNull(User.getUserByName(userSel.getSelectionModel().getSelectedItem())).getId();
                getHistory(currentChatId);
//                getRealTime();
                stage.close();
                return;
            }
//            user.set(userSel.getSelectionModel().getSelectedItem());
            users.add(User.getUserByName(userSel.getSelectionModel().getSelectedItem()));
            chatList.setItems(users);
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }


    @FXML
    private void createGroupChat() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        users = FXCollections.observableArrayList();
        chattingClient = new ChattingClient("127.0.0.1", 9999);
        chatList.setItems(users);
        chatList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> observableValue, User oldUser, User newUser) {
                if (currentChatId != 0)
                    changeChat = 1;
                currentChatId = newUser.getId();
                getHistory(currentChatId);
//                getRealTime();
            }
        });

        chatContentList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Message>() {
            @Override
            public void changed(ObservableValue<? extends Message> observableValue, Message oldMessage, Message newMessage) {

            }

        });
        chatContentList.setCellFactory(new MessageCellFactory());
        getRealTime();
    }

    private void getHistory(int id) {
        ArrayList<Message> arrayList = chattingClient.getHistoryMessage(this.id, id);
        messages = FXCollections.observableArrayList(
                arrayList
        );
        chatContentList.setItems(messages);
    }


    private void getRealTime() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            // 在 JavaFX Application 线程上执行 UI 更新操作
            Platform.runLater(() -> {
                if(currentChatId==0)
                    return;
                ArrayList<Message> arrayList = chattingClient.getRealTimeMessage(currentChatId, id);
                if (arrayList.size() > 0) {
                    messages.addAll(arrayList);
                    chatContentList.setItems(messages);
                }
            });
        };
        long initialDelay = 0;
        long period =2; // 每隔 0.1 秒执行一次

        scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                while (changeChat == 0) {
//                    ArrayList<Message> arrayList = chattingClient.getRealTimeMessage(currentChatId,id);
//                    if (arrayList.size() > 0) {
//                        System.out.println("  getrealtime:" + arrayList.get(0));
//                        messages.addAll(arrayList);
//                        chatContentList.setItems(messages);
//                    }
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                changeChat = 0;
//            }
//        });

    }


    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label((msg.getSentBy().getName()));
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.getText().equals("Hello! " + msg.getSentBy().getName() + " ")) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

}
