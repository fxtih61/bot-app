package com.openjfx.examples;

import com.openjfx.models.Room;
import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;
import java.util.List;

/**
 * The `RoomExample` class demonstrates how to use the `RoomService` to load rooms from an Excel
 * file.
 */
public class RoomExample {

  /**
   * The main method that runs the example.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    RoomService roomService = new RoomService(new ExcelService());
    try {
      List <Room> rooms = roomService.loadFromExcel("daten/1 IMPORTS/IMPORT BOT0_Raumliste.xlsx");
      System.out.println("Rooms loaded from Excel file:");
      for (var room : rooms) {
        System.out.println(room);
      }
    } catch (Exception e) {
      System.err.println("Error loading rooms from Excel file: " + e.getMessage());
    }
  }
}