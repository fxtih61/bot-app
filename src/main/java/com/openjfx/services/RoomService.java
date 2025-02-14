package com.openjfx.services;

import com.openjfx.models.Room;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * The `RoomService` class provides methods to load and save `Room` objects from and to an Excel
 * file. It uses the `ExcelService` to perform the actual file operations.
 */
public class RoomService {

  private final ExcelService excelService;

  /**
   * Constructs a new `RoomService` with the specified `ExcelService`.
   *
   * @param excelService the `ExcelService` to use for file operations
   */
  public RoomService(ExcelService excelService) {
    this.excelService = excelService;
  }

  /**
   * Loads a list of `Room` objects from the specified Excel file.
   *
   * @param path the path to the Excel file
   * @return a list of `Room` objects loaded from the Excel file
   * @throws IOException if an I/O error occurs
   */
  public List<Room> loadRoomsFromExcel(String path) throws IOException {
    List<Map<String, String>> excelData = excelService.readExcelFile(path);
    List<Room> rooms = new ArrayList<>();

    for (Map<String, String> row : excelData) {
      Room room = new Room(
          row.get("Raum"),
          Integer.parseInt(row.get("Kapazität"))
      );
      rooms.add(room);
    }

    return rooms;
  }

  /**
   * Saves a list of `Room` objects to the specified Excel file.
   *
   * @param rooms the list of `Room` objects to save
   * @param path  the path to the Excel file
   * @throws IOException if an I/O error occurs
   */
  public void saveRoomsToExcel(@NotNull List<Room> rooms, String path) throws IOException {
    List<Map<String, Object>> data = new ArrayList<>();

    for (Room room : rooms) {
      Map<String, Object> row = Map.of(
          "Raum", room.getName(),
          "Kapazität", room.getCapacity()
      );
      data.add(row);
    }

    excelService.createExcelFile(data, path);
  }
}