package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.*;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class UserCardCtrl implements Initializable {

    @FXML private Text numFollowerTf;
    @FXML private Text numReadingListTf;
    @FXML private Circle imageProfile;
    @FXML private Label usernameLb;

    private User user;
    private Neo4jManager neoMan;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        //mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
    }

    public void setParameters (User user) {
        this.user = user;

        // set the username
        usernameLb.setText(user.getUsername());

        // set image
        Image image = new Image(user.getPicture(),false);
        imageProfile.setFill(new ImagePattern(image));
        imageProfile.setEffect(new DropShadow(+25d, 0d, +2d, Color.ORANGE));

        // set number of followers
        String numFollowers = Integer.toString(neoMan.getNumFollowersUser(user.getUsername()));
        numFollowerTf.setText(numFollowers);

        // set number of reading list
        String numReadingList = Integer.toString(neoMan.getNumReadingList(user.getUsername()));
        numReadingListTf.setText(numReadingList);
    }

    @FXML
    void showProfile(MouseEvent event) {
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", event);
        ctrl.setProfilePage(user);
    }

}
