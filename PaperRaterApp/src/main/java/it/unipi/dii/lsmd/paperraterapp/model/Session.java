package it.unipi.dii.lsmd.paperraterapp.model;

public class Session {
    private static Session instance = null;
    private User user;

    public static Session getInstance() {
        if(instance==null)
            instance = new Session();
        return instance;
    }

    private Session () {}

    public static void setUser(User u) {
        instance.user = u;
    }
    public User getUser() {
        return user;
    }
}
