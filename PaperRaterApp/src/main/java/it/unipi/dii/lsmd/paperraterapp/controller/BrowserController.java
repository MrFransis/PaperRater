package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class BrowserController implements Initializable {

    @FXML private TextField authorTf;
    @FXML private Button backBt;
    @FXML private ComboBox<String> chooseAnalytics;
    @FXML private ComboBox<String> chooseCategory;
    @FXML public ComboBox<String> chooseSuggestion;
    @FXML private ComboBox<String> chooseSummary;
    @FXML private ComboBox<String> chooseType;
    @FXML private Button forwardBt;
    @FXML private DatePicker fromDate;
    @FXML private TextField keywordTf;
    @FXML private Label profileTf;
    @FXML private Button searchBt;
    @FXML private DatePicker toDate;
    @FXML private HBox authorContainer;
    @FXML private HBox categoryContainer;
    @FXML private HBox keywordContainer;
    @FXML private HBox dateContainer;
    @FXML private Label errorTf;
    @FXML private GridPane cardsGrid;
    @FXML private Label logoutLabel;
    @FXML private CheckBox followsCheckBox;
    @FXML private HBox followsContainer;

    private MongoDBManager mongoManager;
    private Neo4jManager neo4jManager;
    private User user;
    private int page;

    @FXML
    void goToProfilePage(MouseEvent event) {
        //Session.getInstance().setPreviousPageVisited("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml");
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", event);
        ctrl.setProfilePage(user);
    }

    @FXML
    void activeResearch() {
        searchBt.setDisable(false);
        backBt.setDisable(true);
        forwardBt.setDisable(true);
        followsCheckBox.setSelected(false);
        switch (chooseType.getValue()) {
            case "Papers" -> {
                authorContainer.setVisible(true);
                dateContainer.setVisible(true);
                keywordContainer.setVisible(true);
                categoryContainer.setVisible(true);
            }
            case "Users", "Reading lists" -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(true);
                followsContainer.setVisible(true);
                categoryContainer.setVisible(false);
            }
            default -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(false);
                categoryContainer.setVisible(false);
                followsContainer.setVisible(false);
            }
        }
    }

    @FXML
    void showAnalytics(ActionEvent event) {

    }

    @FXML
    void showSuggestion(ActionEvent event) {

    }

    @FXML
    void showSummary(ActionEvent event) {

    }

    @FXML
    void startResearch() {
        forwardBt.setDisable(false);
        backBt.setDisable(true);
        page = 0;
        handleResearch();
    }

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        mongoManager = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neo4jManager = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        user = Session.getInstance().getLoggedUser();
        loadComboBox();
        hideFilterForm();
        forwardBt.setOnMouseClicked(mouseEvent -> goForward());
        backBt.setOnMouseClicked(mouseEvent -> goBack());
        logoutLabel.setOnMouseClicked(mouseEvent -> logout(mouseEvent));
    }

    private Pane loadUsersCard (User user) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/usercard.fxml"));
            pane = loader.load();
            UserCardCtrl ctrl = loader.getController();
            ctrl.setParameters(user);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadPapersCard (Paper paper) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/papercard.fxml"));
            pane = loader.load();
            PaperCardCtrl ctrl = loader.getController();
            ctrl.setPaperCard(paper, false, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadReadingListsCard (ReadingList readingList, String owner) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/readinglistcard.fxml"));
            pane = loader.load();
            ReadingListCardCtrl ctrl = loader.getController();
            ctrl.setReadingListCard(readingList, owner);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void loadComboBox () {
        // load suggestion
        List<String> suggestionList = new ArrayList<>();
        suggestionList.add("Suggested paper");
        suggestionList.add("Suggested reading list");
        suggestionList.add("Suggested user");
        suggestionList.add("Browse Follower");
        suggestionList.add("Browse Following User");
        suggestionList.add("Browse Following Reading Lists");
        ObservableList<String> observableListSuggestion = FXCollections.observableList(suggestionList);
        chooseSuggestion.getItems().clear();
        chooseSuggestion.setItems(observableListSuggestion);

        // load analytics
        List<String> analyticsList = new ArrayList<>();
        analyticsList.add("Most active users");
        analyticsList.add("Most followed reading lists");
        analyticsList.add("Most popular Users");
        analyticsList.add("Most popular Papers");
        analyticsList.add("Most popular Categories");
        ObservableList<String> observableListAnalytics = FXCollections.observableList(analyticsList);
        chooseAnalytics.getItems().clear();
        chooseAnalytics.setItems(observableListAnalytics);

        // load summary
        List<String> summaryList = new ArrayList<>();
        summaryList.add("Categories with more articles published");
        summaryList.add("Categories with more likes");
        summaryList.add("Categories with more comments");
        summaryList.add("Papers with more likes");
        summaryList.add("Papers with more comments");
        ObservableList<String> observableListSummary = FXCollections.observableList(summaryList);
        chooseSummary.getItems().clear();
        chooseSummary.setItems(observableListSummary);

        // load type
        List<String> typeList = new ArrayList<>();
        typeList.add("Papers");
        typeList.add("Users");
        typeList.add("Reading lists");
        if (user.getType() == 1)
            typeList.add("Moderate comments");
        ObservableList<String> observableListType = FXCollections.observableList(typeList);
        chooseType.getItems().clear();
        chooseType.setItems(observableListType);

        // load categories
        List<String> categoriesList = mongoManager.getCategories();
        categoriesList.add(0, "Select category");
        ObservableList<String> observableListCategories = FXCollections.observableList(categoriesList);
        chooseCategory.getItems().clear();
        chooseCategory.setItems(observableListCategories);
    }

    private void hideFilterForm() {
        authorContainer.setVisible(false);
        dateContainer.setVisible(false);
        keywordContainer.setVisible(false);
        categoryContainer.setVisible(false);
        followsContainer.setVisible(false);
    }

    private void fillUsers(String keyword) {
        // set new layout
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,40));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(25);
        cardsGrid.getColumnConstraints().add(constraints);
        // load users
        List<User> usersList = null;
        if(followsCheckBox.isSelected())
            usersList = neo4jManager.getSnapsOfFollowedUserByKeyword(user, keyword, 3, 3*page);
        else
            usersList = mongoManager.getUsersByKeyword(keyword, page);
        if (usersList.size() != 8)
            forwardBt.setDisable(true);
        int row = 0;
        int col = 0;
        for (User u : usersList) {
            Pane card = loadUsersCard(u);
            cardsGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void fillPapers(String title, String author, String start_date, String end_date, String category) {
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(40);
        cardsGrid.setPadding(new Insets(30,40,30,100));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
        // load papers
        List<Paper> papersList = mongoManager.searchPapersByParameters(title, author, start_date, end_date, category,
                3*page, 3);
        System.out.println(papersList.size());
        if (papersList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Paper p : papersList) {
            Pane card = loadPapersCard(p);
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillReadingLists(String keyword) {
        // clean old settings
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,160));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
        // load papers
        List<Pair<String, ReadingList>> readingLists = null;
        if (followsCheckBox.isSelected())
            readingLists = neo4jManager.getSnapsOfFollowedReadingListsByKeyword(keyword, user, 3*page, 3);
        else
            readingLists = mongoManager.getReadingListByKeywords(keyword, 3*page, 3);
        if (readingLists.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        System.out.println(readingLists.size());
        for (Pair<String, ReadingList> cardInfo : readingLists) {
            Pane card = loadReadingListsCard(cardInfo.getValue(), cardInfo.getKey());
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void handleResearch() {
        cardsGrid.getChildren().clear();
        cardsGrid.getColumnConstraints().clear();
        switch (chooseType.getValue()) {
            case "Papers" -> {
                // check the form values
                errorTf.setText("");
                if (keywordTf.getText().equals("") && toDate.getValue() == null && fromDate.getValue() == null &&
                        (chooseCategory.getValue() == null || chooseCategory.getValue().equals("Select category"))
                        && authorTf.getText().equals("")) {
                    errorTf.setText("You have to set some filters.");
                    forwardBt.setDisable(true);
                    return;
                }
                if (toDate.getValue() != null && fromDate.getValue() != null &&
                        toDate.getValue().isBefore(fromDate.getValue())) {
                    errorTf.setText("The From date have to be before the To date.");
                    forwardBt.setDisable(true);
                    return;
                }
                String category = "";
                String startDate = "";
                String endDate = "";
                if (chooseCategory.getValue() != null && !chooseCategory.getValue().equals("Select category"))
                    category = chooseCategory.getValue();
                if (toDate.getValue() != null)
                    endDate = toDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (fromDate.getValue() != null)
                    startDate = fromDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // handle cards display
                fillPapers(keywordTf.getText(), authorTf.getText(), startDate, endDate, category);
            }
            case "Users" -> {
                // form control
                errorTf.setText("");
                if (keywordTf.getText().equals("") && !followsCheckBox.isSelected()) {
                    errorTf.setText("You have to specify an option.");
                    return;
                }
                fillUsers(keywordTf.getText());
            }
            case "Reading lists" -> {
                // form control
                errorTf.setText("");
                if (keywordTf.getText().equals("") && !followsCheckBox.isSelected()) {
                    errorTf.setText("You have to specify an option.");
                    return;
                }
                fillReadingLists(keywordTf.getText());
            }
        }
    }

    private void goForward () {
        page++;
        backBt.setDisable(false);
        handleResearch();
    }

    private void goBack () {
        page--;
        if (page == 0)
            backBt.setDisable(true);
        forwardBt.setDisable(false);
        handleResearch();
    }

    private void logout(MouseEvent event) {
        Session.resetInstance();
        Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml", event);
    }
}
