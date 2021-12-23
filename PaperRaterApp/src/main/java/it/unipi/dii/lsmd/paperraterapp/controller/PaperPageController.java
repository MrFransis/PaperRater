package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.*;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriverE;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaperPageController implements Initializable {
    private Paper paper;
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;

    @FXML private ImageView backIcon;
    @FXML private Text title;
    @FXML private Text id;
    @FXML private Text category;
    @FXML private Text authors;
    @FXML private Text published;
    @FXML private Text likes;
    @FXML private VBox commentsBox;
    @FXML private Button addToReadingList;
    @FXML private Text abstractPaper;
    @FXML private Button comment;
    @FXML private TextField commentText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        addToReadingList.setOnMouseClicked(mouseEvent -> clickOnAddToReadingListBtn(mouseEvent));
        comment.setOnMouseClicked(mouseEvent -> clickOnAddCommentBtn(mouseEvent));
    }

    public void setPaperPage (Paper paper) {
        this.paper = paper;

        title.setText(paper.getTitle());
        id.setText(paper.getId());
        category.setText(paper.getCategory());
        authors.setText(paper.getAuthors().toString());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        published.setText(formatter.format(paper.getPublished()));
        abstractPaper.setText(paper.getAbstract());
        setCommentBox();
    }

    private void setCommentBox() {
        if (paper.getComments() != null) {
            Iterator<Comment> it = paper.getComments().iterator();

            while(it.hasNext()) {
                HBox row = new HBox();
                Comment c = it.next();
                Pane p = loadCommentCard(c);

                row.getChildren().addAll(p);
                commentsBox.getChildren().add(row);
            }
        }
    }

    private Pane loadCommentCard (Comment c) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/comment_card.fxml"));
            pane = loader.load();
            CommentCtrl ctrl = loader.getController();
            ctrl.setCommentCard(c);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        System.out.println("Back");
    }

    private void clickOnAddToReadingListBtn (MouseEvent mouseEvent) {
        if (!Session.getInstance().getUser().getReadingLists().isEmpty()) {
            Iterator<ReadingList> it = Session.getInstance().getUser().getReadingLists().iterator();
            List<String> choices = new ArrayList<>();
            while(it.hasNext()) {
                choices.add(it.next().getName());
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Choose reading list");
            dialog.setHeaderText(null);
            dialog.setContentText("Reading list:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                mongoMan.addPaperToReadingList(Session.getInstance().getUser().getUsername(), result.get(), paper);
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("You haven't created a reading list yet!");
            alert.showAndWait();
        }
    }

    private void clickOnAddCommentBtn (MouseEvent mouseEvent){
        if(!commentText.getText().isEmpty()){
            mongoMan.addComment(paper.getId(), commentText.getText(), Session.getInstance().getUser().getUsername());
            paper = mongoMan.getPaperById(paper.getId());
            setCommentBox();
            commentText.setText("");

        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Inser a commnet!");
            alert.showAndWait();
        }
    }
}
