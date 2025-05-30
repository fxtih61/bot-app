package com.openjfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.awt.Desktop;
import java.net.URI;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.FileInputStream;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.Scene;
import com.openjfx.App;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
/**
 * Controller class for handling the settings view. This class manages the H2 Console button and
 * starts the H2 server when the button is clicked.
 *
 * @author mian
 */
public class SettingController {

  @FXML
  private Button docsButton;
  @FXML
  private Button h2ConsoleButton;

  private static final int H2_PORT = 8082;
  private static final String H2_URL = "http://localhost:" + H2_PORT;
  private static final AtomicBoolean isServerStarting = new AtomicBoolean(false);
  @FXML
  private ToggleButton themeToggle;

  @FXML
  private ComboBox<String> languageComboBox;

  private Scene scene;
  private boolean isDarkMode;
  private static final String SETTINGS_FILE = "settings.properties";

  /**
   * Initializes the controller class. This method is automatically called after
   * the FXML file has been loaded.
   */
  @FXML
  public void initialize() {
    loadSettings();
    docsButton.setOnAction(event -> openDocs());
    h2ConsoleButton.setOnAction(event -> openH2Console());
    themeToggle.setSelected(isDarkMode);
    themeToggle.setText(isDarkMode ? "Dark Mode" : "Light Mode");
    themeToggle.setOnAction(event -> toggleTheme());

    languageComboBox.setOnAction(event -> {
      String selectedLanguage = languageComboBox.getValue();
      if ("Englisch".equals(selectedLanguage)) {
        changeLanguage("en");
      } else {
        changeLanguage("de");
      }
    });

    Platform.runLater(() -> {
      scene = themeToggle.getScene();
      if (scene != null) {
        applyTheme();
      } else {
        System.out.println("Scene konnte nicht aus themeToggle geholt werden.");
      }
    });
  }

  /**
   * Changes the application's language setting and updates the main scene.
   *
   * @param lang the language code to change to (e.g., "en" for English, "de" for
   *             German)
   * @author Fatih Tolip
   */
  private void changeLanguage(String lang) {
    saveSettings(isDarkMode, lang);
    try {
      App app = new App();
      app.showMainScene();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Retrieves the language setting from the properties file.
   *
   * @return the language setting as a string. If the setting is not found or an
   *         error occurs,
   *         the default language "de" is returned.
   * @author Fatih Tolip
   */
  public String getLanguage() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
      props.load(in);
      return props.getProperty("language", "de");
    } catch (IOException e) {
      e.printStackTrace();
      return "de";
    }
  }

  /**
   * Sets the scene for the controller and applies the current theme.
   *
   * @param scene the Scene object to be set
   * @author Fatih Tolip
   */
  public void setScene(Scene scene) {
    this.scene = scene;
    applyTheme();
  }

