package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * The `ExcelService` class provides methods to create and read Excel files. It uses Apache POI
 * library to handle Excel file operations.
 *
 * @author mian
 */
public class ExcelService {

  /**
   * Creates an Excel file with the given data and saves it to the specified file path.
   *
   * @param data     the data to be written to the Excel file
   * @param filePath the path where the Excel file will be saved
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  public void createExcelFile(List<Map<String, Object>> data, String filePath) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {

      Sheet sheet = workbook.createSheet("Data");

      // Create header row
      Row headerRow = sheet.createRow(0);
      Set<String> headers = data.get(0).keySet();
      int colNum = 0;
      for (String header : headers) {
        headerRow.createCell(colNum++).setCellValue(header);
      }

      // Create data rows
      int rowNum = 1;
      for (Map<String, Object> rowData : data) {
        Row row = sheet.createRow(rowNum++);
        colNum = 0;
        for (String header : headers) {
          Cell cell = row.createCell(colNum++);
          Object value = rowData.get(header);
          if (value instanceof String) {
            cell.setCellValue((String) value);
          } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
          }
        }
      }

      // Write the workbook to the file system
      try (FileOutputStream out = new FileOutputStream(filePath)) {
        workbook.write(out);
      }
    }
  }

  /**
   * Reads an Excel file from the specified file path and returns the data as a list of maps.
   *
   * @param filePath the path to the Excel file
   * @return a list of maps representing the data in the Excel file
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  public List<Map<String, String>> readExcelFile(String filePath) throws IOException {
    List<Map<String, String>> result = new ArrayList<>();

    try (InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream)) {

      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.iterator();

      // Read headers
      Row headerRow = rowIterator.next();
      List<String> headers = new ArrayList<>();
      for (Cell cell : headerRow) {
        headers.add(cell.getStringCellValue().trim()); // Trim headers to avoid whitespace issues
      }
      int numColumns = headers.size();

      // Read data rows
      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        Map<String, String> rowData = new HashMap<>();

        // Iterate through ALL columns (based on header count)
        for (int colIdx = 0; colIdx < numColumns; colIdx++) {
          Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
          DataFormatter formatter = new DataFormatter();
          String value = (cell != null) ? formatter.formatCellValue(cell).trim() : "";
          rowData.put(headers.get(colIdx), value);
        }

        result.add(rowData);
      }
    }
    return result;
  }

  public void createExcelFileCustom(List<Map<String, Object>> data, String filePath)
      throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Data");

      int rowIndex = 0;

      // Daten in das Excel-Blatt schreiben
      for (Map<String, Object> rowData : data) {
        Row row = sheet.createRow(rowIndex++);
        int cellIndex = 0;

        if (rowData.containsKey("Header")) {
          // Allgemeine Überschrift schreiben
          Cell cell = row.createCell(cellIndex);
          cell.setCellValue(rowData.get("Header").toString());
        } else {
          // Spaltenüberschriften oder Datenzeilen schreiben
          for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            Cell cell = row.createCell(cellIndex++);
            if (entry.getValue() instanceof String) {
              cell.setCellValue((String) entry.getValue());
            } else if (entry.getValue() instanceof Number) {
              cell.setCellValue(((Number) entry.getValue()).doubleValue());
            }
          }
        }
      }

      // Datei speichern
      try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
        workbook.write(fileOut);
      }
    }
  }
}