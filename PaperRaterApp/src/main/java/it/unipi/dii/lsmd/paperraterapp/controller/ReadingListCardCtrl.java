package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ReadingListCardCtrl {
    private MongoDBManager mongoMan;
    private ReadingList r;
    private String owner;

    @FXML private Label readingListTitle;
    @FXML private Text ownerText;
    @FXML private Label analyticLabelName;
    @FXML private Text analyticValue;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        readingListTitle.setOnMouseClicked(mouseEvent -> clickOnReadingListTitle(mouseEvent));
    }

    public void setReadingListCard (ReadingList r, String owner, String analyticLabelName, int analyticValue) {
        this.r = r;
        this.owner = owner;

        readingListTitle.setText(r.getTitle());
        ownerText.setText(owner);

        if (analyticLabelName != null) {
            this.analyticLabelName.setText(analyticLabelName);
            this.analyticValue.setText(String.valueOf(analyticValue));
        }
        else {
            this.analyticLabelName.setVisible(false);
            this.analyticValue.setVisible(false);
        }
    }

    private void clickOnReadingListTitle (MouseEvent mouseEvent) {
        ReadingListPageController ctrl = (ReadingListPageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/readinglistpage.fxml", mouseEvent);

        // If reading list object is a snapshot, load the complete reading list object
        if (r.getPapers() == null)
            r = mongoMan.getReadingList(owner, r.getTitle());

        ctrl.setReadingList(r, owner);
    }
}
