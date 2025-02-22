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

public class SettingController {
  @FXML
  private Button h2ConsoleButton;

  private static final int H2_PORT = 8082;
  private static final String H2_URL = "http://localhost:" + H2_PORT;
  private static final AtomicBoolean isServerStarting = new AtomicBoolean(false);

  @FXML
  public void initialize() {
    h2ConsoleButton.setOnAction(event -> openH2Console());
  }

  private void openH2Console() {
    // If server is already running, just open the browser
    if (isPortInUse(H2_PORT)) {
      openBrowserToH2Console();
      return;
    }

    // Only try to start the server if it's not already starting
    if (isServerStarting.compareAndSet(false, true)) {
      startH2Server();
    }
  }

  private void startH2Server() {
    new Thread(() -> {
      try {
        H2Server.main(new String[0]);
        Thread.sleep(1000); // Give the server time to start

        if (isPortInUse(H2_PORT)) {
          Platform.runLater(this::openBrowserToH2Console);
        } else {
          Platform.runLater(() ->
              showAlert("Error", "Failed to start H2 Console server. Please check logs.")
          );
        }
      } catch (Exception e) {
        Platform.runLater(() ->
            showAlert("Error", "Failed to start H2 Console: " + e.getMessage())
        );
      } finally {
        isServerStarting.set(false);
      }
    }).start();
  }

  private void openBrowserToH2Console() {
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI(H2_URL));
      } else {
        showAlert("Error", "Desktop operations are not supported on this platform");
      }
    } catch (Exception e) {
      showAlert("Error", "Failed to open browser: " + e.getMessage());
    }
  }

  private boolean isPortInUse(int port) {
    try (Socket socket = new Socket("localhost", port)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}