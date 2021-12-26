package it.unipi.dii.lsmd.paperraterapp.model;

import java.util.List;

public class Session {

    private static Session instance = null;
    private User loggedUser;
    private User previousPageUser;
    private ReadingList previousReadingList;
    private Paper previousPaper;
    private List<String> pageHistory;

    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    private Session () {}

    public void setUser(User u) {
        instance.loggedUser = u;
    }

    public User getUser() {
        return loggedUser;
    }

    public void setPreviousPageUser(User u) {
        instance.loggedUser = u;
    }

    public User getPreviousPageUser() {
        return loggedUser;
    }

    /*
    public void setPreviousPageVisited(String lastPageVisited) {
        this.lastPageVisited = lastPageVisited;
    }

    public String getLastPageVisited() {
        return lastPageVisited;
    }

     */
}
