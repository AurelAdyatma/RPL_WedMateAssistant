module com.rplbo.app.rpl_wedmateassistant {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.rplbo.app.rpl_wedmateassistant to javafx.fxml;
    exports com.rplbo.app.rpl_wedmateassistant;
}