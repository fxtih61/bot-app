package com.openjfx;

import com.openjfx.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

  /*
   * Get the screen width and height to set the scene size to full screen
   */
  double SCENE_WIDTH = Screen.getPrimary().getBounds().getWidth();
  double SCENE_HEIGHT = Screen.getPrimary().getBounds().getHeight();
  private static Stage primaryStage;

  @Override
  public void init() {
    DatabaseConfig.initializeDatabase();
  }

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
    primaryStage.setFullScreen(true);
    primaryStage.setMaximized(true);
    primaryStage.setTitle("Berufsorientierungstag-Programm");
    primaryStage.show();
  }

  public void showMainScene() throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/views/main.fxml"));
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    String css = this.getClass().getResource("/styles/styles.css").toExternalForm();
    scene.getStylesheets().add(css);
    primaryStage.setScene(scene);
    primaryStage.setFullScreen(true);
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void stop() {
    DatabaseConfig.closeDataSource();
  }
}