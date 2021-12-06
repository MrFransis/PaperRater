package it.unipi.pr.paperraterapp.GUIcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    void loadRegisterForm(ActionEvent event) {
        System.out.println("test");
    }

}
