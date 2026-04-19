package com.rplbo.app.rpl_wedmateassistant;

import com.rplbo.app.rpl_wedmateassistant.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.getInstance().initDB();

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        stage.setTitle("WedMate Assistant");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
    }
}
