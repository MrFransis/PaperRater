package it.unipi.dii.lsmd.paperraterapp.persistence;


import com.mongodb.client.*;

public class MongoDriver {

    private static MongoClient client = null;
    private static MongoDatabase database = null;

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
