module com.rplbo.app.rpl_wedmateassistant {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    // SQLite embedded (tidak perlu server)
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Ikonli icon pack
    requires org.kordamp.ikonli.fontawesome5;

    // Package utama
    opens com.rplbo.app.rpl_wedmateassistant to javafx.fxml;
    exports com.rplbo.app.rpl_wedmateassistant;

    // Package model
    opens com.rplbo.app.rpl_wedmateassistant.model to javafx.fxml;
    exports com.rplbo.app.rpl_wedmateassistant.model;

    // Package controller
    opens com.rplbo.app.rpl_wedmateassistant.controller to javafx.fxml;
    exports com.rplbo.app.rpl_wedmateassistant.controller;

    // Package engine
    exports com.rplbo.app.rpl_wedmateassistant.engine;

    // Package database
    exports com.rplbo.app.rpl_wedmateassistant.database;
}