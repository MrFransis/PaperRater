package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReadingListPageController {
    private ReadingList readingList;
    private String owner;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;

    @FXML private Label username;
    @FXML private Label readingListTitle;
    @FXML private Text mostCommonCategory;
    @FXML private Text numFollowers;
    @FXML private Text numPapers;
    @FXML private ImageView backIcon;
    @FXML private VBox papersBox;
    @FXML private Button deleteReadingListBtn;
    @FXML private Button followBtn;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        followBtn.setOnMouseClicked(mouseEvent -> clickOnFollowBtn(mouseEvent));
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        deleteReadingListBtn.setOnMouseClicked(mouseEvent -> clickOnDeleteBtn(mouseEvent));
    }

    public void setReadingList (ReadingList readingList, String owner) {
        this.readingList = readingList;
        this.owner = owner;

        // Push
        Session.getInstance().getPreviousPageReadingList().add(new Pair(owner,readingList));

        username.setText(owner);
        readingListTitle.setText(readingList.getTitle());
        mostCommonCategory.setText(getMostCommonCategory(readingList.getPapers()));
        numFollowers.setText(String.valueOf(neoMan.getNumFollowersReadingList(readingList.getTitle(), owner)));
        numPapers.setText(String.valueOf(readingList.getPapers().size()));

        if (!readingList.getPapers().isEmpty()) {
            papersBox.getChildren().clear();
            Iterator<Paper> it = readingList.getPapers().iterator();

            while(it.hasNext()) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER);
                row.setStyle("-fx-padding: 10px");
                Paper paper = it.next();
                Pane p = loadPaperCard(paper);

                row.getChildren().addAll(p);
                papersBox.getChildren().add(row);
            }
        }
        else {
            papersBox.getChildren().add(new Label("No Papers :("));
        }

        if (owner.equals(Session.getInstance().getLoggedUser().getUsername())) {
            followBtn.setVisible(false);
            deleteReadingListBtn.setVisible(true);
        }
        else {
            followBtn.setVisible(true);
            deleteReadingListBtn.setVisible(false);
        }

        if (neoMan.isUserFollowingReadingList(Session.getInstance().getLoggedUser().getUsername(), owner, readingList))
            followBtn.setText("Unfollow");

    }

    private Pane loadPaperCard (Paper p) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/papercard.fxml"));
            pane = loader.load();
            PaperCardCtrl ctrl = loader.getController();
            boolean showDeleteBtn = Session.getInstance().getLoggedUser().getUsername().equals(username.getText());
            ctrl.setPaperCard(p, showDeleteBtn, this);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private String getMostCommonCategory(List<Paper> papers) {
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

    private void clickOnFollowBtn (MouseEvent mouseEvent) {
        String tmp = followBtn.getText();
        if (tmp.equals("Follow")) {
            neoMan.followReadingList(readingList.getTitle(), owner, Session.getInstance().getLoggedUser().getUsername());
            numFollowers.setText(String.valueOf(neoMan.getNumFollowersReadingList(readingList.getTitle(), owner)));
            followBtn.setText("Unfollow");
        }
        else {
            neoMan.unfollowReadingList(readingList.getTitle(), owner, Session.getInstance().getLoggedUser().getUsername());
            numFollowers.setText(String.valueOf(neoMan.getNumFollowersReadingList(readingList.getTitle(), owner)));
            followBtn.setText("Follow");
        }
    }

    private void clickOnDeleteBtn (MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Reading List?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            mongoMan.deleteReadingList(username.getText(), readingList.getTitle());
            neoMan.deleteReadingList(readingList.getTitle(), username.getText());

            User owner = Session.getInstance().getPreviousPageUsers().get(Session.getInstance().getPreviousPageUsers().size()-1);
            owner.getReadingLists().remove(readingList);
            clickOnBackIcon(mouseEvent);
        }
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPageReadingList().remove(
                Session.getInstance().getPreviousPageReadingList().size() - 1);

        // Check if previous page was a Profile Page
        if (Session.getInstance().getPreviousPageUsers().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml", mouseEvent);
        else {
            ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                        "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", mouseEvent);
            ctrl.setProfilePage(Session.getInstance().getPreviousPageUsers().remove(
                    Session.getInstance().getPreviousPageUsers().size()-1));
        }
    }
}
