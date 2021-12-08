module it.unipi.dii.lsmd.paperraterapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens it.unipi.dii.lsmd.paperraterapp to javafx.fxml;
    exports it.unipi.dii.lsmd.paperraterapp;
}