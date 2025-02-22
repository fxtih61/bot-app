package com.openjfx;

import com.openjfx.services.AssignmentService;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.EventService;
import com.openjfx.services.ExcelService;

import com.openjfx.config.DatabaseConfig;
import com.openjfx.services.RoomService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

  private int SCENE_WIDTH = 1200;
  private int SCENE_HEIGHT = 800;
  private static Stage primaryStage;

  @Override
  public void init() {
    DatabaseConfig.initializeDatabase();

    //migrate choices from Excel to database, in the future this will be done via the GUI

    /*
    ChoiceService choiceService = new ChoiceService(new ExcelService());
    try {
      choiceService.loadFromExcel("daten/1 IMPORTS/IMPORT BOT2_Wahl.xlsx")
          .forEach(choiceService::saveChoice);
    } catch (Exception e) {
      System.err.println("Error loading choices: " + e.getMessage());
    }

    // migrate events from Excel to database
    EventService eventService = new EventService(new ExcelService());
    try {
      eventService.loadFromExcel("daten/1 IMPORTS/IMPORT BOT1_Veranstaltungsliste.xlsx")
          .forEach(eventService::saveEvent);
    } catch (Exception e) {
      System.err.println("Error loading events: " + e.getMessage());
    }

    // migrate rooms from Excel to database
    RoomService roomService = new RoomService(new ExcelService());
    try {
      roomService.loadFromExcel("daten/1 IMPORTS/IMPORT BOT0_Raumliste.xlsx")
          .forEach(roomService::saveRoom);
    } catch (Exception e) {
      System.err.println("Error loading rooms: " + e.getMessage());
    }
     */

    ExcelService excelService = new ExcelService();
    AssignmentService assignmentService = new AssignmentService(
        new ChoiceService(excelService),
        new EventService(excelService),
        new RoomService(excelService)
    );

    try {
      assignmentService.runAssignment();
    } catch (Exception e) {
      System.err.println("Error running assignment: " + e.getMessage());
    }
  }

  @Override
  public void start(Stage stage) throws Exception {
    primaryStage = stage;
    showWelcomeScene();
  }

  public void showWelcomeScene() throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/views/welcome.fxml"));
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    String css = this.getClass().getResource("/styles/welcome.css").toExternalForm();
    scene.getStylesheets().add(css);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void showMainScene() throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/views/main.fxml"));
    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    String css = this.getClass().getResource("/styles/styles.css").toExternalForm();
    scene.getStylesheets().add(css);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void stop() {
    DatabaseConfig.closeDataSource();
  }
}