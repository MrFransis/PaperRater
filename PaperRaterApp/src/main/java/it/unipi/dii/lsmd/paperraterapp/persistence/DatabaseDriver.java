package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.mongodb.client.MongoClient;

/**
 * Interface with the method that every database driver must implement
 */
public interface DatabaseDriver {
    public MongoClient openConnection();
    public void closeConnection();
}
