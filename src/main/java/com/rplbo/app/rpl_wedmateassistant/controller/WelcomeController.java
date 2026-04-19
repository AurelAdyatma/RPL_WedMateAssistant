package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private void handleUserClick(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
            Parent root = loader.load();
            
            ChatController chatController = loader.getController();
            User mockUser = new User(1, "user", "password", "Pengguna", "user@example.com", "08123456789");
            chatController.setUserLogin(mockUser);
            
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setTitle("WedMate Assistant - Chatbot");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminClick(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 400, 400);
            stage.setScene(scene);
            stage.setTitle("WedMate - Login Admin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
