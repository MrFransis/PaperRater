package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriverE;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentCtrl {
    Comment c;
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;

    @FXML private Label username;
    @FXML private Text timestamp;
    @FXML private Text comment;

    public void initialize () {
    }

    public void setCommentCard (Comment c) {
        this.c = c;

        username.setText(c.getUsername());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        timestamp.setText(formatter.format(c.getTimestamp()));
        comment.setText(c.getText());
    }

}
