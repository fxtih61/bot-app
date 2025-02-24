module com.openjfx {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.kordamp.bootstrapfx.core;
  requires org.apache.poi.poi;
  requires org.apache.poi.ooxml;
  requires org.jetbrains.annotations;
  requires com.zaxxer.hikari;
  requires java.sql;
  requires javafx.graphics;
  requires org.apache.pdfbox;
  requires com.h2database;
  requires javafx.base;
  requires java.desktop;
  opens com.openjfx.models to javafx.base;

  opens com.openjfx to javafx.fxml;
  opens com.openjfx.controllers to javafx.fxml;

  exports com.openjfx;
  opens com.openjfx.controllers.Import to javafx.fxml;
}