package it.unipi.dii.lsmd.paperraterapp.persistence;


import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.function.Consumer;

public class MongoDriver implements DatabaseDriver {
    private static MongoDriver instance;

    private static MongoClient client = null;
    private static MongoDatabase database = null;
    private MongoCollection collection;

    /**
     * Method that inits the MongoClient and choose the correct database
     */
    @Override
    public boolean openConnection() {
        try
        {
            String string = "mongodb://localhost:27017";

            ConnectionString connectionString = new ConnectionString(string);

            client = MongoClients.create(connectionString);

            database = client.getDatabase("PaperRater");
            System.out.println("Connected to MongoDB ...");
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
    @Override
    public void closeConnection() {
        if (client == null) {
            System.out.println("Already disconnected ...");
            return;
        }
        client.close();
        database = null;
        System.out.println("Disconnected to MongoDB ...");
    }

    /**
     * Method used to select collection
     */
    public void chooseCollection(String name)
    {
        collection = database.getCollection(name);
    }

}
