package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;

import com.mongodb.client.model.Projections;
import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.conversions.Bson;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static com.mongodb.client.model.Aggregates.*;
import it.unipi.dii.lsmd.paperraterapp.config.ConfigurationParameters;
import it.unipi.dii.lsmd.paperraterapp.utils.Utils;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.Comment;

/**
 * This class is used to communicate with MongoDB
 */
public class MongoDBDriverE {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection collection;
    private String firstIp;
    private int firstPort;
    private String username;
    private String password;
    private String dbName;

    public MongoDBDriverE(){
    };

    /**
     * Consumer function that prints the document in json format
     */
    private Consumer<Document> printDocuments = doc -> {
        System.out.println(doc.toJson());
    };

    private MongoDBDriverE (ConfigurationParameters configurationParameters)
    {
        this.firstIp = configurationParameters.getMongoFirstIp();
        this.firstPort = configurationParameters.getMongoFirstPort();
        this.username = configurationParameters.getMongoUsername();
        this.password = configurationParameters.getMongoPassword();
        this.dbName = configurationParameters.getMongoDbName();
    }

    /**
     * Method that inits the MongoClient and choose the correct database
     */
    public MongoClient initConnection() {
        MongoClient mongoClient = null;
        try
        {
            String string = "mongodb://localhost:27017";
            //if (!username.equals("")) // if there are access rules
            //{
            //    string += username + ":" + password + "@";
            //}
            //string += firstIp + ":" + firstPort;

            ConnectionString connectionString = new ConnectionString(string);

            mongoClient = MongoClients.create(connectionString);

        }
        catch (Exception ex)
        {
            System.out.println("MongoDB is not available");
            return null;
        }
        return mongoClient;
    }

    /**
     * Method used to close the connection
     */
    public void closeConnection() {
        if (mongoClient != null)
            mongoClient.close();
    }
}

