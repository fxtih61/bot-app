package com.openjfx.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private Label navbarTitle;
    @FXML
    private BorderPane contentPane;
    @FXML
    private Button homeButton;
    @FXML
    private Button settingsButton;

    public void initialize() {
        navbarTitle.setText("Navigationsleiste");
        homeButton.setText("Home");
        settingsButton.setText("Einstellungen");

    }

    @FXML
    public void showHome(ActionEvent event) throws Exception {
        try {
            Parent homeView = FXMLLoader.load(getClass().getResource("/home.fxml"));
            contentPane.setCenter(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void showSettings(ActionEvent event) throws Exception {
        try {
            Parent settingsView = FXMLLoader.load(getClass().getResource("/settings.fxml"));
            contentPane.setCenter(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
