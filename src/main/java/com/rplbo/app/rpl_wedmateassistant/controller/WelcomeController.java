package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML private VBox cardUser;
    @FXML private VBox cardAdmin;

    // Default ke User
    private boolean isAdminSelected = false;

    @FXML
    public void initialize() {
        // Set awal
        updateSelection();
    }

    @FXML
    private void handleUserClick(MouseEvent event) {
        isAdminSelected = false;
        updateSelection();
    }

    @FXML
    private void handleAdminClick(MouseEvent event) {
        isAdminSelected = true;
        updateSelection();
    }

    private void updateSelection() {
        if (isAdminSelected) {
            cardAdmin.setStyle("-fx-background-color: #F4F9FD; -fx-background-radius: 12; -fx-border-color: #D97706; -fx-border-radius: 12; -fx-border-width: 3;");
            cardUser.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");
        } else {
            cardUser.setStyle("-fx-background-color: #F4F9FD; -fx-background-radius: 12; -fx-border-color: #3B82F6; -fx-border-radius: 12; -fx-border-width: 3;");
            cardAdmin.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");
        }
    }

    @FXML
    private void handleLanjutkan(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            if (isAdminSelected) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("WedMate - Login Admin");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
                Parent root = loader.load();
                
                ChatController chatController = loader.getController();
                User mockUser = new User(1, "user", "password", "Pengguna", "user@example.com", "08123456789");
                chatController.setUserLogin(mockUser);
                
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("WedMate Assistant - Chatbot");
            }
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
