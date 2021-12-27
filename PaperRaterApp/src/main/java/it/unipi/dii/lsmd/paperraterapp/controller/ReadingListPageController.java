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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Iterator;

public class ReadingListPageController {
    private ReadingList readingList;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;

    @FXML private Label username;
    @FXML private Label readingListTitle;
    @FXML private Text numFollowers;
    @FXML private Text numPapers;
    @FXML private ImageView backIcon;
    @FXML private VBox papersBox;
    @FXML private Button deleteReadingListBtn;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        deleteReadingListBtn.setOnMouseClicked(mouseEvent -> clickOnDeleteBtn(mouseEvent));
    }

    public void setReadingList (ReadingList readingList, String username) {
        this.readingList = readingList;
        this.username.setText(username);

        readingListTitle.setText(readingList.getName());
        numFollowers.setText(String.valueOf(neoMan.getNumFollowersReadingList(readingList.getName(), username)));
        numPapers.setText(String.valueOf(readingList.getPapers().size()));

        // Push
        Session.getInstance().getPreviousPageReadingList().add(readingList);

        if (!readingList.getPapers().isEmpty()) {
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
            papersBox.getChildren().add(new Label("No Reading Lists :("));
        }
    }

    private Pane loadPaperCard (Paper p) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/papercard.fxml"));
            pane = loader.load();
            PaperCardCtrl ctrl = loader.getController();
            ctrl.setPaperCard(p);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnDeleteBtn (MouseEvent mouseEvent) {
        mongoMan.deleteReadingList(username.getText(), readingList.getName());
        neoMan.deleteReadingList(readingList.getName(), username.getText());

        User owner = Session.getInstance().getPreviousPageUser().get(Session.getInstance().getPreviousPageUser().size()-1);
        owner.getReadingLists().remove(readingList);
        clickOnBackIcon(mouseEvent);
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPageReadingList().remove(
                Session.getInstance().getPreviousPageReadingList().size() - 1);

        if (Session.getInstance().getPreviousPageUser().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml", mouseEvent);
        else {
            ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                        "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", mouseEvent);
            ctrl.setProfilePage(Session.getInstance().getPreviousPageUser().remove(
                    Session.getInstance().getPreviousPagePaper().size()));
        }
    }
}
