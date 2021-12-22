package it.unipi.dii.lsmd.paperraterapp.utils;

import it.unipi.dii.lsmd.paperraterapp.config.ConfigurationParameters;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.thoughtworks.xstream.XStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
    /**
     * This function is used to read the config.xml file
     * @return  ConfigurationParameters instance
     */
    public static ConfigurationParameters readConfigurationParameters ()
    {
        if (validConfigurationParameters())
        {
            XStream xs = new XStream();

            String text = null;
            try {
                text = new String(Files.readAllBytes(Paths.get(Utils.class.getResource("/it/unipi/dii/lsmd/paperraterapp/config/config.xml").toURI())));
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }

            // Problem
            return (ConfigurationParameters) xs.fromXML(text);
        }
        else
        {
            //showErrorAlert("Problem with the configuration file!");
            //try {
            //    sleep(2000);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //System.exit(1); //If i can't read the configuration file I can't continue with the program
        }
        return null;
    }

    /**
     * This function is used to validate the config.xml with the config.xsd
     * @return  true if config.xml is well formatted, otherwise false
     */
    private static boolean validConfigurationParameters()
    {
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document document = documentBuilder.parse(String.valueOf(Utils.class.getResource("/it/unipi/dii/lsmd/paperraterapp/config/config.xml")));
            Schema schema = schemaFactory.newSchema(new StreamSource(String.valueOf(Utils.class.getResource("/it/unipi/dii/lsmd/paperraterapp/config/config.xsd"))));
            schema.newValidator().validate(new DOMSource(document));
        }
        catch (Exception e)
        {
            if (e instanceof SAXException)
                System.out.println("Validation Error: " + e.getMessage());
            else
                System.out.println(e.getMessage());

            return false;
        }
        return true;
    }

    public static Object changeScene (String fileName, Event event) {
        Scene scene = null;
        FXMLLoader loader = null;
        try {
            loader=new FXMLLoader(Utils.class.getResource(fileName));
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
