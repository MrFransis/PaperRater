package it.unipi.dii.lsmd.paperraterapp;

import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            stage.setOnCloseRequest((WindowEvent we) -> {
                MongoDriver.getInstance().closeConnection();
                Neo4jDriver.getInstance().closeConnection();
                System.exit(0);
            });
            Parent root = FXMLLoader.load(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml")); //login
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("    PaperRater");
            stage.getIcons().add(new Image(getClass().getResourceAsStream( "img/iconApp.png")));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}