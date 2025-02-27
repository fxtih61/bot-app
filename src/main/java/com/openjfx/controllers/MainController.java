package com.openjfx.controllers;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * MainController is responsible for handling the main user interface actions
 * in the application. It manages the navigation between different views
 * (Import, Export, Settings, About Us) and handles the exit action.
 * 
 * The controller initializes the language setting from the SettingController
 * and updates the UI components accordingly.
 * 
 * The following methods are provided:
 * 
 * - initialize(): Initializes the controller and sets the language setting.
 * - setActiveButton(Button activeButton): Sets the active button and updates
 * the UI accordingly.
 * - showImport(ActionEvent event): Handles the action event to show the
 * "Import" view.
 * - showExport(ActionEvent event): Handles the action event to show the
 * "Export" view.
 * - showSettings(ActionEvent event): Handles the action event to show the
 * "Settings" view.
 * - showAbout(ActionEvent event): Handles the action event to show the
 * "About Us" view.
 * - exitApp(ActionEvent event): Handles the action event to exit the
 * application.
 * 
 * Each method that handles a view change sets the active button, loads the
 * appropriate resource bundle based on the current language setting, and
 * updates the content pane with the corresponding view.
 * 
 * @author Fatih Tolip
 */
public class MainController {

    /**
     * The StackPane that serves as the main content area for the application.
     * This is injected by the FXML loader.
     * 
     * @author Fatih Tolip
     */
    @FXML
    private StackPane contentPane;

    /**
     * FXML annotated buttons used in the MainController.
     * 
     * @FXML private Button importButton - Button to handle import actions.
     * @FXML private Button exportButton - Button to handle export actions.
     * @FXML private Button settingsButton - Button to open settings.
     * @FXML private Button aboutButton - Button to show about information.
     * @FXML private Button exitButton - Button to exit the application.
     * @author Fatih Tolip
     */
    @FXML
    private Button importButton, exportButton, settingsButton, aboutButton, exitButton;

    /**
     * The language setting for the application.
     * 
     * @author Fatih Tolip
     */
    public String lang;

    /**
     * Initializes the MainController by setting the language from the
     * SettingController.
     * This method is called to perform any necessary setup when the controller is
     * created.
     * If an exception occurs during the initialization, it will be caught and its
     * stack trace will be printed.
     * 
     * @author Fatih Tolip
     */
    public void initialize() {
        try {
            SettingController settingsController = new SettingController();
            lang = settingsController.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the active button and updates the UI accordingly.
     * 
     * @param activeButton
     */
    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        importButton.getStyleClass().remove("button-active");
        exportButton.getStyleClass().remove("button-active");
        settingsButton.getStyleClass().remove("button-active");
        aboutButton.getStyleClass().remove("button-active");

        // Add active class to selected button
        activeButton.getStyleClass().add("button-active");
    }

    /**
     * Handles the action event to show the "Import" view.
     * This method sets the active button to the importButton, loads the
     * appropriate
     * resource bundle based on the current language setting, and updates the
     * content pane
     * with the "Import" view.
     *
     * @param event the action event triggered by the user
     * @throws Exception if there is an error during the loading of the FXML file
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void showImport(ActionEvent event) throws Exception {
        try {
            setActiveButton(importButton);
            Locale locale = new Locale(lang);
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            Parent importView = FXMLLoader.load(getClass().getResource("/views/import.fxml"), bundle);
            contentPane.getChildren().setAll(importView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action event to show the "Export" view.
     * This method sets the active button to the exportButton, loads the
     * appropriate
     * resource bundle based on the current language setting, and updates the
     * content pane
     * with the "Export" view.
     *
     * @param event the action event triggered by the user
     * @throws Exception if there is an error during the loading of the FXML file
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void showExport(ActionEvent event) throws Exception {
        try {
            setActiveButton(exportButton);
            Locale locale = new Locale(lang);
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            Parent exportView = FXMLLoader.load(getClass().getResource("/views/export.fxml"), bundle);
            contentPane.getChildren().setAll(exportView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action event to show the "Settings" view.
     * This method sets the active button to the settingsButton, loads the
     * appropriate
     * resource bundle based on the current language setting, and updates the
     * content pane
     * with the "Settings" view.
     *
     * @param event the action event triggered by the user
     * @throws Exception if there is an error during the loading of the FXML file
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void showSettings(ActionEvent event) throws Exception {
        try {
            setActiveButton(settingsButton);
            Locale locale = new Locale(lang);
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            Parent settingsView = FXMLLoader.load(getClass().getResource("/views/settings.fxml"), bundle);
            contentPane.getChildren().setAll(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action event to show the "About Us" view.
     * This method sets the active button to the aboutButton, loads the appropriate
     * resource bundle based on the current language setting, and updates the
     * content pane
     * with the "About Us" view.
     *
     * @param event the action event triggered by the user
     * @throws Exception if there is an error during the loading of the FXML file
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void showAbout(ActionEvent event) throws Exception {
        try {
            setActiveButton(aboutButton);
            Locale locale = new Locale(lang);
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            Parent aboutView = FXMLLoader.load(getClass().getResource("/views/aboutus.fxml"), bundle);
            contentPane.getChildren().setAll(aboutView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action event to exit the application.
     * This method is triggered when the user initiates an exit action.
     * It attempts to gracefully shut down the JavaFX application platform.
     *
     * @param event the action event that triggered this method
     * @throws Exception if an error occurs during the exit process
     * 
     * @author Fatih Tolip
     */
    @FXML
    public void exitApp(ActionEvent event) throws Exception {
        try {
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
