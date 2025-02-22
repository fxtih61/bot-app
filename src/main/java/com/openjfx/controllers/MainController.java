package com.openjfx.controllers;

import java.io.IOException;

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
    private Button homeButton, importButton, exportButton, settingsButton, aboutButton, exitButton;

    public void initialize() {
        homeButton.setText("Home");
        importButton.setText("Import");
        exportButton.setText("Export");
        settingsButton.setText("Settings");
        aboutButton.setText("About");
        exitButton.setText("Exit");
        contentPane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    /**
     * Sets the active button and updates the UI accordingly.
     * 
     * @param activeButton
     */
    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        homeButton.getStyleClass().remove("button-active");
        importButton.getStyleClass().remove("button-active");
        exportButton.getStyleClass().remove("button-active");
        settingsButton.getStyleClass().remove("button-active");
        aboutButton.getStyleClass().remove("button-active");

        // Add active class to selected button
        activeButton.getStyleClass().add("button-active");
    }

    @FXML
    public void showHome(ActionEvent event) throws Exception {
        try {
            setActiveButton(homeButton);
            Parent homeView = FXMLLoader.load(getClass().getResource("/views/home.fxml"));
            contentPane.getChildren().setAll(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showImport(ActionEvent event) throws Exception {
        try {
            setActiveButton(importButton);
            Parent importView = FXMLLoader.load(getClass().getResource("/views/import.fxml"));
            contentPane.getChildren().setAll(importView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showExport(ActionEvent event) throws Exception {
        try {
            setActiveButton(exportButton);
            Parent exportView = FXMLLoader.load(getClass().getResource("/views/export.fxml"));
            contentPane.getChildren().setAll(exportView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(ActionEvent event) throws Exception {
        try {
            setActiveButton(settingsButton);
            Parent settingsView = FXMLLoader.load(getClass().getResource("/views/settings.fxml"));
            contentPane.getChildren().setAll(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void showAbout(ActionEvent event) throws Exception {
        try {
            setActiveButton(aboutButton);
            Parent aboutView = FXMLLoader.load(getClass().getResource("/views/aboutus.fxml"));
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
