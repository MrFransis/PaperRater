package it.unipi.dii.lsmd.paperraterapp.persistence;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;

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
                                "MERGE (owner)-[:FOLLOWS {date: datetime()}]->(r)"
                        , parameters("title", title, "username", username));
                return null;
            });
        }
    }

    public void followReadingList (final String title, final String owner, final String username) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {name: $username}), (r:ReadingList {title: $title, username: $owner}) " +
                        "MERGE (u)-[:FOLLOWS {date: datetime()}]->(r)",
                        parameters("username", username, "title", title, "owner", owner));
                return null;
            });
        }
    }

    public void followUser (final String username, final String target) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {name: $username}), (t:User {name: $target}) " +
                                "MERGE (u)-[:FOLLOWS {date: datetime()}]->(t)",
                        parameters("username", username, "target", target));
                return null;
            });
        }
    }


    //test
    public static void main(String[] args) {
        Neo4jDriverE driver = Neo4jDriverE.getInstance();
        Neo4jManagerT manager = new Neo4jManagerT(driver.openConnection());
        // test 1 ok
        manager.createReadingList("time1_readList", "brownelephant518");

        // test 2 ok
        manager.followReadingList("r_list0", "yellowbird227", "brownelephant518");
        manager.followUser("brownelephant518", );
        // close connection
        driver.closeConnection();
    }
}
