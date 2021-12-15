package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.google.gson.*;
import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.*;


public class DocumentManagerT {

    private final MongoDatabase database;
    private  final MongoCollection usersCollection;
    private final MongoCollection papersCollection;

    public DocumentManagerT (MongoClient client) {
        database = client.getDatabase("PaperRater");
        usersCollection = database.getCollection("Users");
        papersCollection = database.getCollection("Papers");
    }
    /**
     * Search a user by his username
     * @param username username of the user
     * @return User
     */
    public User searchUser(String username) {
        Document result = (Document) usersCollection.find((eq("username", username))).first();
        if (result == null) {
            System.out.println("User " + username + " do not found.");
            return null;
        }
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        return gson.fromJson(gson.toJson(result), User.class);
    }

    /**
     * Add a new empty reading list called "title" list at the user identify by username
     * @param username username of the user
     * @param title title of the new reading list
     * @return true if it adds the reading list, otherwise it returns false
     */
    public boolean createReadingList(String username, String title) {
        // check if there are other list with the same name
        Document document = (Document) usersCollection.find(and(eq("username", username),
                eq("reading_lists.title", title))).first();
        if (document != null) {
            System.err.println("ERROR: name already in use.");
            return false;
        }
        // create the new reading_list
        Document readingList = new Document("title", title)
                .append("papers", Arrays.asList());
        // insert the new reading_list
        usersCollection.updateOne(
                eq("username", username),
                new Document().append(
                        "$push",
                        new Document("reading_lists", readingList)
                )
        );
        System.out.println("Reading list " + title + " has been added");
        return true;
    }

    /**
     * Delete the reading list of user by specifying the title
     * @param username username of the user
     * @param title title of the reading list which i want remove
     * @return true if it removes the reading list, otherwise it returns false
     */
    public boolean deleteReadingList(String username, String title){
        Bson filter = new Document().append("username", username);
        Bson fields = new Document().append("reading_lists", new Document("title", title));
        Bson update = new Document("$pull", fields);
        UpdateResult updateResult = usersCollection.updateOne(filter, update);
        if (updateResult.getModifiedCount() == 0){
            System.err.println("ERROR: can not delete the reading list " + title);
            return false;
        } else {
            System.out.println("Reading list " + title + " has been deleted");
            return true;
        }
    }

    /**
     * Search al the papers published by an author
     * @param author name of the target author
     * @return list of Paper
     */
    public List<Paper> searchPaperByAuthor(String author) {
        // convert document in object
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeSpecialFloatingPointValues().create();
        List<Paper> results = new ArrayList<>();
        Consumer<Document> transformDocument = doc -> {
            Paper paper = gson.fromJson(gson.toJson(doc), Paper.class);
            results.add(paper);
        };
        // query mongo
        papersCollection.find(eq("authors", author)).forEach(transformDocument);
        return results;
    }

    /**
     * Braws all comments that has been written "numDays" ago
     * @param numDays how may day i want to scan
     * @return list of comments
     */
    public List<Comment> searchLastComments(int numDays) {
        // create the date
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(numDays);
        LocalDateTime startOfDay = localDateTime.toLocalDate().atStartOfDay();
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeSpecialFloatingPointValues().create();
        List<Comment> results = new ArrayList<>();
        Consumer<Document> takeComments = doc -> {
            Document docComments = (Document) doc.get("comments");
            Comment comment = gson.fromJson(gson.toJson(docComments), Comment.class);
            results.add(comment);
        };

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson sort = sort(ascending("comments.timestamp"));
        papersCollection.aggregate(Arrays.asList(unwind, filter, sort)).forEach(takeComments);

        return results;
    }

    /**
     * Browse the top categories with more comments
     * @param period (all, month, week)
     * @param top (positive integer)
     * @return HashMap with the category and the number of comments
     */
    public HashMap<String, Integer> summaryCategoriesByComments(String period, int top) {
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

        HashMap<String, Integer> results = new HashMap<>();
        Consumer<Document> rankCategories = doc ->
                results.put((String) doc.get("_id"), (Integer) doc.get("tots"));

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson group = group("$category", sum("tots", 1));
        Bson sort = sort(descending("tots"));
        Bson limit = limit(top);
        papersCollection.aggregate(Arrays.asList(unwind, filter, group, sort, limit)).forEach(rankCategories);

        return results;
    }

    /**
     * Browse the top papers with more comments
     * @param period (all, month, week)
     * @param top (positive integer)
     * @return HashMap with the title and the number of comments
     */
    public HashMap<String, Integer> summaryPapersByComments(String period, int top) {
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

        HashMap<String, Integer> results = new HashMap<>();
        Consumer<Document> rankPapers = doc ->
                results.put((String) doc.get("_id"), (Integer) doc.get("tots"));

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson group = group("$title", sum("tots", 1));
        Bson sort = sort(descending("tots"));
        Bson limit = limit(top);
        papersCollection.aggregate(Arrays.asList(unwind, filter, group, sort, limit)).forEach(rankPapers);

        return results;
    }

    /* test
    public static void main(String[] args) {
        // open connection
        MongoDriver driver = MongoDriver.getInstance();
        DocumentManagerT managerT = new DocumentManagerT(driver.openConnection());
        // test 1 ok
        //User user = managerT.searchUser("crazymouse258");
        //System.out.println(user.toString());

        // test 2 ok
        //managerT.createReadingList("crazymouse258", "new_readList1");

        // test 3 ok
        //managerT.deleteReadingList("crazymouse258", "new_readList5");

        // test 4 ok
        //List<Paper> test = managerT.searchPaperByAuthor("A. Baillod");
        //Consumer<Paper> paperConsumer = paper -> {
        //    System.out.println(paper.toString());
        //};
        //test.forEach(paperConsumer);

        // test 5 ok
        //List<Comment> test = managerT.searchLastComments(5);
        //Consumer<Comment> commentConsumer = comment -> {
        //    System.out.println(comment.toString());
        //};
        //test.forEach(commentConsumer);

        // test 6 ok (ordinare l'hashmap)
        // HashMap<String, Integer> test = managerT.summaryCategoriesByComments("week", 5);

        // test 7 ok (ordinare l'hashmap)
        //HashMap<String, Integer> test = managerT.summaryPapersByComments("week", 5);
        //test.forEach((title, tot) -> {
        //    System.out.println(title + " " + tot);
        //});
        // close connection
        driver.closeConnection();
    }

     */
}
