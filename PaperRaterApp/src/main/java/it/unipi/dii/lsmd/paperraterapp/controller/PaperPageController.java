package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriverE;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());
        System.out.println("Ciao");
        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
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
}
