package com.openjfx.services;

import com.openjfx.models.Room;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Statement;
import java.util.*;

import com.openjfx.config.DatabaseConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service class for handling Room-related Excel operations. This class extends AbstractExcelService
 * to provide specific functionality for reading and writing Room data from/to Excel files.
 *
 * <p>The service maps Excel columns to Room properties using German column headers:
 * <ul>
 *   <li>"raum" → Room name</li>
 *   <li>"kapazität" → Room capacity</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * RoomService roomService = new RoomService(new ExcelService());
 * List<Room> rooms = roomService.loadFromExcel("path/to/excel.xlsx");
 * </pre>
 *
 * @author mian
 */
public class RoomService extends AbstractExcelService<Room> {

  /**
   * Constructs a new RoomService with the specified Excel service.
   *
   * @param excelService the Excel service to use for file operations
   * @author mian
   */
  public RoomService(ExcelService excelService) {
    super(excelService);
  }

  /**
   * Returns a list of required fields for the Room model.
   *
   * @return a list of required fields
   * @author mian
   */
  @Override
  protected List<String> getRequiredFields() {
    return List.of("name", "capacity");
  }

  /**
   * Defines the mapping between internal property names and Excel column prefixes. The column
   * prefixes are case-insensitive partial matches for Excel column headers.
   *
   * @return a Map containing the property-to-column prefix mappings
   * @author mian
   */
  @Override
  protected Map<String, String> getColumnPrefixes() {
    return Map.of(
        "name", "raum",
        "capacity", "kapazität"
    );
  }

  /**
   * Creates a Room object from a row of Excel data.
   *
   * <p>Required fields are:
   * <ul>
   *   <li>Name (string)</li>
   *   <li>Capacity (numeric)</li>
   * </ul>
   *
   * @param row            the row data from Excel
   * @param columnMappings the mappings between internal names and actual Excel columns
   * @return a new Room object, or null if the row data is invalid
   * @author mian
   */
  @Override
  protected Room createModelFromRow(Map<String, String> row, Map<String, String> columnMappings) {
    String name = row.get(columnMappings.get("name"));
    String capacityStr = row.get(columnMappings.get("capacity"));

    // Throw an exception if required fields are missing
    if (name == null || capacityStr == null) {
      throw new IllegalArgumentException("Missing required fields: name or capacity");
    }

    try {
      return new Room(
          name.trim(),
          Integer.parseInt(capacityStr.trim())
      );
    } catch (NumberFormatException e) {
      System.err.println("Error parsing row: " + row + " - " + e.getMessage());
      return null;
    }
  }

  /**
   * Converts a Room object to a map of column names and values for Excel export.
   *
   * @param room the Room object to convert
   * @return a Map containing the column names and values for Excel export
   * @author mian
   */
  @Override
  protected Map<String, Object> convertModelToRow(Room room) {
    return Map.of(
        "Raum", room.getName(),
        "Kapazität", room.getCapacity()
    );
  }

  /**
   * Saves a Room object to the database.
   *
   * @param room the Room object to save
   * @author mian
   */
  public void saveRoom(Room room) {
    String sql = "INSERT INTO rooms ("
        + "name, "
        + "capacity) "
        + "VALUES (?, ?)";

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, room.getName());
        pstmt.setInt(2, room.getCapacity());

