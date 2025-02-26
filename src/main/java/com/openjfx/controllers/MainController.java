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

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Button importButton, exportButton, settingsButton, aboutButton, exitButton;

    public String lang;

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

    @FXML
    public void exitApp(ActionEvent event) throws Exception {
        try {
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
