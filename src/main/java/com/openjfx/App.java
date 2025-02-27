package com.openjfx;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
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
   * Displays the main scene of the application.
   *
   * This method loads application settings from a properties file, including
   * the theme (dark mode or light mode) and the language for localization.
   * It then sets up the main scene with the appropriate styles and language
   * resources, and displays it in full screen and maximized mode.
   *
   * @throws Exception if there is an error loading the FXML file or other
   *                   resources.
   * 
   * @author Fatih Tolip
   */
  public void showMainScene() throws Exception {

    Properties props = new Properties();
    boolean isDarkMode;
    try (FileInputStream in = new FileInputStream("settings.properties")) {
      props.load(in);
      isDarkMode = Boolean.parseBoolean(props.getProperty("darkMode", "true"));
    } catch (IOException e) {
      isDarkMode = true;
    }
    String lang = props.getProperty("language", "de");
    Locale locale = new Locale(lang);
    ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);

    Parent root = FXMLLoader.load(App.class.getResource("/views/main.fxml"), bundle);
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

    // Das richtige Theme setzen
    String theme = isDarkMode ? "/styles/styles.css" : "/styles/light-styles.css";
    scene.getStylesheets().add(this.getClass().getResource(theme).toExternalForm());

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