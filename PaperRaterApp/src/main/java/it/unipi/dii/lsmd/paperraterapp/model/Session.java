package it.unipi.dii.lsmd.paperraterapp.model;

public class Session {
    private static Session instance = null;
    private User user;
    private User previousPageUser;

    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    private Session () {}

    public void setUser(User u) {
        instance.user = u;
    }
    public User getUser() {
        return user;
    }
    public void setPreviousPageUser(User u) {
        instance.user = u;
    }
    public User getPreviousPageUser() {
        return user;
    }
}
