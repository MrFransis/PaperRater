package it.unipi.dii.lsmd.paperraterapp.persistence;

import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.HashMap;

import static org.neo4j.driver.Values.parameters;

public class Neo4jManagerT {

    private Driver driver;

    public Neo4jManagerT (Driver driver) {
        this.driver = driver;
    }

    /**
     * Add a new reading list if it does not exist and the relationship of FOLLOWS between
     * the owner and  the new relationship
     *
     * @param title name of the reading list
     * @param username owner of the reading list
     */
    public void createReadingList (final String title, final String username) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (owner:User {name: $username}) " +
                        "MERGE (r:ReadingList {title: $title, username: $username}) " +
                                "MERGE (owner)-[p:OWNS]->(r) " +
                                "ON CREATE SET p.date = datetime()"
                        , parameters("title", title, "username", username));
                return null;
            });
        }
    }

    /**
     * Add a FOLLOWS relationship between a User and a reading list
     * @param title title of the reading list
     * @param owner owner of the reading list
     * @param username follower
     */
    public void followReadingList (final String title, final String owner, final String username) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {name: $username}), (r:ReadingList {title: $title, username: $owner}) " +
                        "MERGE (u)-[p:FOLLOWS]->(r) " +
                                "ON CREATE SET p.date = datetime()",
                        parameters("username", username, "title", title, "owner", owner));
                return null;
            });
        }
    }

    /**
     * Add a FOLLOWS relationship between two Users
     * @param username follower
     * @param target followed
     */
    public void followUser (final String username, final String target) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {name: $username}), (t:User {name: $target}) " +
                                "MERGE (u)-[p:FOLLOWS]->(t) " +
                                "ON CREATE SET p.date = datetime()",
                        parameters("username", username, "target", target));
                return null;
            });
        }
    }

    /**
     * Remove a reading list and all its relationships
     * @param title title of the reading list
     * @param username owner of the reading list
     */
    public void deleteReadingList (final String title, final String username) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (r:ReadingList {title: $title, username: $username}) " +
                                "DETACH DELETE r",
                        parameters("title", title, "username", username));
                return null;
            });
        }
    }

    /**
     * remove the relationship of FOLLOWS between a User and a reading list
     * @param title title of the reading list
     * @param owner owner of the reading list
     * @param username follower
     */
    public void unfollowReadingList (final String title, final String owner, final String username) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {name: $username})-[r:FOLLOWS]->(:ReadingList {title: $title, username: $owner}) " +
                                "DELETE r",
                        parameters("username", username, "title", title, "owner", owner));
                return null;
            });
        }
    }

    /**
     * remove the relationship of FOLLOWS between two Users
     * @param target followed
     * @param username follower
     */
    public void unfollowUser (final String username, final String target) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {name: $username})-[r:FOLLOWS]->(:User {name: $target}) " +
                                "DELETE r",
                        parameters("username", username, "target", target));
                return null;
            });
        }
    }

    /**
     * Return a hashmap with the suggested user ranked by their popularity
     * @param username who need suggestions
     * @param num num of suggestions
     * @return pair (name, numFollower)
     */
    public HashMap<String, Integer> suggestUser(final String username, final int num) {
        HashMap<String, Integer> suggestion;
        try (Session session = driver.session()) {
            suggestion = session.readTransaction((TransactionWork<HashMap<String, Integer>>) tx -> {
                Result result = tx.run("MATCH (:User {name: $username})-[:FOLLOWS]->(:User)-[:FOLLOWS]-(target:User), " +
                        "(target)<-[r:FOLLOWS]-() RETURN DISTINCT target.name as Name, " +
                        "count(DISTINCT r) as numFollower ORDER BY numFollower DESC LIMIT $num",
                        parameters("username", username, "num", num));
                HashMap<String, Integer> suggestionsList = new HashMap<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    suggestionsList.put(r.get("Name").asString(), r.get("numFollower").asInt());
                }
                return suggestionsList;
            });
        }
        return suggestion;
    }

    /**
     * Return a hashmap with the most popular user
     * @param num num of rank
     * @return pair (name, numFollower)
     */
    public HashMap<String, Integer> mostPopularUsers (final int num) {
        HashMap<String, Integer> rank;
        try (Session session = driver.session()) {
            rank = session.readTransaction((TransactionWork<HashMap<String, Integer>>) tx -> {
                Result result = tx.run("MATCH (target:User)<-[r:FOLLOWS]-(:User) RETURN DISTINCT target.name as Name, " +
                                "count(DISTINCT r) as numFollower ORDER BY numFollower DESC LIMIT $num",
                        parameters("num", num));
                HashMap<String, Integer> popularUser = new HashMap<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    popularUser.put(r.get("Name").asString(), r.get("numFollower").asInt());
                }
                return popularUser;
            });
        }
        return rank;
    }



    //test
    public static void main(String[] args) {
        Neo4jDriverE driver = Neo4jDriverE.getInstance();
        Neo4jManagerT manager = new Neo4jManagerT(driver.openConnection());
        // test 1 ok
        //manager.createReadingList("time1_readList", "brownelephant518");

        // test 2 ok
        //manager.followReadingList("r_list0", "yellowbird227", "brownelephant518");
        //manager.followUser("brownelephant518", "yellowbird227");

        // test 3 ok
        //manager.deleteReadingList("time1_readList", "brownelephant518");
        //manager.unfollowReadingList("r_list3", "happywolf304", "brownelephant518");
        //manager.unfollowUser("brownelephant518", "yellowbird227");

        // test 4
        //HashMap<String, Integer> test = manager.suggestUser("brownelephant518", 10);
        //HashMap<String, Integer> test = manager.mostPopularUsers(10);
        //System.out.println(test);
        // close connection
        driver.closeConnection();
    }
}
