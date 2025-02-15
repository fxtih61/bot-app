package com.openjfx.controllers;

import java.io.IOException;

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
    private Button homeButton, importButton, exportButton, settingsButton, aboutButton;

    public void initialize() {
        homeButton.setText("Home");
        importButton.setText("Import");
        exportButton.setText("Export");
        settingsButton.setText("Settings");
        aboutButton.setText("About");
    }

    @FXML
    public void showHome(ActionEvent event) throws Exception {
        try {
            Parent homeView = FXMLLoader.load(getClass().getResource("/views/home.fxml"));
            contentPane.getChildren().setAll(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showImport(ActionEvent event) throws Exception {
        try {
            Parent importView = FXMLLoader.load(getClass().getResource("/views/import.fxml"));
            contentPane.getChildren().setAll(importView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showExport(ActionEvent event) throws Exception {
        try {
            Parent exportView = FXMLLoader.load(getClass().getResource("/views/export.fxml"));
            contentPane.getChildren().setAll(exportView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(ActionEvent event) throws Exception {
        try {
            Parent settingsView = FXMLLoader.load(getClass().getResource("/views/settings.fxml"));
            contentPane.getChildren().setAll(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void showAbout(ActionEvent event) throws Exception {
        try {
            Parent aboutView = FXMLLoader.load(getClass().getResource("/views/aboutus.fxml"));
            contentPane.getChildren().setAll(aboutView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
