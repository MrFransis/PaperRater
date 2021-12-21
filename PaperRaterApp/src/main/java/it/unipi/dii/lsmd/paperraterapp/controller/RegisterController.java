package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriverE;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
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
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;

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


    public void initialize () {
        neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
    }

    @FXML
    void checkForm(ActionEvent event) {
        System.out.println("LOG: test credential");
        String username = usernameTf.getText();

        if (mongoMan.getUserByUsername(username) != null) {
            System.out.println("Username already registered");
            return;
        }
        User newUser = new User(username, emailTf.getText(), passwordTf.getText(), firstNameTf.getText(),
                                lastNameTf.getText(), null, Integer.parseInt(ageTf.getText()),null);

        mongoMan.addUser(newUser);
        // add to neo4j

        Session session = Session.getInstance();
        session.setUser(newUser);
        System.out.println("User added");
            //Change scene

    }

    @FXML
    void loadLogin(ActionEvent event) throws IOException {
        Stage stage;
        Parent root;
        if (event.getSource() == loginButton) {
            stage = (Stage) loginButton.getScene().getWindow();
            try{
                root = FXMLLoader.load(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml"));
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
