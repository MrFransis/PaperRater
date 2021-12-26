package it.unipi.dii.lsmd.paperraterapp.controller;

import it.unipi.dii.lsmd.paperraterapp.model.Session;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDBManager;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {

    @FXML private TextField authorTf;
    @FXML private Button backBt;
    @FXML private ComboBox<String> chooseAnalytics;
    @FXML private ComboBox<String> chooseCategory;
    @FXML private ComboBox<String> chooseSuggestion;
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

    private MongoDBManager manager = new MongoDBManager(MongoDriver.getInstance().openConnection());
    private User user = Session.getInstance().getUser();

    @FXML
    void goToProfilePage(MouseEvent event) {
        //Session.getInstance().setPreviousPageVisited("/it/unipi/dii/lsmd/paperraterapp/layout/browser.fxml");
        ProfilePageController ctrl = (ProfilePageController) Utils.changeScene(
                "/it/unipi/dii/lsmd/paperraterapp/layout/profilepage.fxml", event);
        ctrl.setProfilePage(user);
    }

    @FXML
    void activeResearch(ActionEvent event) {
        searchBt.setDisable(false);
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
                categoryContainer.setVisible(false);
            }
            default -> {
                authorContainer.setVisible(false);
                dateContainer.setVisible(false);
                keywordContainer.setVisible(false);
                categoryContainer.setVisible(false);
            }
        }
    }

    @FXML
    void showAnalytics(ActionEvent event) {

    }

    @FXML
    void showBackCards(ActionEvent event) {

    }

    @FXML
    void showForwardCards(ActionEvent event) {

    }

    @FXML
    void showSuggestion(ActionEvent event) {

    }

    @FXML
    void showSummary(ActionEvent event) {

    }

    @FXML
    void startResearch(ActionEvent event) {
        switch (chooseType.getValue()) {
            case "Papers" -> {

            }
            case "Users" -> {
                if (keywordTf.getText().equals("")) {
                    errorTf.setText("You have to insert a keyword.");
                    return;
                }
                List<>
            }
            case "Reading lists" -> {

            }
        }
    }

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        loadSpecialQuery();
        hideFilterForm();
    }

    private void loadSpecialQuery () {
        // load suggestion
        List<String> suggestionList = new ArrayList<>();
        suggestionList.add("Suggested paper");
        suggestionList.add("Suggested reading list");
        suggestionList.add("Suggested user");
        ObservableList observableListSuggestion = FXCollections.observableList(suggestionList);
        chooseSuggestion.getItems().clear();
        chooseSuggestion.setItems(observableListSuggestion);

        // load analytics
        List<String> analyticsList = new ArrayList<>();
        analyticsList.add("Most active users");
        analyticsList.add("Most followed reading lists");
        analyticsList.add("Most popular Users");
        analyticsList.add("Most popular Papers");
        analyticsList.add("Most popular Categories");
        ObservableList observableListAnalytics = FXCollections.observableList(analyticsList);
        chooseAnalytics.getItems().clear();
        chooseAnalytics.setItems(observableListAnalytics);

        // load summary
        List<String> summaryList = new ArrayList<>();
        summaryList.add("Categories with more articles published");
        summaryList.add("Categories with more likes");
        summaryList.add("Categories with more comments");
        summaryList.add("Papers with more likes");
        summaryList.add("Papers with more comments");
        ObservableList observableListSummary = FXCollections.observableList(summaryList);
        chooseSummary.getItems().clear();
        chooseSummary.setItems(observableListSummary);

        // load type
        List<String> typeList = new ArrayList<>();
        typeList.add("Papers");
        typeList.add("Users");
        typeList.add("Reading lists");
        ObservableList observableListType = FXCollections.observableList(typeList);
        chooseType.getItems().clear();
        chooseType.setItems(observableListType);

        // load categories
        List<String> categoriesList = manager.getCategories();
        ObservableList observableListCategories = FXCollections.observableList(categoriesList);
        chooseCategory.getItems().clear();
        chooseCategory.setItems(observableListCategories);
    }

    private void hideFilterForm() {
        authorContainer.setVisible(false);
        dateContainer.setVisible(false);
        keywordContainer.setVisible(false);
        categoryContainer.setVisible(false);
    }

    private void showError(String text) {
        errorTf.setText(text);
    }

    private void hideError() {
        errorTf.setText("");
    }

}