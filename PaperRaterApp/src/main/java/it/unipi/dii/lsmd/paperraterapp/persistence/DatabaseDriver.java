package it.unipi.dii.lsmd.paperraterapp.persistence;

/**
 * Interface with the method that every database driver must implement
 */
public interface DatabaseDriver {
    public boolean openConnection();
    public void closeConnection();
}
