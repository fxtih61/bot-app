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

    /**
     * Controller class for handling the settings view.
     * This class manages the H2 Console button and starts the H2 server when the button is clicked.
     */
    public class SettingController {

      @FXML
      private Button h2ConsoleButton;

      private static final int H2_PORT = 8082;
      private static final String H2_URL = "http://localhost:" + H2_PORT;
      private static final AtomicBoolean isServerStarting = new AtomicBoolean(false);

      /**
       * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
       */
      @FXML
      public void initialize() {
        h2ConsoleButton.setOnAction(event -> openH2Console());
      }

      /**
       * Handles the action of opening the H2 Console.
       * If the H2 server is already running, it opens the browser to the H2 Console URL.
       * If the server is not running, it starts the server and then opens the browser.
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
       * Starts the H2 server in a separate thread and waits for it to become available.
       * If the server starts successfully, it opens the browser to the H2 Console URL.
       * If the server fails to start within the timeout period, it shows an error alert.
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
       * @param title the title of the alert
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