package it.unipi.dii.lsmd.paperraterapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml")); //login
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("    PaperRater");
            stage.getIcons().add(new Image(getClass().getResourceAsStream( "img/PRlogo.png")));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}