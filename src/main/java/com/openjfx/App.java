package com.openjfx;

import com.openjfx.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Main application class for the JavaFX application.
 */
public class App extends Application {

  /**
   * Screen width for setting the scene size to full screen.
   */
  double SCENE_WIDTH = Screen.getPrimary().getBounds().getWidth();

  /**
   * Screen height for setting the scene size to full screen.
   */
  double SCENE_HEIGHT = Screen.getPrimary().getBounds().getHeight();

  /**
   * Primary stage of the application.
   */
  private static Stage primaryStage;

  /**
   * Initializes the database configuration.
   */
  @Override
  public void init() {
    DatabaseConfig.initializeDatabase();
  }

  /**
   * Starts the application and shows the welcome scene.
   *
   * @param stage the primary stage
   * @throws Exception if the welcome scene cannot be loaded
   */
  @Override
  public void start(Stage stage) throws Exception {
    primaryStage = stage;
    showMainScene();
  }

  /**
   * Shows the main scene.
   *
   * @throws Exception if the main scene cannot be loaded
   */
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

  /**
   * Main method to launch the application.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch();
  }

  /**
   * Closes the database connection when the application stops.
   */
  @Override
  public void stop() {
    DatabaseConfig.closeDataSource();
  }
}