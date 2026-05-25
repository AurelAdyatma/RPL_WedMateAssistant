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
        // Clear box borders, set background transparent
        cardUser.setStyle("-fx-background-color: transparent;");
        cardAdmin.setStyle("-fx-background-color: transparent;");

        if (isAdminSelected) {
            // Admin selected
            cardAdmin.setOpacity(1.0);
            cardAdmin.setScaleX(1.05);
            cardAdmin.setScaleY(1.05);
            
            cardUser.setOpacity(0.5);
            cardUser.setScaleX(0.95);
            cardUser.setScaleY(0.95);
        } else {
            // User selected
            cardUser.setOpacity(1.0);
            cardUser.setScaleX(1.05);
            cardUser.setScaleY(1.05);
            
            cardAdmin.setOpacity(0.5);
            cardAdmin.setScaleX(0.95);
            cardAdmin.setScaleY(0.95);
        }
    }

    @FXML
    private void handleLanjutkan(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            if (isAdminSelected) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene);
                stage.setTitle("WedMate - Login Admin");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
                Parent root = loader.load();
                
                ChatController chatController = loader.getController();
                User mockUser = new User(1, "user", "password", "Pengguna", "user@example.com", "08123456789");
                chatController.setUserLogin(mockUser);
                
                Scene scene = new Scene(root, 1280, 800);
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
