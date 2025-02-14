module com.openjfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens com.openjfx to javafx.fxml;
    opens com.openjfx.controllers to javafx.fxml;

    exports com.openjfx;
}