package it.unipi.dii.lsmd.paperraterapp.model;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private static Session instance = null;
    private User loggedUser;
    private List<User> previousPageUser;
    private List<ReadingList> previousPageReadingList;
    private List<Paper> previousPagePaper;


    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    private Session () {
        previousPageUser = new ArrayList<>();
        previousPageReadingList = new ArrayList<>();
        previousPagePaper = new ArrayList<>();
    }

    public void setLoggedUser(User u) {
        instance.loggedUser = u;
    }

    public User getLoggedUser() {
        return loggedUser;
    }


    public List<User> getPreviousPageUser() {
        return previousPageUser;
    }

    public List<ReadingList> getPreviousPageReadingList() {
        return previousPageReadingList;
    }

    public List<Paper> getPreviousPagePaper() {
        return previousPagePaper;
    }

}
