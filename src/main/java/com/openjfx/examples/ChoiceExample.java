package com.openjfx.examples;

import com.openjfx.models.Choice;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.ExcelService;
import java.io.File;
import java.util.List;

/**
 * The `ChoiceExample` class demonstrates how to use the `ChoiceService` to load choices from an
 * Excel file.
 */
public class ChoiceExample {

  /**
   * The main method that runs the example.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    ChoiceService choiceService = new ChoiceService(new ExcelService());
    try {
      List<Choice> choices = choiceService.loadFromExcel(
          new File("daten/1 IMPORTS/IMPORT BOT2_Wahl.xlsx"));
      System.out.println("Choices loaded from Excel file:");
      for (var choice : choices) {
        System.out.println(choice);
      }
    } catch (Exception e) {
      System.err.println("Error loading choices from Excel file: " + e.getMessage());
    }
  }
}
