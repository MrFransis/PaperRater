package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class ProfilePageController {
    private User user;
    private MongoDBManager mongoMan;
    private Neo4jManager neoMan;

    @FXML private ImageView backIcon;
    @FXML private ImageView editIcon;
    @FXML private ImageView profileImg;
    @FXML private Label username;
    @FXML private Text email;
    @FXML private Text firstName;
    @FXML private Text lastName;
    @FXML private Text nFollower;
    @FXML private Text nFollowing;
    @FXML private Button followBtn;
    @FXML private VBox readingListsBox;
    @FXML private Button addReadingListBtn;


    public void initialize () {
        neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        mongoMan = new MongoDBManager(MongoDriver.getInstance().openConnection());

        backIcon.setOnMouseClicked(mouseEvent -> clickOnBackIcon(mouseEvent));
        followBtn.setOnMouseClicked(mouseEvent -> clickOnFollowBtn(mouseEvent));
        nFollowing.setOnMouseClicked(mouseEvent -> clickOnFollowing(mouseEvent));
        nFollower.setOnMouseClicked(mouseEvent -> clickOnFollower(mouseEvent));
        editIcon.setOnMouseClicked(mouseEvent -> clickOnEditIcon(mouseEvent));
        addReadingListBtn.setOnMouseClicked(mouseEvent -> clickOnAddReadingListBtn(mouseEvent));

    }

    public void setProfilePage (User user) {
        this.user = user;

        // Push
        Session.getInstance().getPreviousPageUser().add(user);

        username.setText(user.getUsername());
        email.setText(user.getEmail());
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        nFollower.setText(String.valueOf(neoMan.getNumFollowersUser(user.getUsername())));
        nFollowing.setText(String.valueOf(neoMan.getNumFollowingUser(user.getUsername())));


        if (!user.getPicture().isEmpty())
            profileImg.setImage(new Image(user.getPicture()));

        if (neoMan.userAFollowsUserB(Session.getInstance().getLoggedUser().getUsername(), user.getUsername()))
            followBtn.setText("Unfollow");

        if (user.getUsername().equals(Session.getInstance().getLoggedUser().getUsername())) {
            followBtn.setVisible(false);
            editIcon.setVisible(true);
            addReadingListBtn.setVisible(true);
        }
        else {
            followBtn.setVisible(true);
            editIcon.setVisible(false);
            addReadingListBtn.setVisible(false);
        }

        readingListsBox.getChildren().clear();
        if (!user.getReadingLists().isEmpty()) {
            Iterator<ReadingList> it = user.getReadingLists().iterator();

            while(it.hasNext()) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER);
                row.setStyle("-fx-padding: 10px");
                ReadingList r = it.next();
                Pane p = loadReadingListCard(r, user.getUsername());

                row.getChildren().addAll(p);
                readingListsBox.getChildren().add(row);
            }
        }
        else {
            readingListsBox.getChildren().add(new Label("No Reading Lists :("));
        }
    }

    private Pane loadReadingListCard (ReadingList r, String owner) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unipi/dii/lsmd/paperraterapp/layout/readinglistcard.fxml"));
            pane = loader.load();
            ReadingListCardCtrl ctrl = loader.getController();
            ctrl.setReadingListCard(r, owner);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void clickOnBackIcon (MouseEvent mouseEvent) {
        // Pop
        Session.getInstance().getPreviousPageUser().remove(Session.getInstance().getPreviousPageUser().size()-1 );

        if (Session.getInstance().getPreviousPagePaper().isEmpty())
            Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml", mouseEvent);
        else {
            PaperPageController ctrl = (PaperPageController) Utils.changeScene(
                    "/it/unipi/dii/lsmd/paperraterapp/layout/paperpage.fxml", mouseEvent);
            ctrl.setPaperPage(Session.getInstance().getPreviousPagePaper().remove(
                    Session.getInstance().getPreviousPagePaper().size() - 1));
        }
    }

    private void clickOnFollowBtn (MouseEvent mouseEvent) {
        String tmp = followBtn.getText();
        if (tmp.equals("Follow")) {
            neoMan.followUser(Session.getInstance().getLoggedUser().getUsername(), user.getUsername());
            followBtn.setText("Unfollow");
        }
        else {
            neoMan.unfollowUser(Session.getInstance().getLoggedUser().getUsername(), user.getUsername());
            followBtn.setText("Follow");
        }
    }

    private void clickOnFollower (MouseEvent mouseEvent) {
        System.out.println("Browse Follower");
    }

    private void clickOnFollowing (MouseEvent mouseEvent) {
        System.out.println("Browse Following");
    }

    private void clickOnEditIcon (MouseEvent mouseEvent) {
        /* Edit form */
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile Information");

        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField firstName = new TextField(Session.getInstance().getLoggedUser().getFirstName());
        firstName.setPromptText("First Name");
        TextField lastName = new TextField(Session.getInstance().getLoggedUser().getLastName());
        lastName.setPromptText("Last Name");
        TextField age = new TextField(String.valueOf(Session.getInstance().getLoggedUser().getAge()));
        age.setPromptText("Age");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        dialogPane.setContent(new VBox(8, firstName, lastName, age, password));
        Platform.runLater(firstName::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new User(Session.getInstance().getLoggedUser().getUsername(),
                        Session.getInstance().getLoggedUser().getEmail(),
                        Session.getInstance().getLoggedUser().getPassword(),
                        firstName.getText(),
                        lastName.getText(),
                        Session.getInstance().getLoggedUser().getPicture(),
                        Integer.parseInt(age.getText()),
                        Session.getInstance().getLoggedUser().getReadingLists());
            }
            return null;
        });
        Optional<User> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((User u) -> {
            mongoMan.updateUser(u);

            // Refresh Page Content
            Session.getInstance().setLoggedUser(u);
            setProfilePage(u);
        });
    }

    private void clickOnAddReadingListBtn (MouseEvent mouseEvent) {
        // create a text input dialog
        TextInputDialog td = new TextInputDialog("r_list" +
                (Session.getInstance().getLoggedUser().getReadingLists().size() + 1));
        td.setHeaderText("Insert the title of the Reading List");
        td.showAndWait();

        // Add new Reading List to DB
        boolean res = mongoMan.createReadingList(Session.getInstance().getLoggedUser().getUsername(), td.getEditor().getText());
        neoMan.createReadingList(Session.getInstance().getLoggedUser().getUsername(), td.getEditor().getText());

        if (!res) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("There is already a Reading List with this Title");
            alert.show();
            return;
        }

        res = neoMan.createReadingList(Session.getInstance().getLoggedUser().getUsername(), td.getEditor().getText());
        if (!res) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error in Adding Reading List");
            mongoMan.deleteReadingList(Session.getInstance().getLoggedUser().getUsername(), td.getEditor().getText());
            alert.show();
            return;
        }

        // Refresh Page Content
        User refreshUser = Session.getInstance().getLoggedUser();
        refreshUser.getReadingLists().add(new ReadingList(td.getEditor().getText(), new ArrayList<>()));
        Session.getInstance().setLoggedUser(refreshUser);
        setProfilePage(refreshUser);
    }
}
