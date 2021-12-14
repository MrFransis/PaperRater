package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.google.gson.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;


public class DocumentManagerT {

    private MongoDatabase database;
    private MongoCollection usersCollection;
    private MongoCollection papersCollection;

    public DocumentManagerT (MongoClient client) {
        database = client.getDatabase("PaperRater");
        usersCollection = database.getCollection("Users");
        papersCollection = database.getCollection("Papers");
    }
    /**
     * Search a user by his username
     * @param username
     * @return
     */
    public User searchUser(String username) {
        Document result = (Document) usersCollection.find((eq("username", username))).first();
        if (result == null) {
            System.out.println("User " + username + " do not found.");
            return null;
        }
        User user = null;
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        user = gson.fromJson(gson.toJson(result), User.class);
        return user;
    }

    /**
     * Add a new empty reading called "title" list at the user identify by username
     * @param username
     * @param title
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
     * @param username
     * @param title
     * @return
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

    public List<Paper> searchPaperByAuthor(String author) {
        return null;
    }
    // test
    public static void main(String[] args) {
        // open connection
        MongoDriver driver = MongoDriver.getInstance();
        DocumentManagerT managerT = new DocumentManagerT(driver.openConnection());
        // test 1 ok
        //User user = managerT.searchUser("crazymouse258");
        //System.out.println(user.toString());

        // test 2 ok
        //managerT.createReadingList("crazymouse258", "new_readList1");

        // test 3
        managerT.deleteReadingList("crazymouse258", "new_readList1");
        // close connection
        driver.closeConnection();
    }
}
