package it.unipi.dii.lsmd.paperraterapp.persistence;

import org.neo4j.driver.*;

public class Neo4jDriverE {
    private static Neo4jDriverE instance;

    private Driver driver;

    private Neo4jDriverE() {

    }

    public static Neo4jDriverE getInstance() {
        if (instance == null)
            //instance = new MongoDriver(Utils.readConfigurationParameters());
            instance = new Neo4jDriverE();
        return instance;
    }

    public Driver openConnection() {
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
