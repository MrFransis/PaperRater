package it.unipi.dii.lsmd.paperraterapp.persistence;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4jManagerE {

    Driver driver;

    public Neo4jManagerE(Driver driver) {
        this.driver = driver;
    }


    /**
     * Function that add the info of a new user to GraphDB
     * @param u new User
     */
    public void addUser(User u) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("CREATE (u:User {username: $username})",
                        parameters("username", u.getUsername()));

                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that deletes a User from the GraphDB
     * @param u User to delete
     * @return True if the operation is done successfully, false otherwise
     */
    public boolean deleteUser(User u) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.username = $username DETACH DELETE u",
                        parameters("username", u.getUsername()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that adds a Like relationship between a User and a Paper
     * @param u User
     * @param p Paper
     */
    public void like(User u, Paper p) {
        try(Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User), (b:Paper) " +
                                "WHERE a.name = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) " +
                                "CREATE (a)-[r:LIKES]->(b)",
                        parameters("username", u.getUsername(),
                                "arxiv_id", p.getArxiv_id(),
                                "vixra_id", p.getVixra_id()));
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean unlike(User u, Paper p) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User{username:$username}-[r:LIKES]->" +
                                "(p:Paper{arxiv_id:$arxiv_id,vixra_id:$vixra_id}) " +
                                " DELETE r",
                        parameters("username", u.getUsername(),
                                "arxiv_id", p.getArxiv_id(),
                                "vixra_id", p.getVixra_id()));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that returns the snap of the Reading Lists followed by a User
     * @param u Logged User
     * @return a snap List of the followed Reading Lists
     */
    public List<ReadingList> getSnapsOfFollowedReadingLists(User u) {

        List<ReadingList> readingListsSnap = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction((TransactionWork<List<ReadingList>>) tx -> {
                Result result = tx.run("MATCH (u:User{name: $username})-[r:FOLLOWS]->(b:ReadingList) " +
                        "return b.username as username, b.title as title",
                        parameters("username", u.getUsername()));
                System.out.println(result.hasNext());
                while (result.hasNext()) {
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("title").asString(), null);
                    readingListsSnap.add(snap);
                }

                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return readingListsSnap;
    }

    /**
     * Function that returns a list of snaps of suggested papers for the logged user
     * Suggestions based on papers liked by followed users
     *
     * @param u Logged User
     * @param numberFirstLv how many papers suggest from first level follow
     * @param numberSecondLv how many papers suggest from second level follow
     * @return A list of snaps suggested papers
     */
    public List<Paper> getSuggestedPapers(User u, int numberFirstLv, int numberSecondLv) {
        List<Paper> papersSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (p:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS]-(me:User{name:$username}) " +
                                "RETURN p.arxiv_id AS arxiv_id, p.vixra_id AS vixra_id, p.title as title " +
                                " LIMIT $firstlevel " +
                                "UNION " +
                                "MATCH (p:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS*2..2]-(me:User{name:$username}) " +
                                "RETURN p.arxiv_id AS arxiv_id, p.vixra_id AS vixra_id, p.title as title " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstlevel", numberFirstLv, "secondLevel", numberSecondLv));
                while(result.hasNext()){
                    Record r = result.next();
                    Paper snap = new Paper(r.get("arxiv_id").asString(), r.get("vixra_id").asString(),
                                        r.get("title").asString(), null, null, null, null, null);
                    papersSnap.add(snap);
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return papersSnap;
    }
}
