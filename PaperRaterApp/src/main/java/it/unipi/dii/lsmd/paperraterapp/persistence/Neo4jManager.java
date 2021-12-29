package it.unipi.dii.lsmd.paperraterapp.persistence;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import javafx.util.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.EmptyMap;
import static org.neo4j.driver.Values.parameters;

public class Neo4jManager {

    Driver driver;

    public Neo4jManager(Driver driver) {
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
     * return the number of reading lists of the user
     * @param username username of the user
     * @return number of reading list
     */
    public int getNumReadingList(final String username) {
        int numReadingList;
        try (Session session = driver.session()) {
            numReadingList = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[r:OWNS]->() " +
                        "RETURN count(r) AS numReadingList", parameters("username", username));
                return result.next().get("numReadingList").asInt();
            });
        }
        return numReadingList;
    }

    /**
     * return the number of follower of the user
     * @param username username of the user
     * @return number of followers
     */
    public int getNumFollowersUser(final String username) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})<-[r:FOLLOWS]-() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("username", username));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }

    /**
     * return the number of follower of the user
     * @param username username of the user
     * @return number of followers
     */
    public int getNumFollowingUser(final String username) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[r:FOLLOWS]->() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("username", username));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }

    public boolean userAFollowsUserB (String userA, String userB) {
        boolean res = false;
        try(Session session = driver.session()) {
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{username:$userA})-[r:FOLLOWS]->(b:User{username:$userB}) " +
                        "RETURN COUNT(*)", parameters("userA", userA, "userB", userB));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean isUserFollowingReadingList (String user, String owner, ReadingList readingList) {
        boolean res = false;
        try(Session session = driver.session()) {
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{username:$user})-[r:FOLLOWS]->(b:ReadingList{title:$title, username:$owner }) " +
                        "RETURN COUNT(*)", parameters("user", user, "title", readingList.getTitle(), "owner", owner));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Function that return true if exist a relation user-like->paper
     * @param user Username
     * @param paper Paper object
     */
    public boolean userLikePaper (String user, Paper paper){
        boolean res = false;
        try(Session session = driver.session()){
            res = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (:User{username:$user})-[r:LIKES]->(p:Paper) WHERE (p.arxiv_id = $arxiv_id AND p.vixra_id =$vixra_id) " +
                        "RETURN COUNT(*)", parameters("user", user, "arxiv_id", paper.getArxivId(), "vixra_id", paper.getVixraId()));
                Record record = r.next();
                if (record.get(0).asInt() == 0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return res;
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
                                "WHERE a.username = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) " +
                                "MERGE (a)-[r:LIKES]->(b)",
                        parameters("username", u.getUsername(),
                                "arxiv_id", p.getArxivId(),
                                "vixra_id", p.getVixraId()));
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
                tx.run("MATCH (u:User{username:$username})-[r:LIKES]->" +
                                "(p:Paper{arxiv_id:$arxiv_id,vixra_id:$vixra_id}) " +
                                " DELETE r",
                        parameters("username", u.getUsername(),
                                "arxiv_id", p.getArxivId(),
                                "vixra_id", p.getVixraId()));
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
     * It creates the relation user-[:HAS_COMMENTED]->paper
     * @param user  Username of the target user
     * @param paper  Paper Object
     */
    public void hasCommented(String user, Paper paper){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.username = $user " +
                        "MATCH (p:Paper) WHERE (p.arxiv_id = $arxiv_id AND p.vixra_id =$vixra_id) " +
                        "MERGE (u)-[:HAS_COMMENTED]->(p)", parameters("user", user, "arxiv_id", paper.getArxivId(), "vixra_id", paper.getVixraId() ));
                return null; });
        }catch (Exception ex) {
            System.err.println(ex);
        }
    }

    /**
     * Delete the relation user-[:HAS_COMMENTED]->paper
     * @param user  Username of the target user
     * @param paper Paper object
     */
    public void deleteHasCommented(String user, Paper paper){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username:$user})-[r:HAS_COMMENTED]->(p:Paper) WHERE p.arxiv_id = $arxiv_id AND p.vixra_id = $vixra_id " +
                        "DELETE r", parameters("user", user, "arxiv_id", paper.getArxivId(), "vixra_id", paper.getVixraId()));
                return null; });
        }catch (Exception ex) {
            System.err.println(ex);
        }
    }

    /**
     * Add a paper in Neo4j databases
     * @param p Object paper that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addPaper(Paper p){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MERGE (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id})", parameters("arxiv_id", p.getArxivId(), "vixra_id", p.getVixraId()));
                return null; });
            return true;
        }catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }

    /**
     * Delete a paper in Neo4j databases
     * @param p Object paper that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean deletePaper(Paper p){
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id}) DELETE p", parameters("arxiv_id", p.getArxivId(), "vixra_id", p.getVixraId()));
                return null; });
            return true;
        }catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }

    /**
     * return the number of follower of a reading list
     * @param title the title of the reading list
     * @param owner the username of the owner
     * @return number of followers
     */
    public int getNumFollowersReadingList(final String title, final String owner) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (:ReadingList {title: $title, owner: $owner})<-[r:FOLLOWS]-() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("title", title, "owner", owner));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }

    /**
     * return the number of likes of a papers
     * @param paper Paper object
     * @return number of likes
     */
    public int getNumLikes(final Paper paper) {
        int numLikes;
        try (Session session = driver.session()) {
            numLikes = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (p:Paper)<-[r:LIKES]-() WHERE p.arxiv_id = $arxiv_id AND p.vixra_id = $vixra_id " +
                        "RETURN COUNT(r) AS numLikes", parameters("arxiv_id", paper.getArxivId(), "vixra_id", paper.getVixraId()));
                return result.next().get("numLikes").asInt();
            });
        }
        return numLikes;
    }

    /**
     * return the number of comments of a papers
     * @param id ID of a paper
     * @return number of comments
     */
    public int getNumComments(final String id) {
        int numComments;
        try (Session session = driver.session()) {
            numComments = session.writeTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run("MATCH (p:Paper)<-[r:HAS_COMMENTED]-() WHERE p.arxiv_id = $id OR p.vixra_id = $id " +
                        "RETURN COUNT(r) AS numComments", parameters("id", id));
                return result.next().get("numComments").asInt();
            });
        }
        return numComments;
    }

    /**
     * Add a new reading list if it does not exist and the relationship of FOLLOWS between
     * the owner and  the new relationship
     *
     * @param title name of the reading list
     * @param owner owner of the reading list
     */
    public boolean createReadingList (final String title, final String owner) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (owner:User {username: $owner}) " +
                                "MERGE (r:ReadingList {title: $title, owner: owner}) " +
                                "MERGE (owner)-[p:OWNS]->(r) " +
                                "ON CREATE SET p.date = datetime()"
                        , parameters("title", title, "owner", owner));
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Remove a reading list and all its relationships
     * @param title title of the reading list
     * @param owner owner of the reading list
     */
    public void deleteReadingList (final String title, final String owner) {
        try (Session session = driver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (r:ReadingList {title: $title, owner: owner}) " +
                                "DETACH DELETE r",
                        parameters("title", title, "owner", owner));
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
                tx.run("MATCH (u:User {username: $username}), (t:User {username: $target}) " +
                                "MERGE (u)-[p:FOLLOWS]->(t) " +
                                "ON CREATE SET p.date = datetime()",
                        parameters("username", username, "target", target));
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
                tx.run("MATCH (:User {username: $username})-[r:FOLLOWS]->(:User {username: $target}) " +
                                "DELETE r",
                        parameters("username", username, "target", target));
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
                tx.run("MATCH (u:User {username: $username}), (r:ReadingList {title: $title, owner: $owner}) " +
                                "MERGE (u)-[p:FOLLOWS]->(r) " +
                                "ON CREATE SET p.date = datetime()",
                        parameters("username", username, "title", title, "owner", owner));
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
                tx.run("MATCH (:User {username: $username})-[r:FOLLOWS]->(:ReadingList {title: $title, owner: $owner}) " +
                                "DELETE r",
                        parameters("username", username, "title", title, "owner", owner));
                return null;
            });
        }
    }

    /**
     *
     * @param u Logged User
     * @param limit
     * @param skip
     * @return
     */
    public List<User> getSnapsOfFollowedUser (User u, int limit, int skip) {
        List<User> followedUsers;
        try (Session session = driver.session()) {
            followedUsers = session.writeTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[:FOLLOWS]->(u:User) " +
                                "RETURN u.username AS Username, u.email AS Email ORDER BY Username DESC " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username", u.getUsername(), "limit", limit, "skip", skip));
                List<User> followedList = new ArrayList<>();
                while(result.hasNext()) {
                    Record record = result.next();
                    User snap = new User(record.get("Username").asString(), record.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);
                    followedList.add(snap);
                }
                return followedList;
            });
        }
        return followedUsers;
    }

    /**
     * Function that returns the snap of the Reading Lists followed by a User
     * @param u Logged User
     * @return a snap List of the followed Reading Lists
     */
    public List<Pair<String, ReadingList>> getSnapsOfFollowedReadingLists(User u) {
        List<Pair<String, ReadingList>>  readingListsSnap = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction((TransactionWork<List<ReadingList>>) tx -> {
                Result result = tx.run("MATCH (u:User{username: $username})-[r:FOLLOWS]->(b:ReadingList) " +
                                "RETURN b.username as Username, b.title as Title",
                        parameters("username", u.getUsername()));

                while (result.hasNext()) {
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("Title").asString(), new ArrayList<>());
                    readingListsSnap.add(new Pair<>(r.get("username").asString(), snap));
                }
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return readingListsSnap;
    }

    public List<Pair<String, ReadingList>> getSnapsOfFollowedReadingListsByKeyword (final String keyword, User u,
                                                                           int skip, int limit) {
        List<Pair<String, ReadingList>> readingListsSnap = new ArrayList<>();
        try(Session session = driver.session()) {
            readingListsSnap = session.readTransaction((TransactionWork<List<Pair<String, ReadingList>>>) tx -> {
                Result result = tx.run("MATCH (u:User {username: $username})-[r:FOLLOWS]->(b:ReadingList) " +
                                "WHERE toLower(b.title) CONTAINS $keyword " +
                                "RETURN b.owner as username, b.title as title ORDER BY username SKIP $skip " +
                                "LIMIT $limit",
                        parameters("username", u.getUsername(), "keyword", keyword, "skip", skip, "limit", limit));
                List<Pair<String, ReadingList>> resultsList = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("title").asString(), new ArrayList<>());
                    resultsList.add(new Pair<>(r.get("username").asString(), snap));
                }
                return resultsList;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return readingListsSnap;
    }

    /**
     * Shows the suggested readingLists for the given user
     * @param u user
     * @param numberFirstLv how many readingLists suggest from first level follow
     * @param numberSecondLv how many readingLists suggest from second level follow
     */
    public List<Pair<String, ReadingList>> getSnapsOfSuggestedReadingLists(User u, int numberFirstLv, int numberSecondLv){
        List<Pair<String, ReadingList>> readingListsSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS]-(me:User{username:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT target.owner AS username, target.title AS title, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "RETURN username, title, numFollower + follow AS followers ORDER BY followers DESC LIMIT $firstLevel " +
                                "UNION " +
                                "MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS*2..2]-(me:User{username:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT target.owner AS username, target.title AS title, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "RETURN username, title, numFollower + follow AS followers ORDER BY followers DESC LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstLevel", numberFirstLv, "secondLevel", numberSecondLv));

                while(result.hasNext()){
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("title").asString(), new ArrayList<>());
                    readingListsSnap.add(new Pair<>(r.get("username").asString(), snap));
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return readingListsSnap;
    }

    //Most liked Papers
    /**
     * This function return the most liked papers
     * @param number Number or results
     * @return List of Papers
     */
    public List<Pair<Paper, Integer>> getSnapsOfMostLikedPapers (int number)
    {
        List<Pair<Paper, Integer>> topPapers = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[l:LIKES]->(p:Paper) " +
                                "RETURN p.arxiv_id AS ArxivId, p.vixra_id AS VixraId, p.title AS Title, " +
                                "p.category AS Category, p.authors AS Authors, " +
                                "COUNT(l) AS like_count ORDER BY like_count DESC " +
                                "LIMIT $limit",
                        parameters( "limit", number));

                while(result.hasNext()){
                    Record r = result.next();
                    List<String> authors = new ArrayList<>();

                    for (Object o : r.get("Authors").asList())
                        authors.add(o.toString());

                    Paper snap = new Paper( r.get("ArxivId").asString(),
                                            r.get("VixraId").asString(),
                                            r.get("Title").asString(),
                                            "",
                                            r.get("Category").asString(),
                                            authors, null, new ArrayList<>());

                    topPapers.add(new Pair<>(snap, r.get("like_count").asInt()));
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
    public List<Pair<User, Integer>> getSnapsOfMostActiveUsers (int number)
    {
        List<Pair<User, Integer>> topUsers = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User)-[l:HAS_COMMENTED]->(:Paper) " +
                                "RETURN u.username AS Username, u.email AS Email, " +
                                "COUNT(l) AS comments_count ORDER BY comments_count DESC " +
                                "LIMIT $limit",
                        parameters( "limit", number));

                while(result.hasNext()){
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), r.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);

                    topUsers.add(new Pair<>(snap, r.get("comments_count").asInt()));
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return topUsers;
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
    public List<Paper> getSnapsOfSuggestedPapers(User u, int numberFirstLv, int numberSecondLv) {
        List<Paper> papersSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (p:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS]-(me:User{username:$username}) " +
                                "RETURN p.arxiv_id AS ArxivId, p.vixra_id AS VixraId, p.title as Title, " +
                                "p.category AS Category, p.authors AS Authors " +
                                "LIMIT $firstlevel " +
                                "UNION " +
                                "MATCH (p:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS*2..2]-(me:User{username:$username}) " +
                                "RETURN p.arxiv_id AS ArxivId, p.vixra_id AS VixraId, p.title as Title, " +
                                "p.category AS Category, p.authors AS Authors " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstlevel", numberFirstLv, "secondLevel", numberSecondLv));
                while(result.hasNext()){
                    Record r = result.next();
                    List<String> authors = new ArrayList<>();

                    for (Object o : r.get("Authors").asList())
                        authors.add(o.toString());

                    Paper snap = new Paper( r.get("ArxivId").asString(),
                            r.get("VixraId").asString(),
                            r.get("Title").asString(),
                            "",
                            r.get("Category").asString(),
                            authors, null, new ArrayList<>());

                    papersSnap.add(snap);
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return papersSnap;
    }


    /**
     * Return a hashmap with the suggested user ranked by their popularity
     * @param u user who need suggestions
     * @param num num of suggestions
     * @return pair (name, numFollower)
     */
    public List<Pair<User, Integer>> getSnapsOfSuggestedUsers(User u, final int num) {
        List<Pair<User, Integer>> suggestion;

        try (Session session = driver.session()) {
            suggestion = session.readTransaction((TransactionWork<List<Pair<User, Integer>>>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[:FOLLOWS]->(:User)-[:FOLLOWS]-(target:User), " +
                                "(target)<-[r:FOLLOWS]-() " +
                                "RETURN DISTINCT target.username AS Username, target.email AS Email, " +
                                "count(DISTINCT r) as numFollower ORDER BY numFollower DESC LIMIT $num",
                        parameters("username", u.getUsername(), "num", num));
                List<Pair<User, Integer>> suggestionsList = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), r.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);

                    suggestionsList.add(new Pair<>(snap, r.get("numFollower").asInt()));
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
    public List<Pair<User, Integer>> getSnapsOfMostPopularUsers (final int num) {
        List<Pair<User, Integer>> rank;
        try (Session session = driver.session()) {
            rank = session.readTransaction((TransactionWork<List<Pair<User, Integer>>>) tx -> {
                Result result = tx.run("MATCH (target:User)<-[r:FOLLOWS]-(:User) " +
                                "RETURN DISTINCT target.username AS Username, target.email AS Email, " +
                                "COUNT(DISTINCT r) as numFollower ORDER BY numFollower DESC LIMIT $num",
                        parameters("num", num));
                List<Pair<User, Integer>> popularUser = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), r.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);

                    popularUser.add(new Pair<>(snap, r.get("numFollower").asInt()));
                }
                return popularUser;
            });
        }
        return rank;
    }

}
