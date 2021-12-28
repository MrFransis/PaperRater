package it.unipi.dii.lsmd.paperraterapp.controller;

import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.persistence.*;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;

public class PaperCardCtrl {
    private Paper p;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;
    private ReadingListPageController readingListPageCtrl;

    @FXML private Label paperId;
    @FXML private Label paperTitle;
    @FXML private Label paperAuthors;
    @FXML private Text paperCategory;
    @FXML private Text paperLikes;
    @FXML private Button removeFromReadingListBtn;

    public void initialize () {
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());

        paperTitle.setOnMouseClicked(mouseEvent -> clickOnPaperTitle(mouseEvent));
        removeFromReadingListBtn.setOnMouseClicked(mouseEvent -> clickOnRemoveFromReadingListBtn(mouseEvent));
    }

    public void setPaperCard (Paper p, boolean showDeleteBtn, ReadingListPageController readingListPageCtrl) {
        this.p = p;

        String validId;
        if (!p.getArxivId().isEmpty()) {
            validId = p.getArxivId();
            paperId.setText("arXiv:" + validId);
        }
        else {
            validId = p.getVixraId();
            paperId.setText("viXra:" + validId);
        }

        paperTitle.setText(p.getTitle());

        // set the authors list
        String tmp = "";
        int k = 0;
        for (String author: p.getAuthors()) {
            if (k == 0) {
                k = 1;
                tmp += author;
            }
            else
                tmp +=", " + author;
        }
        paperAuthors.setText(tmp);
        paperCategory.setText(p.getCategory());
        paperLikes.setText(String.valueOf(neoMan.getNumLikes(validId)));

        if (showDeleteBtn) {
            removeFromReadingListBtn.setVisible(true);
            this.readingListPageCtrl = readingListPageCtrl;
        }
        else {
            removeFromReadingListBtn.setVisible(false);
            this.readingListPageCtrl = null;
        }
    }

    private void clickOnPaperTitle (MouseEvent mouseEvent) {
        PaperPageController ctrl = (PaperPageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/paperpage.fxml", mouseEvent);
        ctrl.setPaperPage(p);
    }

    private void clickOnRemoveFromReadingListBtn(MouseEvent mouseEvent) {
        UpdateResult res = mongoMan.removePaperFromReadingList(Session.getInstance().getLoggedUser().getUsername(),
                Session.getInstance().getPreviousPageReadingList().get(
                        Session.getInstance().getPreviousPageUser().size() - 1
                ).getName(),
                p);

        Session.getInstance().getPreviousPageReadingList().get(
                Session.getInstance().getPreviousPageReadingList().size() - 1).getPapers().remove(p);

        // Refresh ReadingListPage
        readingListPageCtrl.setReadingList(Session.getInstance().getPreviousPageReadingList().get(
                Session.getInstance().getPreviousPageReadingList().size() - 1
                ),
                Session.getInstance().getLoggedUser().getUsername());
    }
}
