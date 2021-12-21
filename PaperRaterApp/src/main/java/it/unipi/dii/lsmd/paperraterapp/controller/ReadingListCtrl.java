package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriverE;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingListCtrl {
    ReadingList r;
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;

    @FXML private Label readingListTitle;
    @FXML private Text nFollower;
    @FXML private Text nPapers;
    @FXML private Text mostCommonCategory;
    @FXML private Text mostFamousPaperTitle;

    public void initialize () {
        neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());

        readingListTitle.setOnMouseClicked(mouseEvent -> clickOnReadingListTitle(mouseEvent));
    }

    public void setReadingListCard (ReadingList r) {
        this.r = r;

        readingListTitle.setText(r.getName());
        //nFollower.setText(neoMan.getFollowers());
        mostCommonCategory.setText(mostCommonCategory(r.getPapers()));
        nPapers.setText(String.valueOf(r.getPapers().size()));
        //mostFamousPaperTitle.setText(getMostFamousPaperInReadingList()
    }

    private String mostCommonCategory(List<Paper> papers) {
        Map<String, Integer> map = new HashMap<>();

        for (Paper p : papers) {
            Integer val = map.get(p.getCategory());
            map.put(p.getCategory(), val == null ? 1 : val + 1);
        }

        Map.Entry<String, Integer> max = null;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    private void clickOnReadingListTitle (MouseEvent mouseEvent) {
        System.out.println("Click on Reading List " + r.getName());

        // Change scene View Reading List
    }
}
