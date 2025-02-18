package com.openjfx.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * The `ExcelService` class provides methods to create and read Excel files. It uses Apache POI
 * library to handle Excel file operations.
 */
public class ExcelService {

  /**
   * Creates an Excel file with the given data and saves it to the specified file path.
   *
   * @param data the data to be written to the Excel file
   * @param filePath the path where the Excel file will be saved
   * @throws IOException if an I/O error occurs
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
   */
  public List<Map<String, String>> readExcelFile(String filePath) throws IOException {
    List<Map<String, String>> result = new ArrayList<>();

    try (InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream)) {

      // Get the first sheet
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.iterator();

      // Get the header row
      Row headerRow = rowIterator.next();
      List<String> headers = new ArrayList<>();
      for (Cell cell : headerRow) {
        headers.add(cell.getStringCellValue());
      }

      // Get the data rows
      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        Map<String, String> rowData = new HashMap<>();
        int colNum = 0;
        for (Cell cell : row) {
          DataFormatter formatter = new DataFormatter();
          rowData.put(headers.get(colNum++), formatter.formatCellValue(cell));
        }
        result.add(rowData);
      }
      return result;
    }
  }
}