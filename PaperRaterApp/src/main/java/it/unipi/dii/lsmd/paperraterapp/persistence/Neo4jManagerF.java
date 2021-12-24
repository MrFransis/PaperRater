package it.unipi.dii.lsmd.paperraterapp.persistence;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import javafx.util.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4jManagerF {

    private final Driver driver;

    public Neo4jManagerF(String uri, String user, String password){
        driver = GraphDatabase.driver(uri, AuthTokens.basic( user, password));
    }

    public void closeConnection()
    {
        if (driver != null)
            driver.close();
    }

    //Create paper (il secondo id è una stringa vuota non un NaN)
    /**
     * Add a paper in Neo4j databases
     * @param p Object paper that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addPaper(Paper p){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MERGE (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id})", parameters("arxiv_id", p.getArxiv_id(), "vixra_id", p.getVixra_id()));
            return null; });
            return true;
        }catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }

    //Create HasCommented
    /**
     * It creates the relation user-[:HAS_COMMENTED]->paper
     * @param user  Username of the target user
     * @param id  Id of the target paper
     */
    public void hasCommented(String user, String id){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) where u.name = $user " +
                        "MATCH (p:Paper) where (p.arxiv_id = $id OR p.vixra_id =$id) " +
                        "MERGE (u)-[:HAS_COMMENTED]->(p)", parameters("user", user, "id", id));
                return null; });
        }catch (Exception ex) {
            System.err.println(ex);
        }
    }

    //Delete paper
    /**
     * Delete a paper in Neo4j databases
     * @param p Object paper that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean deletePaper(Paper p){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id}) DELETE p", parameters("arxiv_id", p.getArxiv_id(), "vixra_id", p.getVixra_id()));
                return null; });
            return true;
        }catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }

    //Delete HasCommented
    /**
     * Delete the relation user-[:HAS_COMMENTED]->paper
     * @param user  Username of the target user
     * @param id  Id of the target paper
     */
    public void deleteHasCommented(String user, String id){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {name:$user})-[r:HAS_COMMENTED]->(p:Paper) WHERE p.arxiv_id = $id OR p.vixra_id = $id " +
                        "DELETE r", parameters("user", user, "id", id));
                return null; });
        }catch (Exception ex) {
            System.err.println(ex);
        }
    }

    //Get suggestions about Reading Lists
    /**
     * Shows the suggested readingLists for the given user
     * @param user username of the user
     * @param numberFirstLv how many readingLists suggest from first level follow
     * @param numberSecondLv how many readingLists suggest from second level follow
     */
    public List<Pair<String, String>> suggestedReadingLists(String user, int numberFirstLv, int numberSecondLv){
        List<Pair<String, String>> readingLists = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS]-(me:User{name:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT target.username AS username, target.title AS title, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "RETURN username, title, numFollower + follow AS followers ORDER BY followers DESC LIMIT $firstLevel" +
                                "UNION " +
                                 "MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS*2..2]-(me:User{name:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT target.username AS username, target.title AS title, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "RETURN username, title, numFollower + follow AS followers ORDER BY followers DESC LIMIT $secondLevel",
                        parameters("username", user, "firstlevel", numberFirstLv, "secondLevel", numberSecondLv));

                while(result.hasNext()){
                    Record r = result.next();
                    String title = r.get("title").asString();
                    String username = r.get("username").asString();
                    readingLists.add(new Pair<>(title, username));
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return readingLists;
    }

    //Most liked Papers
    /**
     * This function return the most liked papers
     * @param number Number or results
     * @return List of Papers
     */
    public List<Pair<String, Integer>> searchMostLikePapers (int number)
    {
        List<Pair<String, Integer>> topPapers = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[l:LIKES]->(p:Paper) RETURN p.arxiv_id AS Arxiv_id, p.vixra_id AS Vixra_id, " +
                                "COUNT(l) AS like_count ORDER BY like_count DESC " +
                                "LIMIT $limit",
                        parameters( "limit", number));

                while(result.hasNext()){
                    Record r = result.next();
                    String arxiv = r.get("Arxiv_id").asString();
                    String vixra = r.get("Vixra_id").asString();
                    int likes = r.get("like_count").asInt();
                    if(!arxiv.isEmpty())
                        topPapers.add(new Pair<>(arxiv, likes));
                    else
                        topPapers.add(new Pair<>(vixra, likes));
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return topPapers;
    }

    //Users that commented the highest number of papers
    /**
     * This function return Users that commented the highest number of papers
     * @param number Number or results
     * @return List of Users
     */
    public List<Pair<String, Integer>> serachMostActiveUser (int number)
    {
        List<Pair<String, Integer>> topUsers = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User)-[l:HAS_COMMENTED]->(:Paper) RETURN u.name AS username, " +
                                "COUNT(l) AS comments_count ORDER BY comments_count DESC " +
                                "LIMIT $limit",
                        parameters( "limit", number));

                while(result.hasNext()){
                    Record r = result.next();
                    String arxiv = r.get("username").asString();
                    int comments = r.get("comments_count").asInt();
                    topUsers.add(new Pair<>(arxiv, comments));
                }
                System.out.println(topUsers);
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return topUsers;
    }

}
