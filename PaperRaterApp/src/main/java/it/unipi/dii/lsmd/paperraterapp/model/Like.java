package it.unipi.dii.lsmd.paperraterapp.model;

import java.util.Date;

public class Like {
    private String username;
    private Date timestamp;

    public Like(String username, Date timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Like{" +
                "Username='" + username + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
