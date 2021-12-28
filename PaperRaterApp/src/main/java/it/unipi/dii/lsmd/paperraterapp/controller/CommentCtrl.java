package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;

public class CommentCtrl {
    private Comment c;
    private Paper paper;
    private String user;
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
        bin.setOnMouseClicked(mouseEvent -> clickOnBin(mouseEvent));
        modify.setOnMouseClicked(mouseEvent -> clickOnModify(mouseEvent));
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void setCommentCard (Comment c, String user, Paper paper) {
        this.c = c;
        this.paper = paper;
        this.user = user;
        if(Objects.equals(user, c.getUsername())) {
            mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
            neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
            bin.setVisible(true);
            modify.setVisible(true);
        } else {
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
        if(mongoMan.numUserComments(paper.getId(), user) == 0)
            neoMan.deleteHasCommented(c.getUsername(), paper.getId());
        ((VBox) commentBox.getParent()).getChildren().remove(commentBox);
        int numComm = Integer.parseInt(getText());
        numComm--;
        setText(String.valueOf(numComm));
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
}
