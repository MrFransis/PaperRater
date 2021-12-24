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
public class MongoDriver {
    private static MongoDriver instance;

    private MongoClient client = null;
    private MongoDatabase database = null;
    public String firstIp;
    public int firstPort;
    public String dbName;

    /*
    private MongoDriver(ConfigurationParameters configurationParameters){
        this.firstIp = configurationParameters.mongoFirstIp;
        this.firstPort = configurationParameters.mongoFirstPort;
        this.dbName = configurationParameters.mongoDbName;
    };
    */

    public static MongoDriver getInstance() {
        if (instance == null)
            //instance = new MongoDriver(Utils.readConfigurationParameters());
            instance = new MongoDriver();
        return instance;
    }

    /**
     * Method that inits the MongoClient and choose the correct database
     */
    public MongoClient openConnection() {
        if (client != null)
            return client;

        try
        {
            //String string = "mongodb://172.16.4.66:27020,172.16.4.67:27020,172.16.4.66:27020";
            String string = "mongodb://localhost:27017";
            ConnectionString connectionString = new ConnectionString(string);

            client = MongoClients.create(connectionString);

            database = client.getDatabase("PaperRater");
            System.out.println("Connected to MongoDB ...");
            return client;
        }
        catch (Exception ex)
        {
            System.out.println("MongoDB is not available");
            return null;
        }
    }

    /**
     * Method used to close the connection
     */
    public void closeConnection() {
        if (client != null)
            System.out.println("Connection closed ...");
            client.close();
    }
}

