package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;

public class CommentCtrl {
    private Comment c;
    private Paper paper;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;
    private StringProperty text = new SimpleStringProperty();

    @FXML private Label username;
    @FXML private Text timestamp;
    @FXML private Text comment;
    @FXML private ImageView bin;
    @FXML private ImageView modify;
    @FXML private AnchorPane commentBox;
    @FXML private ScrollPane scrollpane;

    public void initialize () {
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        modify.setOnMouseClicked(mouseEvent -> clickOnModify(mouseEvent));
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        username.setOnMouseClicked(mouseEvent -> clickOnUsername(mouseEvent));
    }

    public void setCommentCard (Comment c, Paper paper, boolean browser) {
        this.c = c;
        this.paper = paper;
        if (browser)
            bin.setOnMouseClicked(mouseEvent -> clickOnBinBrowser(mouseEvent));
        else
            bin.setOnMouseClicked(mouseEvent -> clickOnBin(mouseEvent));
        if(Objects.equals(Session.getInstance().getLoggedUser().getUsername(), c.getUsername())) {
            bin.setVisible(true);
            modify.setVisible(true);
        } else {
            if(Session.getInstance().getLoggedUser().getType() > 0) //If the user is a moderator/admin can delete other comments
                bin.setVisible(true);
            else
                bin.setVisible(false);
            modify.setVisible(false);
        }
        username.setText(c.getUsername());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        timestamp.setText(formatter.format(c.getTimestamp()));
        comment.setText(c.getText());
    }

    public StringProperty textProperty() {
        return text ;
    }

    private final String getText() {
        return textProperty().get();
    }

    private final void setText(String text) {
        textProperty().set(text);
    }

    private void clickOnBin (MouseEvent mouseEvent) {
        mongoMan.deleteComment(paper, c);
        ((VBox) commentBox.getParent()).getChildren().remove(commentBox);
        int numComm = Integer.parseInt(getText());
        numComm--;
        setText(String.valueOf(numComm));
    }

    private void clickOnBinBrowser (MouseEvent mouseEvent) {
        paper = mongoMan.getPaperById(paper);
        mongoMan.deleteComment(paper, c);
        ((GridPane) commentBox.getParent()).getChildren().remove(commentBox);
    }

    private void clickOnModify (MouseEvent mouseEvent) {
        TextInputDialog dialog = new TextInputDialog(c.getText());
        dialog.setHeaderText(null);
        dialog.setTitle("Edit comment");
        Optional<String> result = dialog.showAndWait();
        c.setText(result.get());
        comment.setText(result.get());
        if (result.isPresent()){
            mongoMan.updateComment(paper, c);
        }
    }

    private void clickOnUsername(MouseEvent mouseEvent){
        User u = mongoMan.getUserByUsername(c.getUsername());
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", mouseEvent);
        ctrl.setProfilePage(u);
    }
}
