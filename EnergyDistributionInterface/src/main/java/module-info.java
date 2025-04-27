module com.example.energydistributioninterface {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires org.json;

    opens com.example.energydistributioninterface to javafx.fxml;
    exports com.example.energydistributioninterface;
}