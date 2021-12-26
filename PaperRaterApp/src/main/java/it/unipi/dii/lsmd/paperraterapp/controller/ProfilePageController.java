package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManagerE;
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

import java.util.Iterator;
import java.util.Optional;

public class ProfilePageController {
    private User user;
    private MongoDBManager mongoMan;
    private Neo4jManagerE neoMan;

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
       // neoMan = new Neo4jManagerE(Neo4jDriverE.getInstance().openConnection());
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
        Session.getInstance().setPreviousPageUser(user);

        username.setText(user.getUsername());
        email.setText(user.getEmail());
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        nFollower.setText("100"); // neoMan.getNumberOfFollowers(u.getUsername())
        nFollowing.setText("2000"); // neoMan.getNumberOfFollowing(u.getUsername())

        if (!user.getPicture().isEmpty())
            profileImg.setImage(new Image(user.getPicture()));

        /*if (man.userAFollowsUserB(Session.user, user))
            followBtn.setText("Unfollow");*/


        if (user.getUsername().equals(Session.getInstance().getUser().getUsername())) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/readinglistcard.fxml"));
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
        System.out.println("Back");
        //Utils.changeScene(Session.getInstance().getLastPageVisited(), mouseEvent);
    }

    private void clickOnFollowBtn (MouseEvent mouseEvent) {
        String tmp = followBtn.getText();
        if (tmp.equals("Follow")) {
            //neoMan.follow(Session.getInstance().getUser().getUsername(), user.getUsername());
            System.out.println("Follow");
            followBtn.setText("Unfollow");
        }
        else {
            //neoMan.unfollow(Session.getUsername(), user.getUsername());
            System.out.println("Unfollow");
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
        TextField firstName = new TextField(Session.getInstance().getUser().getFirstName());
        firstName.setPromptText("First Name");
        TextField lastName = new TextField(Session.getInstance().getUser().getLastName());
        lastName.setPromptText("Last Name");
        TextField age = new TextField(String.valueOf(Session.getInstance().getUser().getAge()));
        age.setPromptText("Age");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        dialogPane.setContent(new VBox(8, firstName, lastName, age, password));
        Platform.runLater(firstName::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new User(Session.getInstance().getUser().getUsername(),
                        Session.getInstance().getUser().getEmail(),
                        Session.getInstance().getUser().getPassword(),
                        firstName.getText(),
                        lastName.getText(),
                        Session.getInstance().getUser().getPicture(),
                        Integer.parseInt(age.getText()),
                        Session.getInstance().getUser().getReadingLists());
            }
            return null;
        });
        Optional<User> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((User u) -> {
            mongoMan.updateUser(u);

            // Refresh Page Content
            Session.getInstance().setUser(u);
            setProfilePage(u);
        });
    }

    private void clickOnAddReadingListBtn (MouseEvent mouseEvent) {
        // create a text input dialog
        TextInputDialog td = new TextInputDialog("r_list" +
                (Session.getInstance().getUser().getReadingLists().size() + 1));
        td.setHeaderText("Insert the title of the Reading List");
        td.showAndWait();

        // Add new Reading List to DB
        boolean res = mongoMan.createReadingList(Session.getInstance().getUser().getUsername(), td.getEditor().getText());
        //neoMan.createReadingList

        if (!res) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("There is already a Reading List with this Title");
            alert.show();
            return;
        }

        // refresh page
        User refreshUser = mongoMan.getUserByUsername(Session.getInstance().getUser().getUsername());
        Session.getInstance().setUser(refreshUser);
        setProfilePage(refreshUser);
    }
}
