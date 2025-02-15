package com.openjfx;

import com.openjfx.services.AssignmentService;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.EventService;
import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

  @Override
  public void start(Stage stage) {
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + "\nRunning on Java " + javaVersion);
    // Scene scene = new Scene(new StackPane(label), 640, 480);

    Panel panel = new Panel("This is the title");
    panel.getStyleClass().add("panel-primary");
    BorderPane content = new BorderPane();
    content.setPadding(new Insets(20));
    Button button = new Button("Hello BootstrapFX");
    button.getStyleClass().setAll("btn", "btn-danger");
    content.setCenter(button);
    panel.setBody(content);

    Scene scene = new Scene(panel);
    scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

    stage.setTitle("Hello BootstrapFX");
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
  }

  public static void main(String[] args) {
    // launch();

    ExcelService excelService = new ExcelService();
    ChoiceService choiceService = new ChoiceService(excelService);
    EventService eventService = new EventService(excelService);
    RoomService roomService = new RoomService(excelService);

    AssignmentService assignmentService = new AssignmentService(
        choiceService,
        eventService,
        roomService
    );

    try {
      assignmentService.runAssignment(
          "daten/1 IMPORTS/IMPORT BOT2_Wahl.xlsx",
          "daten/1 IMPORTS/IMPORT BOT1_Veranstaltungsliste.xlsx",
          "daten/1 IMPORTS/IMPORT BOT0_Raumliste.xlsx"
      );
    } catch (Exception e) {
      System.err.println("Error running assignment: " + e.getMessage());
    }
  }
}