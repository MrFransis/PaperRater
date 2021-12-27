package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class RegisterController {
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;

    @FXML private TextField ageTf;
    @FXML private TextField emailTf;
    @FXML private TextField firstNameTf;
    @FXML private TextField lastNameTf;
    @FXML private Button loginButton;
    @FXML private PasswordField passwordTf;
    @FXML private Button signUPButton;
    @FXML private TextField usernameTf;


    public void initialize () {
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
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
                                lastNameTf.getText(), "", Integer.parseInt(ageTf.getText()), new ArrayList<ReadingList>());

        mongoMan.addUser(newUser);

        // add to neo4j
        Session.getInstance().setLoggedUser(newUser);
        Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml", event);
    }

    /**
     * If the user click the button register this function
     * will change the app stage and show the register form
     *
     * @param event
     */
    @FXML
    void loadLogin(ActionEvent event) {
        Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml", event);
    }
}
