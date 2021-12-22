module it.unipi.dii.lsmd.paperraterapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires java.xml;
    requires xstream;
    requires com.google.gson;
    requires org.neo4j.driver;

    opens it.unipi.dii.lsmd.paperraterapp to javafx.fxml;
    exports it.unipi.dii.lsmd.paperraterapp;
    opens it.unipi.dii.lsmd.paperraterapp.controller to javafx.fxml;
    exports it.unipi.dii.lsmd.paperraterapp.controller;
    exports it.unipi.dii.lsmd.paperraterapp.persistence;
    opens it.unipi.dii.lsmd.paperraterapp.persistence to javafx.fxml;
    opens it.unipi.dii.lsmd.paperraterapp.model to com.google.gson;
}