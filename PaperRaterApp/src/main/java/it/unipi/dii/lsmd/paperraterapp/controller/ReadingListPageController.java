package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ReadingListPageController {
    private ReadingList r;

    @FXML private Label username;
    @FXML private Label readingListTitle;
    @FXML private ImageView backIcon;
    @FXML private VBox papersBox;

    public void initialize () {
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
    }

    public void setReadingList (ReadingList r) {
        this.r = r;
        username.setText(Session.getInstance().getPreviousPageUser().getUsername());
        readingListTitle.setText(r.getName());

    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", mouseEvent);
        ctrl.setProfilePage(Session.getInstance().getPreviousPageUser());
    }
}
