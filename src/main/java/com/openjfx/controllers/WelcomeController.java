package com.openjfx.controllers;

import java.io.IOException;

import com.openjfx.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WelcomeController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label textLabel;

    public void initialize() {
        titleLabel.setText("Willkommen");
        textLabel.setText("Herzlich Willkommen zu unserem Azubi Schul Projekt.");
    }

    @FXML
    private void goToLandingPage(ActionEvent event) throws Exception {
        try {
            App app = new App();
            app.showMainScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}