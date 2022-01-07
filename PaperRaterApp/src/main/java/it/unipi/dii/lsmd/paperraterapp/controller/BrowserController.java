package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.*;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jDriver;
import it.unipi.dii.lsmd.paperraterapp.persistence.Neo4jManager;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.Locale;
import java.util.ResourceBundle;


public class BrowserController implements Initializable {

    @FXML private TextField authorTf;
    @FXML private Button backBt;
    @FXML private ComboBox<String> chooseCategory;
    @FXML private ComboBox<String> chooseType;
    @FXML private Button forwardBt;
    @FXML private Button specialSearchBt;
    @FXML private DatePicker fromDate;
    @FXML private TextField keywordTf;
    @FXML private Button searchBt;
    @FXML private DatePicker toDate;
    @FXML private HBox authorContainer;
    @FXML private HBox categoryContainer;
    @FXML private HBox keywordContainer;
    @FXML private HBox dateContainer;
    @FXML private Label errorTf;
    @FXML private GridPane cardsGrid;
    @FXML private CheckBox followsCheckBox;
    @FXML private HBox followsContainer;
    @FXML private HBox paramContainer;
    @FXML private HBox timeRangeContainer;
    @FXML private ComboBox<String> chooseTarget;
    @FXML private ComboBox<String> chooseQuery;
    @FXML private ComboBox<String> chooseTimeRange;

    private MongoDBManager mongoManager;
    private Neo4jManager neo4jManager;
    private User user;
    private int page;
    private int special;

    @FXML
    void goToProfilePage(MouseEvent event) {
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", event);
        if (ctrl != null) {
            ctrl.setProfilePage(user);
        }
    }

