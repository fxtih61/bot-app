package com.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

  private int SCENE_WIDTH = 1200;
  private int SCENE_HEIGHT = 800;
  private static Stage primaryStage;

  @Override
  public void start(Stage stage) throws Exception {

    primaryStage = stage;
    showWelcomeScene();
  }

  public void showWelcomeScene() throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/views/welcome.fxml"));
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    String css = this.getClass().getResource("/styles/welcome.css").toExternalForm();
    scene.getStylesheets().add(css);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void showMainScene() throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/views/main.fxml"));
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    String css = this.getClass().getResource("/styles/styles.css").toExternalForm();
    scene.getStylesheets().add(css);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}