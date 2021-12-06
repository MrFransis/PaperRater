module it.unipi.pr.paperraterapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens it.unipi.pr.paperraterapp to javafx.fxml;
    exports it.unipi.pr.paperraterapp;
    exports it.unipi.pr.paperraterapp.GUIcontroller;
    opens it.unipi.pr.paperraterapp.GUIcontroller to javafx.fxml;
}