  /**
   * Toggles the application's theme between dark mode and light mode.
   * Applies the selected theme and saves the settings.
   *
   * @throws Exception if an error occurs while applying the theme or saving the
   *                   settings.
   * @author Fatih Tolip
   */
  private void toggleTheme() {
    try {
      isDarkMode = !isDarkMode;
      applyTheme();
      saveSettings(isDarkMode, languageComboBox.getValue().equals("Englisch") ? "en" : "de");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Applies the selected theme to the current scene.
   * This method runs on the JavaFX Application Thread.
   * It clears the current stylesheets and adds the appropriate stylesheet
   * based on the value of the isDarkMode flag.
   * It also updates the text of the theme toggle button to reflect the current
   * theme.
   *
   * @author Fatih Tolip
   */
  private void applyTheme() {
    Platform.runLater(() -> {
      if (scene == null) {
        return;
      }
      scene.getStylesheets().clear();
      String theme = isDarkMode ? "/styles/styles.css" : "/styles/light-styles.css";
      scene.getStylesheets().add(this.getClass().getResource(theme).toExternalForm());
      themeToggle.setText(isDarkMode ? "Dark Mode" : "Light Mode");
    });
  }

  /**
   * Saves the application settings to a properties file.
   * The settings include the dark mode setting and the language setting.
   *
   * @param isDarkMode a boolean indicating whether dark mode is enabled
   * @param language   the language setting to be saved
   *
   * @author Fatih Tolip
   */
  public void saveSettings(boolean isDarkMode, String language) {
    Properties props = new Properties();

    // Bestehende Einstellungen laden
    try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
      props.load(in);
    } catch (IOException e) {
      System.out.println("Einstellungen-Datei nicht gefunden, wird neu erstellt.");
    }

    // Neue Werte setzen
    props.setProperty("darkMode", Boolean.toString(isDarkMode));
    props.setProperty("language", language);

    // Datei speichern
    try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
      props.store(out, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads the application settings from a properties file.
   * This method reads the settings from the specified properties file and applies
   * them to the application.
   * It loads the dark mode setting and the language setting. If the properties
   * file cannot be read,
   * it defaults to dark mode being enabled.
   * The dark mode setting is read from the property "darkMode" and defaults to
   * "true" if not specified.
   * The language setting is read from the property "language" and defaults to
   * "de" (German) if not specified.
   * The language setting is then applied to a combo box with values "Englisch"
   * for English and "Deutsch" for German.
   *
   * @throws IOException if an I/O error occurs when reading the properties file
   *
   * @author Fatih Tolip
   */
  public void loadSettings() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
      props.load(in);

      // DarkMode-Einstellung laden
      isDarkMode = Boolean.parseBoolean(props.getProperty("darkMode", "true"));

      // Spracheinstellung laden
      String lang = props.getProperty("language", "de");
      if ("en".equals(lang)) {
        languageComboBox.setValue("Englisch");
      } else {
        languageComboBox.setValue("Deutsch");
      }

    } catch (IOException e) {
      isDarkMode = true; // Standardwert für DarkMode
      e.printStackTrace();
    }
  }

  /**
   * Opens the documentation in the default web browser. If the default browser cannot be opened,
   * it tries to open it using different options on Linux. If all attempts fail, it shows an alert
   * with the documentation URL.
   *
   * @author mian
   */
  private void openDocs() {
    try {
      String docUrl = "https://docs.google.com/document/d/1ORkgtmaymn2wac9gj1fX_38GON4L5VK2aELffv47Xvk/edit?usp=sharing";
      String os = System.getProperty("os.name").toLowerCase();

      // Linux-specific handling
      if (os.contains("linux")) {
        // Try different browser options on Linux
        String[] browsers = {"xdg-open", "google-chrome", "firefox", "mozilla", "konqueror",
            "netscape", "opera"};
        boolean browserOpened = false;

        for (String browser : browsers) {
          try {
            Runtime.getRuntime().exec(new String[]{browser, docUrl});
            browserOpened = true;
            break;
          } catch (Exception e) {
            // Try next browser
          }
        }

        if (!browserOpened) {
          showAlert("Information",
              "Could not open browser automatically.\nPlease manually navigate to: " + docUrl);
        }
      }
      // Windows and Mac handling
      else if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI(docUrl));
      } else {
        showAlert("Information",
            "Desktop operations not supported.\nPlease manually navigate to: " + docUrl);
      }
    } catch (Exception e) {
      showAlert("Error", "Cannot open documentation: " + e.getMessage());
    }
  }

  /**
   * Handles the action of opening the H2 Console. If the H2 server is already running, it opens the
   * browser to the H2 Console URL. If the server is not running, it starts the server and then
   * opens the browser.
   *
   * @author mian
   */
  private void openH2Console() {
    if (isPortInUse(H2_PORT)) {
      openBrowserToH2Console();
      return;
    }

    if (isServerStarting.compareAndSet(false, true)) {
      startH2Server();
    }
  }

  /**
   * Starts the H2 server in a separate thread and waits for it to become available. If the server
   * starts successfully, it opens the browser to the H2 Console URL. If the server fails to start
   * within the timeout period, it shows an error alert.
   *
   * @author mian
   */
  private void startH2Server() {
    new Thread(() -> {
      try {
        // Start server in separate thread to avoid blocking
        Thread serverThread = new Thread(() -> {
          try {
            H2Server.main(new String[0]);
          } catch (Exception e) {
            Platform.runLater(() ->
                showAlert("Error", "H2 Server exception: " + e.getMessage()));
          }
        });
        serverThread.setDaemon(true); // Make thread daemon so it won't prevent JVM shutdown
        serverThread.start();

        // Check for server startup with timeout
        int attempts = 0;
        int maxAttempts = 20; // 10 seconds total
        boolean serverStarted = false;

        while (attempts < maxAttempts && !(serverStarted = isPortInUse(H2_PORT))) {
          Thread.sleep(500);
          attempts++;
        }

        final boolean finalStatus = serverStarted;
        Platform.runLater(() -> {
          if (finalStatus) {
            openBrowserToH2Console();
          } else {
            showAlert("Error", "H2 Console server failed to start within 10 seconds.\n" +
                "Check system logs for more information.");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() ->
            showAlert("Error", "Failed to start H2 Console: " + e.getMessage()));
      } finally {
        isServerStarting.set(false);
      }
    }).start();
  }

  /**
   * Opens the default browser to the H2 Console URL. If desktop operations are not supported, it
   * shows an error alert. There is a known issue with the Desktop API on Linux, where it does not
   * run the default browser as expected. In this case, it tries to open the browser using different
   * options.
   *
   * @author mian
   */
  private void openBrowserToH2Console() {
    try {
      String url = H2_URL;
      String os = System.getProperty("os.name").toLowerCase();

      // Linux-specific handling
      if (os.contains("linux")) {
        // Try different browser options on Linux
        String[] browsers = {"xdg-open", "google-chrome", "firefox", "mozilla", "konqueror",
            "netscape", "opera"};
        boolean browserOpened = false;

        for (String browser : browsers) {
          try {
            Runtime.getRuntime().exec(new String[]{browser, url});
            browserOpened = true;
            break;
          } catch (Exception e) {
            // Try next browser
          }
        }

        if (!browserOpened) {
          showAlert("Information",
              "Could not open browser automatically.\nPlease manually navigate to: " + url);
        }
      }
      // Windows and Mac handling
      else if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI(url));
      } else {
        showAlert("Information",
            "Desktop operations not supported.\nPlease manually navigate to: " + url);
      }
    } catch (Exception e) {
      showAlert("Error", "Cannot open browser: " + e.getMessage());
    }
  }

  /**
   * Checks if a specific port is in use.
   *
   * @param port the port number to check
   * @return true if the port is in use, false otherwise
   * @author mian
   */
  private boolean isPortInUse(int port) {
    try (Socket socket = new Socket("localhost", port)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Shows an alert dialog with the specified title and content.
   *
   * @param title   the title of the alert
   * @param content the content of the alert
   * @author mian
   */
  private void showAlert(String title, String content) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}