    @FXML
    private void logout(MouseEvent event) {
        Session.resetInstance();
        Utils.changeScene("/it/unipi/dii/lsmd/paperraterapp/layout/login.fxml", event);
    }

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        mongoManager = new MongoDBManager(MongoDriver.getInstance().openConnection());
        neo4jManager = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        user = Session.getInstance().getLoggedUser();
        special = 0;
        loadComboBox();
        hideFilterForm();
        forwardBt.setOnMouseClicked(mouseEvent -> goForward());
        backBt.setOnMouseClicked(mouseEvent -> goBack());
    }

    // -------------------------------------------- NORMAL RESEARCH --------------------------------------------
    @FXML
    void switchForm() {
        searchBt.setDisable(false);
        backBt.setDisable(true);
        forwardBt.setDisable(true);
        followsCheckBox.setSelected(false);
        special = 0;
        switch (chooseType.getValue()) {
            case "Papers" -> {
                authorContainer.setVisible(true);
                dateContainer.setVisible(true);
                keywordContainer.setVisible(true);
                categoryContainer.setVisible(true);
                followsContainer.setVisible(false);
            }
            case "Users", "Reading lists" -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(true);
                followsContainer.setVisible(true);
                categoryContainer.setVisible(false);
            }
            case "Moderate comments" -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(true);
                keywordContainer.setVisible(false);
                followsContainer.setVisible(false);
                categoryContainer.setVisible(false);
            }
            case "Search moderator" -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(true);
                followsContainer.setVisible(false);
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
    void startResearch() {
        forwardBt.setDisable(false);
        backBt.setDisable(true);
        page = 0;
        special = 0;
        specialSearchBt.setDisable(true);
        handleResearch();
    }

    private void handleResearch() {
        special = 0;
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
                // load papers
                List<Paper> papersList = mongoManager.searchPapersByParameters(keywordTf.getText(), authorTf.getText(),
                        startDate, endDate, category, 3*page, 3);
                fillPapers(papersList);
            }
            case "Users" -> {
                // form control
                errorTf.setText("");
                if (keywordTf.getText().equals("") && !followsCheckBox.isSelected()) {
                    errorTf.setText("You have to specify an option.");
                    return;
                }
                List<User> usersList;
                // check if you need follows
                if(followsCheckBox.isSelected())
                    usersList = neo4jManager.getSnapsOfFollowedUserByKeyword(user, keywordTf.getText(), 8, 8*page);
                else
                    usersList = mongoManager.getUsersByKeyword(keywordTf.getText(), false, page);
                fillUsers(usersList);
            }
            case "Reading lists" -> {
                // form control
                errorTf.setText("");
                if (keywordTf.getText().equals("") && !followsCheckBox.isSelected()) {
                    errorTf.setText("You have to specify an option.");
                    return;
                }
                // load papers
                List<Pair<String, ReadingList>> readingLists;
                if (followsCheckBox.isSelected())
                    readingLists = neo4jManager.getSnapsOfFollowedReadingListsByKeyword(keywordTf.getText(), user, 4*page, 4);
                else
                    readingLists = mongoManager.getReadingListByKeyword(keywordTf.getText(), 4*page, 4);
                fillReadingLists(readingLists);
            }
            case "Moderate comments" -> {
                // form control
                errorTf.setText("");
                if (toDate.getValue() != null && fromDate.getValue() != null &&
                        toDate.getValue().isBefore(fromDate.getValue())) {
                    errorTf.setText("The From date have to be before the To date.");
                    forwardBt.setDisable(true);
                    return;
                }

                String startDate = "";
                String endDate = "";

                if (toDate.getValue() != null)
                    endDate = toDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (fromDate.getValue() != null)
                    startDate = fromDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                fillComments(startDate, endDate);
            }
            case "Search moderator" -> {
                List<User> usersList = mongoManager.getUsersByKeyword(keywordTf.getText(), true, page);
                fillUsers(usersList);
            }
            case "Bad Users" -> {
                List<User> usersList = mongoManager.getBadUsers(8*page, 8);
                fillUsers(usersList);
            }
        }
    }

    // -------------------------------------------- SPECIAL RESEARCH --------------------------------------------


    @FXML
    void showOption() {
        page = 0;
        special = 1;
        cleanGrid();
        paramContainer.setVisible(false);
        timeRangeContainer.setVisible(false);
        //chooseTarget.getItems().clear();
        chooseTarget.setPromptText("Select option");
        forwardBt.setDisable(true);
        backBt.setDisable(true);
        specialSearchBt.setDisable(true);
        special = 0;
        switch (chooseQuery.getValue()) {
            case "Suggestion" -> {
                List<String> typeList1 = new ArrayList<>();
                typeList1.add("Papers");
                typeList1.add("Users");
                typeList1.add("Reading lists");
                ObservableList<String> observableListType1 = FXCollections.observableList(typeList1);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType1);
                chooseTarget.setPromptText("Select option");
                paramContainer.setVisible(true);
                timeRangeContainer.setVisible(false);
                chooseTarget.setDisable(false);
            }
            case "Analytics" -> {
                List<String> typeList2 = new ArrayList<>();
                typeList2.add("Most commented papers");
                typeList2.add("Most liked papers");
                typeList2.add("Most followed users");
                typeList2.add("Most followed reading lists");
                typeList2.add("Most versatile users");
                ObservableList<String> observableListType2 = FXCollections.observableList(typeList2);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType2);
                chooseTarget.setPromptText("Select option");
                paramContainer.setVisible(true);
                chooseTarget.setDisable(false);
            }
            case "Summary" -> {
                List<String> typeList = new ArrayList<>();
                typeList.add("Categories by likes");
                typeList.add("Categories by comments");
                typeList.add("Categories by number of papers published");
                ObservableList<String> observableListType = FXCollections.observableList(typeList);
                //chooseTarget.getItems().clear();
                chooseTarget.setItems(observableListType);
                paramContainer.setVisible(true);
                timeRangeContainer.setVisible(true);
                chooseTarget.setDisable(false);
            }
        }
    }

    @FXML
    void selectedOption() {
        if (chooseTarget.getValue() != null) {
            switch (chooseTarget.getValue()) {
                case "Most versatile users", "Most followed users", "Most followed reading lists" -> {
                    timeRangeContainer.setVisible(false);
                    specialSearchBt.setDisable(false);
                }
                default -> timeRangeContainer.setVisible(true);
            }
        }
    }

    @FXML
    void periodSelected() {
        specialSearchBt.setDisable(false);
    }

    @FXML
    void startSpecialSearch() {
        cleanGrid();
        forwardBt.setDisable(false);
        searchBt.setDisable(true);
        errorTf.setText("");
        special = 1;
        switch (chooseQuery.getValue()) {
            case "Suggestion" -> {
                if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                }
                switch (chooseTarget.getValue()) {
                    case "Users" -> {
                        List<User> suggestedUser = neo4jManager.getSnapsOfSuggestedUsers(user, 4,
                                4, 4*page, 4*page);
                        fillUsers(suggestedUser);
                    }
                    case "Papers" -> {
                        List<Paper> suggestedPaper = neo4jManager.getSnapsOfSuggestedPapers(user, 2,
                                1, 2*page, page);
                        fillPapers(suggestedPaper);
                    }
                    case "Reading lists" -> {
                        List<Pair<String, ReadingList>> suggestedReadingLists =
                                neo4jManager.getSnapsOfSuggestedReadingLists(user, 2, 2,
                                        2*page, 2*page);
                        fillReadingLists(suggestedReadingLists);
                    }
                }
            }
            case "Summary" -> {
                forwardBt.setDisable(true);
                if (chooseTimeRange.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                } else if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                } else {
                    String period = chooseTimeRange.getValue().toLowerCase(Locale.ROOT);
                    List<Pair<String, Integer>> list;
                    switch (chooseTarget.getValue()) {
                        case "Categories by likes" -> {
                            list = neo4jManager.getCategoriesSummaryByLikes(period);
                            categoriesTableView(list, "Likes");
                        }
                        case "Categories by comments" -> {
                            list = mongoManager.getCategoriesSummaryByComments(period);
                            categoriesTableView(list, "Comments");
                        }
                        case "Categories by number of papers published" -> {
                            list = mongoManager.getCategoriesSummaryByNumberOfPaperPublished(period);
                            categoriesTableView(list, "Papers published");
                        }
                    }
                }
            }
            case "Analytics" -> {
                if (chooseTarget.getValue() == null) {
                    errorTf.setText("You have to select a valid option.");
                    return;
                }
                if (!timeRangeContainer.isVisible()) {
                    switch (chooseTarget.getValue()) {
                        case "Most versatile users" -> {
                            List<Pair<User, Integer>> users = mongoManager.getTopVersatileUsers(8*page, 8);
                            fillUsers(users, "Categories saved");
                        }
                        case "Most followed users" -> {
                            List<Pair<User, Integer>> users = neo4jManager.getMostFollowedUsers(8*page, 8);
                            fillUsers(users, "Follower");
                        }
                        case "Most followed reading lists" -> {
                            List<Pair<Pair<String, ReadingList>, Integer>> lists = neo4jManager.getMostFollowedReadingLists(4*page, 4);
                            fillReadingLists(lists, "Follower");
                        }
                    }
                } else {
                    String period = chooseTimeRange.getValue().toLowerCase(Locale.ROOT);
                    switch (chooseTarget.getValue()) {
                        case "Most commented papers" -> {
                            List<Pair<Paper, Integer>> papers = mongoManager.getMostCommentedPapers(period, 3*page, 3);
                            fillPapers(papers, "Comments");
                        }
                        case "Most liked papers" -> {
                            List<Pair<Paper, Integer>> papers = neo4jManager.getMostLikedPapers(period, 3*page, 3);
                            fillPapers(papers, "Likes");
                        }
                    }
                }
            }
        }
    }
    // -------------------------------------------- UTILS --------------------------------------------

    private Pane loadUsersCard (User user, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/usercard.fxml"));
            pane = loader.load();
            UserCardCtrl ctrl = loader.getController();
            ctrl.setParameters(user, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadPapersCard (Paper paper, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/papercard.fxml"));
            pane = loader.load();
            PaperCardCtrl ctrl = loader.getController();
            ctrl.setPaperCard(paper, false, null, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadReadingListsCard (ReadingList readingList, String owner, String analytics, int value) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/readinglistcard.fxml"));
            pane = loader.load();
            ReadingListCardCtrl ctrl = loader.getController();
            ctrl.setReadingListCard(readingList, owner, analytics, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Pane loadCommentCard (Comment comment, Paper paper) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unipi/dii/lsmd/paperraterapp/layout/commentcard.fxml"));
            pane = loader.load();
            CommentCtrl ctrl = loader.getController();
            ctrl.setCommentCard(comment, user, paper, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return pane;
    }

    private void loadComboBox () {
        // load suggestion
        List<String> suggestionList = new ArrayList<>();
        suggestionList.add("Suggestion");
        suggestionList.add("Analytics");
        suggestionList.add("Summary");
        ObservableList<String> observableListSuggestion = FXCollections.observableList(suggestionList);
        chooseQuery.getItems().clear();
        chooseQuery.setItems(observableListSuggestion);

        List<String> timeRange = new ArrayList<>();
        timeRange.add("Week");
        timeRange.add("Month");
        timeRange.add("All");
        ObservableList<String> observableListTimeRange = FXCollections.observableList(timeRange);
        chooseTimeRange.getItems().clear();
        chooseTimeRange.setItems(observableListTimeRange);

        // load type
        List<String> typeList = new ArrayList<>();
        typeList.add("Papers");
        typeList.add("Users");
        typeList.add("Reading lists");
        if (user.getType() > 0)
            typeList.add("Moderate comments");
        if (user.getType() == 2) {
            typeList.add("Search moderator");
            typeList.add("Bad Users");
        }
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

    private void fillUsers(List<User> usersList) {
        // set new layout
        setGridUsers();
        if (usersList.size() != 8)
            forwardBt.setDisable(true);
        int row = 0;
        int col = 0;
        for (User u : usersList) {
            Pane card = loadUsersCard(u, null, 0);
            cardsGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void fillUsers(List<Pair<User, Integer>> usersList, String label) {
        // set new layout
        setGridUsers();
        if (usersList.size() != 8)
            forwardBt.setDisable(true);
        int row = 0;
        int col = 0;
        for (Pair<User, Integer> u : usersList) {
            Pane card = loadUsersCard(u.getKey(), label, u.getValue());
            cardsGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void fillPapers(List<Paper> papersList) {
        setGridPapers();
        if (papersList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Paper p : papersList) {
            Pane card = loadPapersCard(p, null, 0);
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillPapers(List<Pair<Paper, Integer>> papersList, String label) {
        setGridPapers();
        if (papersList.size() != 3)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<Paper, Integer> p : papersList) {
            Pane card = loadPapersCard(p.getKey(), label, p.getValue());
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillReadingLists(List<Pair<String, ReadingList>> readingLists) {
        setGridReadingList();
        if (readingLists.size() != 4)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<String, ReadingList> cardInfo : readingLists) {
            Pane card = loadReadingListsCard(cardInfo.getValue(), cardInfo.getKey(), null, 0);
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillReadingLists(List<Pair<Pair<String, ReadingList>, Integer>> readingLists, String label) {
        setGridReadingList();
        if (readingLists.size() != 4)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<Pair<String, ReadingList>, Integer> cardInfo : readingLists) {
            Pane card = loadReadingListsCard(cardInfo.getKey().getValue(), cardInfo.getKey().getKey(),
                    label, cardInfo.getValue());
            cardsGrid.add(card, 0, row);
            row++;
        }
    }

    private void fillComments(String start_date, String end_date) {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setHgap(5);
        cardsGrid.setPadding(new Insets(30,40,30,120));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);

        // load papers
        List<Pair<Paper, Comment>> commentsList = mongoManager.searchLastComments(start_date, end_date, 4*page, 4);
        if (commentsList.size() != 4)
            forwardBt.setDisable(true);
        int row = 0;
        for (Pair<Paper, Comment> cardInfo : commentsList) {
            Pane card = loadCommentCard(cardInfo.getValue(), cardInfo.getKey());
            cardsGrid.add(card, 0, row);
            row++;
            if(row == 4)
                row = 0;
        }
    }

    private void goForward () {
        page++;
        backBt.setDisable(false);
        switch (special) {
            case 0 -> handleResearch();
            default -> startSpecialSearch();
        }
    }

    private void goBack () {
        page--;
        if (page <= 0) {
            page = 0;
            backBt.setDisable(true);
        }
        forwardBt.setDisable(false);
        switch (special) {
            case 0 -> handleResearch();
            default -> startSpecialSearch();
        }
    }

    private void setGridUsers() {
        cleanGrid();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,40));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(25);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void setGridPapers() {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(25);
        cardsGrid.setPadding(new Insets(30,40,30,100));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void setGridReadingList() {
        cleanGrid();
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,160));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);
    }

    private void cleanGrid() {
        cardsGrid.getColumnConstraints().clear();
        while (cardsGrid.getChildren().size() > 0) {
            cardsGrid.getChildren().remove(0);
        }
    }

    private void categoriesTableView(List<Pair<String, Integer>> list, String value) {
        cleanGrid();
        TableView table = new TableView();
        TableColumn firstColumn = new TableColumn("Categories");
        firstColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn secondColumn = new TableColumn(value);
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        table.getColumns().addAll(firstColumn, secondColumn);

        for(Pair<String, Integer> row : list) {
            table.getItems().add(row);
        }
        // append the table to a scrollable (???)
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(30,40,30,160));
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100);
        cardsGrid.getColumnConstraints().add(constraints);

        cardsGrid.add(table, 0, 0);
    }

}
