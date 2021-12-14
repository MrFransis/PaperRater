package it.unipi.dii.lsmd.paperraterapp.config;

/**
 * Class used to store the configuration parameters retrieved from the config.xml
 * There is no need to modify this value, so there are only the getters methods
 */
public class ConfigurationParameters {
    private String mongoFirstIp;
    private int mongoFirstPort;
    private String mongoDbName;

    public int getMongoFirstPort() {
        return mongoFirstPort;
    }

    public String getMongoFirstIp() {
        return mongoFirstIp;
    }

    public String getMongoDbName() {
        return mongoDbName;
    }

}
