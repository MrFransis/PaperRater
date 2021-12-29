package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ReadingListCardCtrl {

    private ReadingList r;
    private String owner;

    @FXML private Label readingListTitle;
    @FXML private Text ownerText;

    public void initialize () {
        readingListTitle.setOnMouseClicked(mouseEvent -> clickOnReadingListTitle(mouseEvent));
    }

    public void setReadingListCard (ReadingList r, String owner) {
        this.r = r;
        this.owner = owner;

        readingListTitle.setText(r.getTitle());
        ownerText.setText(owner);

    }

    private void clickOnReadingListTitle (MouseEvent mouseEvent) {
        ReadingListPageController ctrl = (ReadingListPageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/readinglistpage.fxml", mouseEvent);

        ctrl.setReadingList(r, owner);
    }
}
