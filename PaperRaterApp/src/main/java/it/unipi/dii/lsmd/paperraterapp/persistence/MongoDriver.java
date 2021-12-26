package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * This class is used to communicate with MongoDB
 */
public class MongoDriver {
    private static MongoDriver instance;

    private MongoClient client = null;
    private MongoDatabase database = null;
    private CodecRegistry pojoCodecRegistry;
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

            pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            MongoClientSettings clientSettings = MongoClientSettings.builder()
                                                                    .applyConnectionString(connectionString)
                                                                    .codecRegistry(pojoCodecRegistry)
                                                                    .build();

            client = MongoClients.create(clientSettings);

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

