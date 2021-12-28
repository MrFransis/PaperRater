package it.unipi.dii.lsmd.paperraterapp.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Utils {
    /**
     * This function is used to read the config.xml file
     * @return  ConfigurationParameters instance
     */
    public static Properties readConfigurationParameters(){
        try{
            FileInputStream fis = new FileInputStream(Utils.class.getResource("/it/unipi/dii/lsmd/paperraterapp/config/config.properties").toURI().getPath());
            Properties prop = new Properties();
            prop.load(fis);
            return prop;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }



    public static Object changeScene (String fileName, Event event) {
        Scene scene = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(Utils.class.getResource(fileName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
