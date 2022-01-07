package it.unipi.dii.lsmd.paperraterapp.controller;

import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.*;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class PaperPageController implements Initializable {
    private Paper paper;
    private User user;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;
    private final int maxLength = 280;
    private String linkPdf;
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
    @FXML private Text comNum;
    @FXML private ScrollPane scrollpane;
    @FXML private Button likebtn;
    @FXML private Button webLink;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commentsBox.setSpacing(10);
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        addToReadingList.setOnMouseClicked(mouseEvent -> clickOnAddToReadingListBtn(mouseEvent));
        likebtn.setOnMouseClicked(mouseEvent -> clickLike(mouseEvent));
        webLink.setOnMouseClicked(mouseEvent -> clickOpenPdf(mouseEvent));
        comment.setOnMouseClicked(mouseEvent -> clickOnAddCommentBtn(mouseEvent));
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void setPaperPage (Paper p) {
        this.paper = mongoMan.getPaperById(p);
        this.user = Session.getInstance().getLoggedUser();

        // Push
        Session.getInstance().getPreviousPagePaper().add(p);

        title.setText(paper.getTitle());

        String validId;
        if (!Objects.equals(p.getArxivId(), "nan")) {
            validId = p.getArxivId();
            id.setText("arXiv:" + validId);
            linkPdf = "https://arXiv.org/pdf/" + validId + "v1.pdf";
        }
        else {
            validId = p.getVixraId();
            id.setText("viXra:" + validId);
            linkPdf = "https://vixra.org/pdf/" + validId + "v1.pdf";
        }
        category.setText(paper.getCategory());
        authors.setText(paper.getAuthors().toString());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        published.setText(formatter.format(paper.getPublished()));
        abstractPaper.setText(paper.getAbstract());
        if(neoMan.userLikePaper(user.getUsername(), paper))
            likebtn.setText("Unlike");
        else
            likebtn.setText("Like");
        likes.setText(Integer.toString(neoMan.getNumLikes(paper)));
        setCommentBox();
    }

    private void setCommentBox() {
        int numComment = 0;
        if (paper.getComments() != null) {
            commentsBox.getChildren().clear();
            Iterator<Comment> it = paper.getComments().iterator();

            while(it.hasNext()) {
                //VBox row = new VBox();
                Comment c = it.next();
                Pane p = loadCommentCard(c, paper);
                //row.getChildren().add(p);
                commentsBox.getChildren().add(p);
                numComment++;
            }
        }
        comNum.setText(String.valueOf(numComment));
    }

    private Pane loadCommentCard (Comment c, Paper p) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/commentcard.fxml"));
            pane = loader.load();
            CommentCtrl ctrl = loader.getController();
            ctrl.textProperty().bindBidirectional(comNum.textProperty());
            ctrl.setCommentCard(c, user, p, false);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPagePaper().remove(
                Session.getInstance().getPreviousPagePaper().size() - 1);

        // Check if previous page is Reading List Page
        if (Session.getInstance().getPreviousPageReadingList().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml", mouseEvent);
        else {
            ReadingListPageController ctrl = (ReadingListPageController) Utils.changeScene(
                    "/it/unipi/dii/lsmd/paperraterapp/layout/readinglistpage.fxml", mouseEvent);

            // Get Previous Page Reading List info
            Pair<String, ReadingList> p = Session.getInstance().getPreviousPageReadingList().get(
                    Session.getInstance().getPreviousPageReadingList().size() - 1);

            ctrl.setReadingList(p.getValue(), p.getKey());
        }
    }

    private void clickOnAddToReadingListBtn (MouseEvent mouseEvent) {
        if (!Session.getInstance().getLoggedUser().getReadingLists().isEmpty()) {
            Iterator<ReadingList> it = Session.getInstance().getLoggedUser().getReadingLists().iterator();
            List<String> choices = new ArrayList<>();
            while(it.hasNext()) {
                choices.add(it.next().getTitle());
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Choose reading list");
            dialog.setHeaderText(null);
            dialog.setContentText("Reading list:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){

                // Check if selected Reading List does not exceed limit
                for (int i=0; i<Session.getInstance().getLoggedUser().getReadingLists().size(); i++) {
                    ReadingList tmp = Session.getInstance().getLoggedUser().getReadingLists().get(i);
                    if (tmp.getPapers().size() > 100) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText(null);
                        alert.setContentText("Too many papers in selected Reading List");
                        alert.showAndWait();
                        return;
                    }
                }

                UpdateResult res = mongoMan.addPaperToReadingList(Session.getInstance().getLoggedUser().getUsername(), result.get(), paper);
                if(res.getModifiedCount() == 0){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("This paper is already present in this reading list!");
                    alert.showAndWait();
                }
                else {
                    // Update Session User Object
                    for (ReadingList r : Session.getInstance().getLoggedUser().getReadingLists()) {
                        if (r.getTitle().equals(result.get())) {
                            r.getPapers().add(paper);
                            break;
                        }
                    }
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
        if((!commentText.getText().isEmpty()) || (commentText.getText().length() <= maxLength)){
            Comment comment = new Comment(user.getUsername(), commentText.getText(), new Date());
            mongoMan.addComment(paper, comment);
            paper = mongoMan.getPaperById(paper);
            setCommentBox();
            commentText.setText("");

        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Insert a commnet!");
            alert.showAndWait();
        }
    }

    private void clickLike (MouseEvent mouseEvent){
        if(Objects.equals(likebtn.getText(), "Like")){
            neoMan.like(user, paper);
            likes.setText(Integer.toString(neoMan.getNumLikes(paper)));
            likebtn.setText("UnLike");
        }else{
            neoMan.unlike(user, paper);
            likes.setText(Integer.toString(neoMan.getNumLikes(paper)));
            likebtn.setText("Like");
        }
    }

    private void clickOpenPdf (MouseEvent mouseEvent){
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(linkPdf));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
}
