package it.unipi.dii.lsmd.paperraterapp.model;

import java.util.Date;
import java.util.List;

public class ReadingList {
    private String username;
    private String name;
    private List<Paper> papers;

    public ReadingList(String username, String name, List<Paper> papers) {
        this.username = username;
        this.name = name;
        this.papers = papers;
    }

    public String getUsername() { return username; }

    public String getName() {
        return name;
    }

    public List<Paper> getPapers() {
        return papers;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name ) {
        this.name = name;
    }

    public void setPaper(List<Paper> papers) { this.papers = papers; }

    public void addPapers(Paper paper) {this.papers.add(paper);}

    public void deletePaper(Paper paper) {this.papers.remove(paper);}

    @Override
    public String toString() {
        return "ReadingList{" + "papers='" + papers + '\'' + '}';
    }
}

