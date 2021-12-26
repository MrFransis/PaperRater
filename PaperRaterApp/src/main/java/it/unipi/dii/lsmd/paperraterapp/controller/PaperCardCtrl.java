package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.persistence.*;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class PaperCardCtrl {
    private Paper p;
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;           // da unire
    private Neo4jManagerT neoMan1;

    @FXML private Label paperId;
    @FXML private Label paperTitle;
    @FXML private Label paperAuthors;
    @FXML private Text paperCategory;
    @FXML private Text paperLikes;
    @FXML private Text paperNComments;

    public void initialize () {
        neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        neoMan1 = new Neo4jManagerT(Neo4jDriverE.getInstance().openConnection());       // da unire
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());

        paperTitle.setOnMouseClicked(mouseEvent -> clickOnPaperTitle(mouseEvent));
    }

    public void setPaperCard (Paper p) {
        this.p = p;
        // set the id
        String validId;
        if (!p.getArxiv_id().isEmpty())
            validId = p.getArxiv_id();
        else
            validId = p.getVixra_id();
        paperId.setText("arXiv:" + validId);

        // set the title
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

        // set category
        paperCategory.setText(p.getCategory());

        // set num likes
        String numLikes = Integer.toString(neoMan1.getNumLikes(validId));
        paperLikes.setText(numLikes);

        // set num comments
        String numComments = Integer.toString(neoMan1.getNumComments(validId));
        paperNComments.setText(numComments);
    }

    private void clickOnPaperTitle (MouseEvent mouseEvent) {
        PaperPageController ctrl = (PaperPageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/paperpage.fxml", mouseEvent);
        ctrl.setPaperPage(p);
    }

}
