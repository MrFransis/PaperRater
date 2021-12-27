package it.unipi.dii.lsmd.paperraterapp.persistence;

import org.neo4j.driver.*;

public class Neo4jDriver {
    private static Neo4jDriver instance;

    private Driver driver;

    private Neo4jDriver() {

    }

    public static Neo4jDriver getInstance() {
        if (instance == null)
            //instance = new MongoDriver(Utils.readConfigurationParameters());
            instance = new Neo4jDriver();
        return instance;
    }

    public Driver openConnection() {
        if (driver != null)
            return driver;
        try {
            driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic( "neo4j", "root"));
            driver.verifyConnectivity();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return driver;
    }

    public void closeConnection() {
        if (driver != null)
            driver.close();
    }
}
