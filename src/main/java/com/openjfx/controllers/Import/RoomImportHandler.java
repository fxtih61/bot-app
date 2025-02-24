package com.openjfx.controllers.Import;

import com.openjfx.models.Room;
import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handler for importing Room data from an Excel file.
 */
public class RoomImportHandler implements ImportHandler<Room> {

  private final RoomService roomService;

  /**
   * Constructs a RoomImportHandler with the specified ExcelService.
   *
   * @param excelService the Excel service to use for importing data
   */
  public RoomImportHandler(ExcelService excelService) {
    this.roomService = new RoomService(excelService);
  }

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   */
  @Override
  public List<Pair<String, String>> getColumns() {
    return List.of(
        new Pair<>("Room", "name"),
        new Pair<>("Capacity", "capacity")
    );
  }

  /**
   * Loads the room data to be displayed in the table.
   *
   * @return a list of room data items
   */
  @Override
  public List<Room> loadData() {
    return roomService.loadRooms();
  }

  /**
   * Imports room data from the specified file.
   *
   * @param selectedFile the file to import data from
   * @throws IOException if an I/O error occurs during import
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    File tempFile = TempFileManager.createTempFile(selectedFile);
    try {
      List<Room> rooms = roomService.loadFromExcel(tempFile.getAbsolutePath());
      clearData();
      rooms.forEach(roomService::saveRoom);
    } finally {
      TempFileManager.deleteTempFile(tempFile);
    }
  }

  /**
   * Checks if the given room matches the search term.
   *
   * @param room       the room to check
   * @param searchTerm the search term to match against
   * @return true if the room matches the search term, false otherwise
   */
  @Override
  public boolean matchesSearch(Room room, String searchTerm) {
    String lowerTerm = searchTerm.toLowerCase();
    return room.getName().toLowerCase().contains(lowerTerm) ||
        String.valueOf(room.getCapacity()).contains(lowerTerm);
  }

  /**
   * Gets the text to be displayed on the import button.
   *
   * @return the import button text
   */
  @Override
  public String getImportButtonText() {
    return "Import Rooms";
  }

  /**
   * Clears the existing room data.
   */
  @Override
  public void clearData() {
    roomService.clearRooms();
  }

  /**
   * Gets the Excel service used for import operations.
   *
   * @return the Excel service
   */
  @Override
  public ExcelService getExcelService() {
    return roomService.getExcelService();
  }
}