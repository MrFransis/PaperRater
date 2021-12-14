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
     * Search a user by his username
     * @param username
     * @return
     */
    public User searchUser(String username) {
        driver.openConnection();
        MongoCollection<Document> collection = driver.chooseCollection("User");
        Document result = collection.find((eq("username", username))).first();
        if (result == null) {
            System.out.println("User " + username + " do not found.");
            return null;
        }
        User user = null;
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        user = gson.fromJson(gson.toJson(result), User.class);
        driver.closeConnection();
        return user;
    }

    public ReadingList createReadingList(String username, String name) {
        MongoDatabase database = driver.openConnection();
        MongoCollection<Document> collection = database.getCollection("Users");
        User user = searchUser(username);
        driver.closeConnection();
        return null;
    }

    // test
    public static void main(String[] args) {
        DocumentManagerT test = new DocumentManagerT();
        // test 1
        User user = test.searchUser("crazymouse258");
        System.out.println(user.getFirstName());
    }
}
