package it.unipi.dii.lsmd.paperraterapp.persistence;

import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import javafx.util.Pair;

import java.util.List;
public class Main {
    public static void main(String[] args) {
        Neo4jManager neoMan = new Neo4jManager(Neo4jDriver.getInstance().openConnection());
        User u = new User("happytiger362", null, null, null, null, null, 0, null, 0);
        List<Pair<String, ReadingList>> list = neoMan.getSnapsOfSuggestedReadingLists(u,2,2);
        for (Pair<String, ReadingList> t : list) {
            System.out.println(t.getKey());
            System.out.println(t.getValue().getTitle());
        }
        return;
    }
}