package it.unipi.dii.lsmd.paperraterapp.controller;

import com.google.gson.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import it.unipi.dii.lsmd.paperraterapp.persistence.MongoDriver;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;


public class DocumentManagerT {

    MongoDriver driver = new MongoDriver();

    /**
     * Search a user by his MongoDB _id
     * @param userId
     * @return
     */
    public User searchUser(String userId) {
        MongoDatabase database = driver.openConnection();
        MongoCollection<Document> collection = database.getCollection("Users");
        Document result = collection.find((eq("_id", new ObjectId(userId)))).first();
        if (result == null) {
            System.out.println("User " + userId + " do not found.");
            return null;
        }
        User user = null;
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        user = gson.fromJson(gson.toJson(result), User.class);
        driver.closeConnection();
        return user;
    }

    public ReadingList createReadingList(String userId) {
        MongoDatabase database = driver.openConnection();
        MongoCollection<Document> collection = database.getCollection("Users");
        // search the user
        driver.closeConnection();
        return null;
    }

    // test
    public static void main(String[] args) {
        DocumentManagerT test = new DocumentManagerT();
        User user = test.searchUser("61b84622d8a93b1d8e06eea9");
        System.out.println(user.getFirstName());
    }
}
