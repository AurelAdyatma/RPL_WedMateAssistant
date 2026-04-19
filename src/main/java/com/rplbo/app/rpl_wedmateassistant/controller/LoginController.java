package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password tidak boleh kosong!");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        try {
            if (username.equalsIgnoreCase("admin") && password.equals("admin")) {
                loadView("/com/rplbo/app/rpl_wedmateassistant/view/AdminPanel.fxml", "Admin Panel");
            } else {
                User mockUser = new User(
                        1,
                        username,
                        password,
                        "Pengguna " + username,
                        username + "@example.com",
                        "08123456789"
                );
                loadChatView(mockUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Gagal memuat tampilan!");
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    private void loadView(String fxmlFile, String title) throws IOException {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }

    private void loadChatView(User user) throws IOException {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml")
        );
        Parent root = loader.load();

        ChatController chatController = loader.getController();
        chatController.setUserLogin(user);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("WedMate Assistant");
        stage.centerOnScreen();
        stage.show();
    }
}
