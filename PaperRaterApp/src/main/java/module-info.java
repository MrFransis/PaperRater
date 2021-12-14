module it.unipi.dii.lsmd.paperraterapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;

    opens it.unipi.dii.lsmd.paperraterapp to javafx.fxml;
    exports it.unipi.dii.lsmd.paperraterapp;
    opens it.unipi.dii.lsmd.paperraterapp.controller to javafx.fxml;
    exports it.unipi.dii.lsmd.paperraterapp.controller;
}