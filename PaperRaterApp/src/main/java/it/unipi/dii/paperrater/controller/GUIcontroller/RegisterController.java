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

public class RegisterController {

    @FXML
    private TextField ageTf;

    @FXML
    private TextField emailTf;

    @FXML
    private TextField firstNameTf;

    @FXML
    private TextField lastNameTf;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordTf;

    @FXML
    private Button signUPButton;

    @FXML
    private TextField usernameTf;

    @FXML
    void checkForm(ActionEvent event) {

    }

    @FXML
    void loadLogin(ActionEvent event) throws IOException {
        Stage stage;
        Parent root;
        if (event.getSource() == loginButton) {
            stage = (Stage) loginButton.getScene().getWindow();
            try{
                root = FXMLLoader.load(getClass().getResource("/it/unipi/pr/paperraterapp/layouts/login.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                System.err.println("ERROR: Can't find the fxml file.");
                e.printStackTrace();
            }
            System.out.println("LOG: move to login.");
        }
    }

}
