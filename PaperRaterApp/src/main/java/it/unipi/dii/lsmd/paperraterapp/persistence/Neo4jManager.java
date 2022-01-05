package it.unipi.dii.lsmd.paperraterapp.persistence;

import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import javafx.util.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    public List<User> getSnapsOfFollowedUserByKeyword (User u, String keyword, int limit, int skip) {
        List<User> followedUsers;
        try (Session session = driver.session()) {
            followedUsers = session.writeTransaction((TransactionWork<List<User>>) tx -> {
                Result result = tx.run("MATCH (:User {username: $username})-[:FOLLOWS]->(u:User) " +
                                "WHERE toLower(u.username) CONTAINS $keyword " +
                                "RETURN u.username AS Username, u.email AS Email ORDER BY Username DESC " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username", u.getUsername(), "keyword", keyword, "limit", limit, "skip", skip));
                List<User> followedList = new ArrayList<>();
                while(result.hasNext()) {
                    Record record = result.next();
                    User snap = new User(record.get("Username").asString(), record.get("Email").asString());
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
                    ReadingList snap = new ReadingList(r.get("Title").asString(), null);
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
                    ReadingList snap = new ReadingList(r.get("title").asString(), null);
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
     * Method that returns papers with the highest number of likes in the specified period of time.
     * @param limit
     * @return List of Papers
     */
    public List<Pair<Paper, Integer>> getMostLikedPapers(String period, int skip, int limit) {
        List<Pair<Paper, Integer>> topPapers = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[l:LIKES]->(p:Paper) " +
                                "WHERE p.published >= $start_date " +
                                "RETURN p.arxiv_id AS ArxivId, p.vixra_id AS VixraId, p.title AS Title, " +
                                "p.category AS Category, p.authors AS Authors, " +
                                "COUNT(l) AS like_count ORDER BY like_count DESC " +
                                "SKIP $skip " +
                                "LIMIT $limit",
                        parameters( "start_date", filterDate,"skip", skip, "limit", limit));

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

                    topPapers.add(new Pair(snap, r.get("like_count").asInt()));
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

    /**
     * Return a hashmap with the most popular user
     * @param num num of rank
     * @return pair (name, numFollower)
     */
    public List<Pair<User, Integer>> getMostFollowedUsers (int skip, int num) {
        List<Pair<User, Integer>> rank;
        try (Session session = driver.session()) {
            rank = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:User)<-[r:FOLLOWS]-(:User) " +
                                "RETURN DISTINCT target.username AS Username, target.email AS Email, " +
                                "COUNT(DISTINCT r) as numFollower ORDER BY numFollower DESC " +
                                "SKIP $skip " +
                                "LIMIT $num",
                        parameters("skip", skip, "num", num));
                List<Pair<User, Integer>> popularUser = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), r.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);

                    popularUser.add(new Pair(snap, r.get("numFollower").asInt()));
                }
                return popularUser;
            });
        }
        return rank;
    }

    /**
     * Return a hashmap with the most popular user
     * @param num num of rank
     * @return pair (name, numFollower)
     */
    public List<Pair<Pair<String, ReadingList>, Integer>> getMostFollowedReadingLists (int skip, final int num) {
        List<Pair<Pair<String, ReadingList>, Integer>> rank;
        try (Session session = driver.session()) {
            rank = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:ReadingList)<-[r:FOLLOWS]-(:User) " +
                                "RETURN DISTINCT target.title AS Title, target.owner AS Owner, " +
                                "COUNT(DISTINCT r) as numFollower ORDER BY numFollower DESC " +
                                "SKIP $skip " +
                                "LIMIT $num",
                        parameters("skip", skip, "num", num));
                List<Pair<Pair<String, ReadingList>, Integer>> popularReadingLists = new ArrayList<>();
                while (result.hasNext()) {
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("Title").asString(), new ArrayList<>());

                    popularReadingLists.add(new Pair(new Pair(r.get("Owner").asString(), snap)
                            , r.get("numFollower").asInt()));
                }
                return popularReadingLists;
            });
        }
        return rank;
    }

    /**
     * Function that returns a list of suggested papers snapshots for the logged user.
     * Suggestions are based on papers liked by followed users (first level) and papers liked by users
     * that are 2 FOLLOWS hops far from the logged user (second level).
     * Papers returned are ordered by the number of times they appeared in the results, so papers
     * that appear more are most likely to be similar to the interests of the logged user.
     *
     * @param u Logged User
     * @param numberFirstLv how many papers suggest from first level
     * @param numberSecondLv how many papers suggest from second level
     * @return A list of suggested papers snapshots
     */
    public List<Paper> getSnapsOfSuggestedPapers(User u, int numberFirstLv, int numberSecondLv, int skipFirstLv, int skipSecondLv) {
        List<Paper> papersSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKES]->(target)) " +
                                "RETURN target.arxiv_id AS ArxivId, target.vixra_id AS VixraId, target.title as Title, " +
                                "target.category AS Category, target.authors AS Authors, COUNT(*) AS nOccurences " +
                                "ORDER BY nOccurences DESC " +
                                "SKIP $skipFirstLevel " +
                                "LIMIT $firstlevel " +
                                "UNION " +
                                "MATCH (target:Paper)<-[r:LIKES]-(u:User)<-[:FOLLOWS*2..2]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKES]->(target)) " +
                                "RETURN target.arxiv_id AS ArxivId, target.vixra_id AS VixraId, target.title as Title, " +
                                "target.category AS Category, target.authors AS Authors, COUNT(*) AS nOccurences " +
                                "ORDER BY nOccurences DESC " +
                                "SKIP $skipSecondLevel " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstlevel", numberFirstLv, "secondLevel", numberSecondLv, "skipFirstLevel", skipFirstLv, "skipSecondLevel", skipSecondLv));
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
     * Function that returns a list of suggested users snapshots for the logged user.
     * Suggestions are based on most followed users who are 2 FOLLOWS hops far from the
     * logged user (first level);
     * The second level of suggestion returns most followed users that have likes in common with
     * the logged user.
     *
     * @param u user who need suggestions
     * @param numberFirstLv how many users suggest from first level suggestion
     * @param numberSecondLv how many users suggest from second level
     * @return A list of suggested users snapshots
     */
    public List<User> getSnapsOfSuggestedUsers(User u, int numberFirstLv, int numberSecondLv, int skipFirstLv, int skipSecondLv) {
        List<User> usersSnap = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (me:User {username: $username})-[:FOLLOWS*2..2]->(target:User), " +
                                "(target)<-[r:FOLLOWS]-() " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target)) " +
                                "RETURN DISTINCT target.username AS Username, target.email AS Email, " +
                                "COUNT(DISTINCT r) as numFollower " +
                                "ORDER BY numFollower DESC " +
                                "SKIP $skipFirstLevel " +
                                "LIMIT $firstLevel " +
                                "UNION " +
                                "MATCH (me:User {username: $username})-[:LIKES]->()<-[:LIKES]-(target:User), " +
                                "(target)<-[r:FOLLOWS]-() " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target)) " +
                                "RETURN target.username AS Username, target.email AS Email, " +
                                "COUNT(DISTINCT r) as numFollower " +
                                "ORDER BY numFollower DESC " +
                                "SKIP $skipSecondLevel " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstLevel", numberFirstLv, "secondLevel", numberSecondLv,  "skipFirstLevel", skipFirstLv, "skipSecondLevel", skipSecondLv));
                while (result.hasNext()) {
                    Record r = result.next();
                    User snap = new User(r.get("Username").asString(), r.get("Email").asString(),
                            "","","","",-1, new ArrayList<>(), 0);

                    usersSnap.add(snap);
                }
                return null;
            });
        }
        return usersSnap;
    }


    /**
     * Function that returns a list of suggested reading lists snapshots for the logged user.
     * Suggestions are based on most followed reading lists followed by followed users (first level)
     * and most followed reading lists followed by users that are 2 FOLLOWS hops far from the
     * logged user (second level).
     *
     * @param u Logged User
     * @param numberFirstLv how many readingLists suggest from first level
     * @param numberSecondLv how many readingLists suggest from second level
     * @return A list of suggested reading lists snapshots
     */
    public List<Pair<String, ReadingList>> getSnapsOfSuggestedReadingLists(User u, int numberFirstLv, int numberSecondLv, int skipFirstLv, int skipSecondLv){
        List<Pair<String, ReadingList>> readingListsSnap = new ArrayList<>();
        try(Session session = driver.session()){
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS]-(me:User{username:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT me, target, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target)) " +
                                "RETURN target.owner AS Owner, target.title AS Title, numFollower + follow AS followers " +
                                "ORDER BY followers DESC " +
                                "SKIP $skipFirstLevel " +
                                "LIMIT $firstLevel " +
                                "UNION " +
                                "MATCH (target:ReadingList)<-[f:FOLLOWS]-(u:User)<-[:FOLLOWS*2..2]-(me:User{username:$username}), " +
                                "(target)<-[r:FOLLOWS]-(n:User) WITH DISTINCT me, target, " +
                                "COUNT(DISTINCT r) AS numFollower, COUNT(DISTINCT u) AS follow " +
                                "WHERE NOT EXISTS((me)-[:FOLLOWS]->(target))" +
                                "RETURN target.owner AS Owner, target.title AS Title, numFollower + follow AS followers " +
                                "ORDER BY followers DESC " +
                                "SKIP $skipSecondLevel " +
                                "LIMIT $secondLevel",
                        parameters("username", u.getUsername(), "firstLevel", numberFirstLv, "secondLevel", numberSecondLv, "skipFirstLevel", skipFirstLv, "skipSecondLevel", skipSecondLv));

                while(result.hasNext()){
                    Record r = result.next();
                    ReadingList snap = new ReadingList(r.get("Title").asString(), null);
                    readingListsSnap.add(new Pair<>(r.get("Owner").asString(), snap));
                }

                return null;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return readingListsSnap;
    }

    /**
     * Method that returns categories with the highest number of likes in the specified period of time.
     * @param period
     * @return list of categories and the number of likes
     */
    public List<Pair<String, Integer>> getCategoriesSummaryByLikes(String period) {
        List<Pair<String, Integer>> results = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (p:Paper)<-[l:LIKES]-(:User) " +
                                "WHERE p.published >= $start_date " +
                                "RETURN count(l) AS nLikes, p.category AS Category " +
                                "ORDER BY nLikes DESC",
                        parameters( "start_date", filterDate));

                while(result.hasNext()){
                    Record r = result.next();
                    results.add(new Pair(r.get("Category").asString(), r.get("nLikes").asInt()));
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return results;
    }
}
