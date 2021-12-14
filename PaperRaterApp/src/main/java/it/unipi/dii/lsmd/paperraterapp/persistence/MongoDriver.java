package it.unipi.dii.lsmd.paperraterapp.persistence;


import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.function.Consumer;

public class MongoDriver {
    private static MongoDriver instance;

    private static MongoClient client = null;
    private static MongoDatabase database = null;
    private MongoCollection collection;
/*
    public static MongoDatabase openConnection() {
        if (client == null) {
            client = MongoClients.create("mongodb://127.0.0.1:27017/?" +
                    "directConnection=true&serverSelectionTimeoutMS=2000");
            database = client.getDatabase("PaperRater");
            System.out.println("Connected to MongoDB ...");
            return database;
        }
        System.out.println("Already connected ...");
        return database;
    }
*/
    public void chooseCollection(String name)
    {
        collection = database.getCollection(name);
    }

    /**
     * Consumer function that prints the document in json format
     */
    private Consumer<Document> printDocuments = doc -> {
        System.out.println(doc.toJson());
    };

    /**
     * Method that inits the MongoClient and choose the correct database
     */
    public boolean openConnection() {
        try
        {
            String string = "mongodb://localhost:27017";
            //if (!username.equals("")) // if there are access rules
            //{
            //    string += username + ":" + password + "@";
            //}
            //string += firstIp + ":" + firstPort;

            ConnectionString connectionString = new ConnectionString(string);

            client = MongoClients.create(connectionString);

            database = client.getDatabase("PaperRater");
            //chooseCollection("Papers");
        }
        catch (Exception ex)
        {
            System.out.println("MongoDB is not available");
            return false;
        }
        return true;
    }

    /**
     * Method used to close the connection
     */
    public static void closeConnection() {
        if (client == null) {
            System.out.println("Already disconnected ...");
            return;
        }
        client.close();
        database = null;
        System.out.println("Disconnected to MongoDB ...");
    }
}
