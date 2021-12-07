package it.unipi.pr.paperraterapp.GUIcontroller;

import it.unipi.pr.paperraterapp.GUIApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordTf;

    @FXML
    private Button registerButton;

    @FXML
    private TextField usernameTf;

    @FXML
    void checkCredential(ActionEvent event) {
        System.out.println("test");
    }

    @FXML
    void loadRegisterForm(ActionEvent event) throws IOException {
        Stage stage;
        Parent root;
        if (event.getSource() == registerButton) {
            stage = (Stage) registerButton.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("/it/unipi/pr/paperraterapp/layouts/register.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("LOG: move to register.");
        }
    }

}
