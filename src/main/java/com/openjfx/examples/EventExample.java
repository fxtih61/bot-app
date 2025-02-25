package com.openjfx.examples;

import com.openjfx.models.Event;
import com.openjfx.services.EventService;
import com.openjfx.services.ExcelService;
import java.io.File;
import java.util.List;

/**
 * The `EventExample` class demonstrates how to use the `EventService` to load events from an Excel
 * file.
 *
 * @author mian
 */

public class EventExample {

  /**
   * The main method that runs the example.
   *
   * @param args the command line arguments
   * @author mian
   */
  public static void main(String[] args) {
    EventService eventService = new EventService(new ExcelService());
    try {
      List<Event> events = eventService.loadFromExcel(
          new File("daten/1 IMPORTS/IMPORT BOT1_Veranstaltungsliste.xlsx"));
      System.out.println("Events loaded from Excel file:" + events.size());
      for (var event : events) {
        System.out.println(event);
      }
    } catch (Exception e) {
      System.err.println("Error loading events from Excel file: " + e.getMessage());
    }
  }
}