package com.openjfx.controllers;

import java.net.URI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import java.awt.Desktop;

public class AboutUsController {

    @FXML
    private Button fatihButton, mianButton, leonButton, batuhanButton;

    @FXML
    public void initialize() {

    }

    @FXML
    public void openFatihGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/fxtih61";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }

    }

    @FXML
    public void openMianGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/MianGo7";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }

    }

    @FXML
    public void openLeonGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/leokin4";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }

    }

    @FXML
    public void openBatuhanGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/ymbatu64";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }

    }

    private void openBrowserToUserGithub(String rUrl) {
        try {
            String url = rUrl;
            String os = System.getProperty("os.name").toLowerCase();

            // Linux-specific handling
            if (os.contains("linux")) {
                // Try different browser options on Linux
                String[] browsers = { "xdg-open", "google-chrome", "firefox", "mozilla", "konqueror", "netscape",
                        "opera" };
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
                    showAlert("Information",
                            "Could not open browser automatically.\nPlease manually navigate to: " + url);
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
