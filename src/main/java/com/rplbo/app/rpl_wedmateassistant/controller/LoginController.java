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

/**
 * Controller untuk tampilan Login.
 * Menangani autentikasi User dan Admin, lalu mengarahkan ke view yang sesuai.
 */
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password tidak boleh kosong!");
            lblError.setVisible(true);
            return;
        }

        try {
            if (username.equals("admin") && password.equals("admin")) {
                loadView("/com/rplbo/app/rpl_wedmateassistant/view/AdminPanel.fxml", "Admin Panel");
            } else {
                // Mock user for now
                User mockUser = new User(1, username, password, "Pengguna " + username, username + "@example.com", "08123456789");
                loadChatView(mockUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Gagal memuat tampilan!");
            lblError.setVisible(true);
        }
    }

    private void loadView(String fxmlFile, String title) throws IOException {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
    
    private void loadChatView(User user) throws IOException {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
        Parent root = loader.load();
        
        ChatController chatController = loader.getController();
        chatController.setUserLogin(user);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("WedMate Assistant");
        stage.show();
    }
}
