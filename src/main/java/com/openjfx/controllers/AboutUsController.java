package com.openjfx.controllers;

import java.net.URI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import java.awt.Desktop;

/**
 * Controller class for the "About Us" section of the application.
 * This class handles the actions for opening GitHub profiles of team members.
 * 
 * FXML Components:
 * - Button fatihButton
 * - Button mianButton
 * - Button leonButton
 * - Button batuhanButton
 * 
 * Methods:
 * - initialize(): Initializes the controller.
 * - openFatihGitHub(ActionEvent event): Opens Fatih's GitHub profile.
 * - openMianGitHub(ActionEvent event): Opens Mian's GitHub profile.
 * - openLeonGitHub(ActionEvent event): Opens Leon's GitHub profile.
 * - openBatuhanGitHub(ActionEvent event): Opens Batuhan's GitHub profile.
 * - openBrowserToUserGithub(String rUrl): Opens the default web browser to the
 * specified URL.
 * - showAlert(String title, String content): Shows an alert dialog with the
 * specified title and content.
 * 
 * @author Fatih Tolip
 */
public class AboutUsController {

    /**
     * FXML annotated buttons representing team members in the About Us section.
     * These buttons are linked to their respective FXML elements.
     * 
     * @author Fatih Tolip
     */
    @FXML
    private Button fatihButton, mianButton, leonButton, batuhanButton;

    @FXML
    public void initialize() {

    }

    /**
     * Opens the default web browser to the specified GitHub URL.
     *
     * @param event the action event that triggered this method
     * @throws Exception if an error occurs while attempting to open the browser
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void openFatihGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/fxtih61";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }
    }

    /**
     * Opens the default web browser to the specified GitHub URL.
     *
     * @param event the action event that triggered this method
     * @throws Exception if an error occurs while attempting to open the browser
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void openMianGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/MianGo7";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }
    }

    /**
     * Opens the default web browser to the specified GitHub URL.
     *
     * @param event the action event that triggered this method
     * @throws Exception if an error occurs while attempting to open the browser
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void openLeonGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/leokin4";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }
    }

    /**
     * Opens the default web browser to the specified GitHub URL.
     *
     * @param event the action event that triggered this method
     * @throws Exception if an error occurs while attempting to open the browser
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void openBatuhanGitHub(ActionEvent event) throws Exception {
        String url = "https://github.com/ymbatu64";
        try {
            openBrowserToUserGithub(url);
        } catch (Exception e) {
            showAlert("Error", "Cannot open browser: " + e.getMessage());
        }
    }

    /**
     * Opens the user's default web browser and navigates to the specified URL.
     * 
     * This method handles different operating systems:
     * - On Linux, it attempts to open the URL using a list of common browsers.
     * - On Windows and Mac, it uses the Desktop API to open the URL.
     * 
     * If the browser cannot be opened automatically, an alert is shown to the user
     * with instructions to manually navigate to the URL.
     * 
     * @param rUrl The URL to open in the web browser.
     * 
     * @author Fatih Tolip
     */
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
