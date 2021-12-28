package it.unipi.dii.lsmd.paperraterapp.model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private static Session instance = null;
    private User loggedUser;
    private List<User> previousPageUsers;
    private List<Pair<String, ReadingList>> previousPageReadingLists;
    private List<Paper> previousPagePapers;



    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private Session () {
        previousPageUsers = new ArrayList<>();
        previousPageReadingLists = new ArrayList<>();
        previousPagePapers = new ArrayList<>();
    }

    public void setLoggedUser(User u) {
        instance.loggedUser = u;
    }

    public User getLoggedUser() {
        return loggedUser;
    }


    public List<User> getPreviousPageUsers() {
        return previousPageUsers;
    }

    public List<Pair<String, ReadingList>> getPreviousPageReadingList() {
        return previousPageReadingLists;
    }

    public List<Paper> getPreviousPagePaper() {
        return previousPagePapers;
    }

}
