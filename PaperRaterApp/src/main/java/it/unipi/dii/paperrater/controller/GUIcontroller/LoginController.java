package it.unipi.pr.paperraterapp.GUIcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
        Stage stage;
        Parent root;
        if (event.getSource() == registerButton) {
            stage = (Stage) registerButton.getScene().getWindow();
            try{
                root = FXMLLoader.load(getClass().getResource("/it/unipi/pr/paperraterapp/layouts/register.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                System.err.println("ERROR: Can't find the fxml file.");
                e.printStackTrace();
            }
            System.out.println("LOG: move to register.");
        }
    }

}
