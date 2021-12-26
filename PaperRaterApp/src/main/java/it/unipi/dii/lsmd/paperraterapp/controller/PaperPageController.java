package it.unipi.dii.lsmd.paperraterapp.controller;

import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.*;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaperPageController implements Initializable {
    private Paper paper;
    private User user;
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
    @FXML private ScrollPane scrollpane;
    @FXML private Button likebtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        addToReadingList.setOnMouseClicked(mouseEvent -> clickOnAddToReadingListBtn(mouseEvent));
        likebtn.setOnMouseClicked(mouseEvent -> clickOnLike(mouseEvent));
        comment.setOnMouseClicked(mouseEvent -> clickOnAddCommentBtn(mouseEvent));
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void setPaperPage (Paper p) {
        this.paper = mongoMan.getPaperById(p.getId());
        this.user = Session.getInstance().getUser();
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
            commentsBox.getChildren().clear();
            Iterator<Comment> it = paper.getComments().iterator();

            while(it.hasNext()) {
                VBox row = new VBox();
                Comment c = it.next();
                Pane p = loadCommentCard(c, paper);

                row.getChildren().addAll(p);
                commentsBox.getChildren().add(row);
            }
        }
    }

    private Pane loadCommentCard (Comment c, Paper p) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/comment_card.fxml"));
            pane = loader.load();
            CommentCtrl ctrl = loader.getController();
            ctrl.setCommentCard(c, user.getUsername(), p);

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
                UpdateResult res = mongoMan.addPaperToReadingList(Session.getInstance().getUser().getUsername(), result.get(), paper);
                if(res.getModifiedCount() == 0){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("This paper is already present in this reading list!");
                    alert.showAndWait();
                }
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
            mongoMan.addComment(paper.getId(), commentText.getText(), user.getUsername());
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

    private void clickOnLike (MouseEvent mouseEvent){
        System.out.println("Like");
    }
}
