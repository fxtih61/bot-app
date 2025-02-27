package com.openjfx.handlers.Import;

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
 *
 * @author mian
 */
public class RoomHandler implements Handler<Room> {

  private final RoomService roomService;

  /**
   * Constructs a RoomImportHandler with the specified ExcelService.
   *
   * @param excelService the Excel service to use for importing data
   * @author mian
   */
  public RoomHandler(ExcelService excelService) {
    this.roomService = new RoomService(excelService);
  }

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   * @author mian
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
   * @author mian
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
   * @author mian
   */
  @Override
  public void importData(File selectedFile) throws IOException {
    File tempFile = TempFileManager.createTempFile(selectedFile);
    try {
      List<Room> rooms = roomService.loadFromExcel(new File(tempFile.getAbsolutePath()));

      if (rooms.isEmpty()) {
        throw new IOException("No rooms found in Excel file");
      }

      // Clear existing room data before importing new data
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
   * @author mian
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
   * @author mian
   */
  @Override
  public String getImportButtonText() {
    return "Import Rooms";
  }

  /**
   * Clears the existing room data.
   *
   * @author mian
   */
  @Override
  public void clearData() {
    roomService.clearRooms();
  }

  /**
   * Gets the Excel service used for import operations.
   *
   * @return the Excel service
   * @author mian
   */
  @Override
  public ExcelService getExcelService() {
    return roomService.getExcelService();
  }
}