package cn.edu.sustech.cs209.chatting.client.controller;

import cn.edu.sustech.cs209.chatting.client.ChattingClient;
import cn.edu.sustech.cs209.chatting.client.ClientService;
import cn.edu.sustech.cs209.chatting.client.controller.LoginController;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.stream.Collectors;
import javafx.util.Callback;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatController implements Initializable {
    @FXML
    public TextArea inputArea;
    @FXML
    ListView<Message> chatContentList;
    private static ScheduledExecutorService scheduledExecutorService;
    @FXML
    ListView<User> chatList;

    @FXML
    private Label currentOnlineCnt;

    @FXML
    private Label username;

    static int id;

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

    public void setId(int id1) {
        id = id1;
    }

    @FXML
    private void doSendMessage() throws IOException {
        String content = inputArea.getText();
        if(content.equals(""))
            return;
        try {
            chattingClient.sendMessage(content, id, currentChatId);
        } catch (Exception e) {
            System.out.println("no");
        }
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
        Stage stage = new Stage();
        ListView<User> userSel = new ListView<>();
        ArrayList<User> selected = new ArrayList<>();
        selected.add(User.getUserById(id));
        userSel.setCellFactory(CheckBoxListCell.forListView(new Callback<User, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(User item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isSelected) -> {
                    System.out.println(isSelected);
                    if (isSelected) {
                        selected.add(item);
                    } else {
                        selected.remove(item);
                    }
                });
                return observable;
            }
        }));

        ArrayList<User> arrayList = User.getUsers();
        for (User user1 : arrayList) {
            if (user1.getId() != id)
                userSel.getItems().add(user1);
        }
        Button okBtn = new Button("OK");

        okBtn.setOnAction(e -> {
            StringBuilder stringBuilder = new StringBuilder();
            selected.sort(Comparator.comparingInt(User::getId));
            for (User item : selected) {
                System.out.println(item.getId());
                stringBuilder.append(item.getId()).append("&");
            }
            if (users.contains(User.getUserByName(stringBuilder.toString()))) {
                if (currentChatId != 0)
                    changeChat = 1;
                currentChatId = Objects.requireNonNull(User.getUserByName(stringBuilder.toString())).getId();
                getHistory(currentChatId);
                stage.close();
                return;
            }
            users.add(User.getUserByName(stringBuilder.toString()));
            chatList.setItems(users);
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

    }

    public void init() {
        class temporal {
            User user;
            long time;
        }
        PriorityQueue<temporal> priorityQueue = new PriorityQueue<>((o1, o2) -> (int) (-o1.time + o2.time));
        for (User user : User.getAllUsers()) {
            ArrayList<Message> arrayList = chattingClient.getHistoryMessage(id, user.getId());
            if (arrayList.isEmpty())
                continue;
            long time = arrayList.get(arrayList.size() - 1).getTimestamp();
            temporal t = new temporal();
            t.time = time;
            t.user = user;
            priorityQueue.add(t);
        }
        while (!priorityQueue.isEmpty()) {
            users.add(priorityQueue.poll().user);
        }
        chatList.setItems(users);
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
                System.out.println(newUser);
                System.out.println(currentChatId + "id");
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

    private void getHistory(int id1) {
        try {
            ArrayList<Message> arrayList = chattingClient.getHistoryMessage(id, id1);
            messages = FXCollections.observableArrayList(
                    arrayList
            );
            chatContentList.setItems(messages);
        } catch (Exception e) {
            System.out.println("Server not available");
        }
    }


    private void getRealTime() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {

            // 在 JavaFX Application 线程上执行 UI 更新操作
            Platform.runLater(() -> {
                try {
                    ArrayList<User> list = ClientService.searchOnline();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (User user : list) {
                        stringBuilder.append(user.getName()).append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length()-1);
                    currentOnlineCnt.setText(stringBuilder.toString());
                } catch (SQLException e) {
                    System.out.println("Error");
                }
                if (currentChatId == 0)
                    return;
                try {
                    ArrayList<Message> arrayList = chattingClient.getRealTimeMessage(currentChatId, id);
                    if (arrayList.size() > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setHeaderText("This is a popup notification");
                        alert.setContentText("New message");
                        alert.showAndWait();
                        messages.addAll(arrayList.stream().
                                filter(message -> message.getSentBy().getId() != id)
                                .collect(Collectors.toList()));
                        chatContentList.setItems(messages);
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Information");
                    alert.setHeaderText("This is a popup notification");
                    alert.setContentText("Server not available!");

                    alert.showAndWait();
                    scheduledExecutorService.shutdown();
                }

            });
        };
        long initialDelay = 0;
        long period = 2;
        scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

    }

    public static void shutdown() {
        try {
            ClientService.updateOnline(id,0);
        } catch (SQLException e) {
            System.out.println("Error");;
        }
        scheduledExecutorService.shutdown();
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
