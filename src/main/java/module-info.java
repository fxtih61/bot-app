module com.openjfx {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.kordamp.bootstrapfx.core;
  requires org.apache.poi.poi;
  requires org.apache.poi.ooxml;
  requires org.jetbrains.annotations;

  opens com.openjfx to javafx.fxml;

  exports com.openjfx;
}