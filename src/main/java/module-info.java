module com.openjfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;

    opens com.openjfx to javafx.fxml;

    exports com.openjfx;
}