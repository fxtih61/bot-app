package com.openjfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.awt.Desktop;
import java.net.URI;
import java.net.Socket;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openjfx.App;

/**
 * Controller class for handling the settings view.
 * This class manages the H2 Console button and starts the H2 server when the
 * button is clicked.
 */
public class SettingController {

  @FXML
  private Button h2ConsoleButton;

  @FXML
  private ToggleButton themeToggle;

  @FXML
  private ComboBox<String> languageComboBox;

  private Scene scene;
  private boolean isDarkMode;
  private static final String SETTINGS_FILE = "settings.properties";

  private static final int H2_PORT = 8082;
  private static final String H2_URL = "http://localhost:" + H2_PORT;
  private static final AtomicBoolean isServerStarting = new AtomicBoolean(false);

  /**
   * Initializes the controller class. This method is automatically called after
   * the FXML file has been loaded.
   */
  @FXML
  public void initialize() {
    loadSettings();
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

  private void changeLanguage(String lang) {
    saveSettings(isDarkMode, lang);
    try {
      App app = new App();
      app.showMainScene();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

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

  public void setScene(Scene scene) {
    this.scene = scene;
    applyTheme();
  }

  private void toggleTheme() {
    try {
      isDarkMode = !isDarkMode;
      applyTheme();
      saveSettings(isDarkMode, languageComboBox.getValue().equals("Englisch") ? "en" : "de");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

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
   * Handles the action of opening the H2 Console.
   * If the H2 server is already running, it opens the browser to the H2 Console
   * URL.
   * If the server is not running, it starts the server and then opens the
   * browser.
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
   * Starts the H2 server in a separate thread and waits for it to become
   * available.
   * If the server starts successfully, it opens the browser to the H2 Console
   * URL.
   * If the server fails to start within the timeout period, it shows an error
   * alert.
   */
  private void startH2Server() {
    new Thread(() -> {
      try {
        // Start server in separate thread to avoid blocking
        Thread serverThread = new Thread(() -> {
          try {
            H2Server.main(new String[0]);
          } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "H2 Server exception: " + e.getMessage()));
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
        Platform.runLater(() -> showAlert("Error", "Failed to start H2 Console: " + e.getMessage()));
      } finally {
        isServerStarting.set(false);
      }
    }).start();
  }

  /**
   * Opens the default browser to the H2 Console URL. If desktop operations are
   * not supported, it
   * shows an error alert. There is a known issue with the Desktop API on Linux,
   * where it does not
   * run the default browser as expected. In this case, it tries to open the
   * browser using different
   * options.
   */
  private void openBrowserToH2Console() {
    try {
      String url = H2_URL;
      String os = System.getProperty("os.name").toLowerCase();

      // Linux-specific handling
      if (os.contains("linux")) {
        // Try different browser options on Linux
        String[] browsers = { "xdg-open", "google-chrome", "firefox", "mozilla", "konqueror", "netscape", "opera" };
        boolean browserOpened = false;

        for (String browser : browsers) {
          try {
            Runtime.getRuntime().exec(new String[] { browser, url });
            browserOpened = true;
            break;
          } catch (Exception e) {
            // Try next browser
          }
        }

        if (!browserOpened) {
          showAlert("Information", "Could not open browser automatically.\nPlease manually navigate to: " + url);
        }
      }
      // Windows and Mac handling
      else if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI(url));
      } else {
        showAlert("Information", "Desktop operations not supported.\nPlease manually navigate to: " + url);
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
   */
  private void showAlert(String title, String content) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}