        int result = pstmt.executeUpdate();
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        System.err.println("Error saving room, transaction rolled back: " + e.getMessage());
        e.printStackTrace();
      }
    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Deletes all rooms from the database.
   *
   * @author mian
   */
  public void clearRooms() {
    String delete = "DELETE FROM rooms";

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      conn.setAutoCommit(false);
      stmt.executeUpdate(delete);
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Error clearing events: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Loads all rooms from the database.
   *
   * @return a list of Room objects
   * @author mian
   */
  public List<Room> loadRooms() {
    String sql = "SELECT * FROM rooms";
    List<Room> rooms = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        String name = rs.getString("name");
        int capacity = rs.getInt("capacity");

        Room room = new Room(name, capacity);
        rooms.add(room);
      }

    } catch (SQLException e) {
      System.err.println("Database connection error: " + e.getMessage());
      e.printStackTrace();
    }
    return rooms;
  }
  /**
   * The file path where the exported Excel file will be saved.
   *
   */
  private String filePath = "EXPORT BOT4 Room and Schedule Plan";

  /**
   * General headers for the Excel sheet, including event information and instructions.
   */
  private static final List<String> GENERAL_HEADERS = Arrays.asList(
          "Organisationsplan für den Berufsorientierungstag",
          "8:30 bis 8:45 Uhr Begrüßung und Einführung in der Aula",
          "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
  );

  /**
   * Column headers representing time slots for the schedule.
   */
  private static final List<String> TIME_HEADERS = Arrays.asList(
          "", "8:45 - 9:30", "9:50 - 10:35",
          "10:35 - 11:20", "11:40 - 12:25", "12:25 - 13:10"
  );

  /**
   * Letters (A, B, C, D, E) representing the separate rows for time slots.
   */
  private static final List<String> LETTER_HEADERS = Arrays.asList(
          "", "A", "B", "C", "D", "E"
  );

  /**
   * Exports the provided data to an Excel file for rooms.
   *
   * @param data      The data to be exported (list of maps containing company and room/time information)
   * @param filePath  The name of the Excel file to be created
   * @throws IOException If an error occurs during file creation or writing
   *
   * @author leon
   */
  public void exportDataToExcel(List<Map<String, Object>> data, String filePath) throws IOException {
    if (data == null || data.isEmpty()) {
      throw new IllegalArgumentException("Data list must not be null or empty");
    }

    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path must not be null or empty");
    }

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Raumplan");

      // Create styles for the Excel sheet
      Map<String, CellStyle> styles = createStyles(workbook);

      int rowIndex = 0;

      // Add general headers
      rowIndex = addGeneralHeaders(sheet, styles, rowIndex);

      // Add time and letter headers
      rowIndex = addTimeAndLetterHeaders(workbook, sheet, styles, rowIndex);

      // Add data rows
      addDataRows(workbook, sheet, styles, data, rowIndex);

      // Auto-size columns for better readability
      for (int i = 0; i < TIME_HEADERS.size(); i++) {
        sheet.autoSizeColumn(i);
      }

      // Save the file
      try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
        workbook.write(fileOut);
      }
    }
  }

  /**
   * Creates and returns a map of styles for the Excel sheet.
   *
   * @param workbook The Excel workbook
   * @return A map of styles
   *
   * @author leon
   */
  private Map<String, CellStyle> createStyles(Workbook workbook) {
    Map<String, CellStyle> styles = new HashMap<>();

    // Thin border style for all cells
    CellStyle borderStyle = workbook.createCellStyle();
    borderStyle.setBorderTop(BorderStyle.THIN);
    borderStyle.setBorderBottom(BorderStyle.THIN);
    borderStyle.setBorderLeft(BorderStyle.THIN);
    borderStyle.setBorderRight(BorderStyle.THIN);

    // Thick border style for the bottom section
    CellStyle thickBorderStyle = workbook.createCellStyle();
    thickBorderStyle.setBorderTop(BorderStyle.THICK);
    thickBorderStyle.setBorderBottom(BorderStyle.THICK);
    thickBorderStyle.setBorderLeft(BorderStyle.THICK);
    thickBorderStyle.setBorderRight(BorderStyle.THICK);

    // Header style with grey background
    CellStyle headerStyle = workbook.createCellStyle();
    headerStyle.cloneStyleFrom(borderStyle);
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.LEFT);
    headerStyle.setWrapText(true);

    // Header font (bold, size 10)
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 10);
    headerStyle.setFont(headerFont);

    // Organizational plan style (bold, size 14)
    CellStyle orgPlanStyle = workbook.createCellStyle();
    orgPlanStyle.cloneStyleFrom(borderStyle);
    orgPlanStyle.setAlignment(HorizontalAlignment.LEFT);
    orgPlanStyle.setWrapText(true);
    Font orgPlanFont = workbook.createFont();
    orgPlanFont.setBold(true);
    orgPlanFont.setFontHeightInPoints((short) 14);
    orgPlanStyle.setFont(orgPlanFont);

    // Letter style (centered, grey background, bold, size 11)
    CellStyle letterStyle = workbook.createCellStyle();
    letterStyle.cloneStyleFrom(borderStyle);
    letterStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    letterStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    letterStyle.setAlignment(HorizontalAlignment.CENTER);
    Font letterFont = workbook.createFont();
    letterFont.setBold(true);
    letterFont.setFontHeightInPoints((short) 11);
    letterStyle.setFont(letterFont);

    // Bold and thick border style
    CellStyle boldThickBorderStyle = workbook.createCellStyle();
    boldThickBorderStyle.cloneStyleFrom(borderStyle);
    boldThickBorderStyle.setBorderRight(BorderStyle.THICK);
    Font boldFont = workbook.createFont();
    boldFont.setBold(true);
    boldThickBorderStyle.setFont(boldFont);

    // Centered style for rooms
    CellStyle centeredStyle = workbook.createCellStyle();
    centeredStyle.cloneStyleFrom(boldThickBorderStyle);
    centeredStyle.setBorderTop(BorderStyle.THIN);
    centeredStyle.setBorderBottom(BorderStyle.THIN);
    centeredStyle.setBorderLeft(BorderStyle.THIN);
    centeredStyle.setBorderRight(BorderStyle.THIN);
    centeredStyle.setAlignment(HorizontalAlignment.CENTER);

    // General header style (bold, size 11, grey background)
    CellStyle generalHeaderStyle = workbook.createCellStyle();
    generalHeaderStyle.cloneStyleFrom(headerStyle);
    generalHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
    generalHeaderStyle.setWrapText(true);
    Font generalHeaderFont = workbook.createFont();
    generalHeaderFont.setBold(true);
    generalHeaderFont.setFontHeightInPoints((short) 11);
    generalHeaderStyle.setFont(generalHeaderFont);

    // Add styles to the map
    styles.put("borderStyle", borderStyle);
    styles.put("thickBorderStyle", thickBorderStyle);
    styles.put("headerStyle", headerStyle);
    styles.put("orgPlanStyle", orgPlanStyle);
    styles.put("letterStyle", letterStyle);
    styles.put("boldThickBorderStyle", boldThickBorderStyle);
    styles.put("centeredStyle", centeredStyle);
    styles.put("generalHeaderStyle", generalHeaderStyle);

    return styles;
  }

  /**
   * Adds general headers to the Excel sheet.
   *
   * @param sheet      The Excel sheet
   * @param styles     A map of styles
   * @param rowIndex   The current row index
   * @return The updated row index
   *
   * @author leon
   */
  private int addGeneralHeaders(Sheet sheet, Map<String, CellStyle> styles, int rowIndex) {
    for (String header : GENERAL_HEADERS) {
      Row row = sheet.createRow(rowIndex++);
      Cell cell = row.createCell(0);
      cell.setCellValue(header);

      if (header.equals("Organisationsplan für den Berufsorientierungstag")) {
        cell.setCellStyle(styles.get("orgPlanStyle"));
      } else {
        cell.setCellStyle(styles.get("generalHeaderStyle"));
      }

      sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, TIME_HEADERS.size() - 1));

      // Add an empty row after "13:10 bis 13:20 Uhr Abschluss im Klassenverbund"
      if (header.equals("13:10 bis 13:20 Uhr Abschluss im Klassenverbund")) {
        sheet.createRow(rowIndex++);
      }
    }

    return rowIndex;
  }

  /**
   * Adds time and letter headers to the Excel sheet.
   *
   * @param workbook   The Excel workbook
   * @param sheet      The Excel sheet
   * @param styles     A map of styles
   * @param rowIndex   The current row index
   * @return The updated row index
   *
   * @author leon
   */
  private int addTimeAndLetterHeaders(Workbook workbook, Sheet sheet, Map<String, CellStyle> styles, int rowIndex) {
    // Add time headers
    Row timeHeaderRow = sheet.createRow(rowIndex++);
    for (int i = 0; i < TIME_HEADERS.size(); i++) {
      Cell cell = timeHeaderRow.createCell(i);
      cell.setCellValue(TIME_HEADERS.get(i));

      CellStyle timeHeaderCellStyle = workbook.createCellStyle();
      timeHeaderCellStyle.cloneStyleFrom(styles.get("headerStyle"));
      timeHeaderCellStyle.setBorderBottom(BorderStyle.NONE);
      timeHeaderCellStyle.setBorderTop(BorderStyle.THICK);
      if (i == 0) {
        timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
      }
      if (i == TIME_HEADERS.size() - 1) {
        timeHeaderCellStyle.setBorderRight(BorderStyle.THICK);
      }
      cell.setCellStyle(timeHeaderCellStyle);
    }

    // Add letter headers
    Row letterHeaderRow = sheet.createRow(rowIndex++);
    for (int i = 0; i < LETTER_HEADERS.size(); i++) {
      Cell cell = letterHeaderRow.createCell(i);
      cell.setCellValue(LETTER_HEADERS.get(i));

      CellStyle letterHeaderCellStyle = workbook.createCellStyle();
      letterHeaderCellStyle.cloneStyleFrom(styles.get("letterStyle"));
      letterHeaderCellStyle.setBorderTop(BorderStyle.NONE);
      if (i == 0) {
        letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
      }
      if (i == LETTER_HEADERS.size() - 1) {
        letterHeaderCellStyle.setBorderRight(BorderStyle.THICK);
      }
      cell.setCellStyle(letterHeaderCellStyle);
    }

    return rowIndex;
  }

  /**
   * Adds data rows to the Excel sheet.
   *
   * @param workbook   The Excel workbook
   * @param sheet      The Excel sheet
   * @param styles     A map of styles
   * @param data       The data to be added
   * @param rowIndex   The current row index
   *
   * @author leon
   * */
  private void addDataRows(Workbook workbook, Sheet sheet, Map<String, CellStyle> styles, List<Map<String, Object>> data, int rowIndex) {
    int dataRowIndex = 0;
    for (Map<String, Object> rowData : data) {
      Row row = sheet.createRow(rowIndex++);
      dataRowIndex++;
      for (int i = 0; i < TIME_HEADERS.size(); i++) {
        Cell cell = row.createCell(i);
        String key = i == 0 ? "Unternehmen" : "Zeit " + i;
        Object value = rowData.get(key);
        String cellValue = value != null ? value.toString().trim() : "";
        cell.setCellValue(cellValue);

        if (i == 0) {
          cell.setCellStyle(styles.get("boldThickBorderStyle"));
        } else if (i < TIME_HEADERS.size() - 1) {
          cell.setCellStyle(styles.get("centeredStyle"));
        } else {
          CellStyle centeredStyleLastCell = workbook.createCellStyle();
          centeredStyleLastCell.cloneStyleFrom(styles.get("centeredStyle"));
          centeredStyleLastCell.setBorderRight(BorderStyle.THICK);
          cell.setCellStyle(centeredStyleLastCell);
        }

        if (data.size() == dataRowIndex) {
          CellStyle centeredStyleLastLineCell = workbook.createCellStyle();
          centeredStyleLastLineCell.cloneStyleFrom(styles.get("centeredStyle"));
          centeredStyleLastLineCell.setBorderBottom(BorderStyle.THICK);
          cell.setCellStyle(centeredStyleLastLineCell);
          if (i == 0) {
            CellStyle newStyle = workbook.createCellStyle();
            newStyle.cloneStyleFrom(styles.get("boldThickBorderStyle"));
            newStyle.setBorderBottom(BorderStyle.THICK);
            cell.setCellStyle(newStyle);
          }
          if (i == TIME_HEADERS.size() - 1) {
            CellStyle newStyle = workbook.createCellStyle();
            newStyle.cloneStyleFrom(styles.get("centeredStyle"));
            newStyle.setBorderBottom(BorderStyle.THICK);
            newStyle.setBorderRight(BorderStyle.THICK);
            cell.setCellStyle(newStyle);
          }
        }
      }
    }
  }

  /**
   * Prepares a list of maps containing data for export to an Excel file.
   * Each map represents a row in the Excel file, with keys as column headers
   * and values as the corresponding data.
   *
   * @param dataToExport A list of objects (assumed to be maps) containing the raw data.
   * @return A list of maps, where each map represents a row in the Excel file.
   *
   * @author leon
   */
  public List<Map<String, Object>> prepareDataForExport(List<Object> dataToExport) {
    // Initialize the list to hold the rows of data
    List<Map<String, Object>> data = new ArrayList<>();

    // Iterate through the data received at the beginning
    for (Object item : dataToExport) {
      // Assume that the item is a map containing the data
      Map<String, Object> itemMap = (Map<String, Object>) item;

      // Create a new row for the Excel file
      Map<String, Object> row = new LinkedHashMap<>();

      // Add the company to the row
      row.put("Unternehmen", itemMap.get("company"));

      // Add the slots to the row (if they exist)
      row.put("Zeit 1", itemMap.getOrDefault("slot_A", ""));
      row.put("Zeit 2", itemMap.getOrDefault("slot_B", ""));
      row.put("Zeit 3", itemMap.getOrDefault("slot_C", ""));
      row.put("Zeit 4", itemMap.getOrDefault("slot_D", ""));
      row.put("Zeit 5", itemMap.getOrDefault("slot_E", ""));

      // Add the row to the data list
      data.add(row);
    }

    // Return the prepared data
    return data;
  }

  /**
   * Returns the file path to which the data will be exported.
   *
   * @return The file path as a string.
   *
   * @author Leon
   */
  public String getFilePath() {
    return filePath;
  }

}