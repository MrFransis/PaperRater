package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
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
        System.out.println("LOG: test credential");
        String username = usernameTf.getText();
        String password = passwordTf.getText();
        // TO DO: check login credential in the DB
        usernameTf.setText("");
        passwordTf.setText("");
    }

    /**
     * If the user click the button register this function
     * will change the app stage and show the register form
     *
     * @param event
     */
    @FXML
    void loadRegisterForm(ActionEvent event) {
        Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/register.fxml", event);
    }

}
