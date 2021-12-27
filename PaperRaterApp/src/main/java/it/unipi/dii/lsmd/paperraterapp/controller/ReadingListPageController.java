package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Iterator;

public class ReadingListPageController {
    private ReadingList readingList;

    @FXML private Label username;
    @FXML private Label readingListTitle;
    @FXML private ImageView backIcon;
    @FXML private VBox papersBox;

    public void initialize () {
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
    }

    public void setReadingList (ReadingList readingList, String username) {
        this.readingList = readingList;
        this.username.setText(username);
        readingListTitle.setText(readingList.getName());

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

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", mouseEvent);
        ctrl.setProfilePage(Session.getInstance().getPreviousPageUser());
    }
}
