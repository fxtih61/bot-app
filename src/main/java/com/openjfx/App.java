package com.openjfx;

import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

  @Override
  public void start(Stage stage) {
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + "\nRunning on Java " + javaVersion);
    // Scene scene = new Scene(new StackPane(label), 640, 480);

    Panel panel = new Panel("This is the title");
    panel.getStyleClass().add("panel-primary");
    BorderPane content = new BorderPane();
    content.setPadding(new Insets(20));
    Button button = new Button("Hello BootstrapFX");
    button.getStyleClass().setAll("btn", "btn-danger");
    content.setCenter(button);
    panel.setBody(content);

    Scene scene = new Scene(panel);
    scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

    stage.setTitle("Hello BootstrapFX");
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}