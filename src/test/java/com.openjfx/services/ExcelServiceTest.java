package com.openjfx.services;

import java.io.File;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void testCreateAndReadExcel() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> testData = createTestData();

    String filePath = tempDir.resolve("test.xlsx").toString();
    service.createExcelFile(testData, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);

    assertEquals(2, result.size());
    assertEquals("John", result.get(0).get("Name"));
    assertEquals("30", result.get(0).get("Age"));
    assertEquals("Developer", result.get(0).get("Role"));
  }

  private List<Map<String, Object>> createTestData() {
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("Name", "John");
    row1.put("Age", 30);
    row1.put("Role", "Developer");
    data.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    row2.put("Name", "Jane");
    row2.put("Age", 28);
    row2.put("Role", "Designer");
    data.add(row2);

    return data;
  }

  @Test
  void testFileCreation() throws IOException {
    ExcelService service = new ExcelService();
    String filePath = tempDir.resolve("test.xlsx").toString();
    service.createExcelFile(createTestData(), filePath);

    assertTrue(new File(filePath).exists());
  }

  @Test
  void testEmptyData() {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> emptyData = new ArrayList<>();
    String filePath = tempDir.resolve("empty.xlsx").toString();

    assertThrows(IndexOutOfBoundsException.class,
        () -> service.createExcelFile(emptyData, filePath));
  }

  @Test
  void testMissingKeys() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("Name", "John");
    row1.put("Age", 30);
    data.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    row2.put("Name", "Jane"); // Missing "Age" and "Role"
    data.add(row2);

    String filePath = tempDir.resolve("missing_keys.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals(2, result.size());
    assertEquals("30", result.get(0).get("Age"));
    assertEquals("", result.get(1).get("Age")); // Missing key should be empty
  }

  @Test
  void testUnsupportedDataTypes() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row = new HashMap<>();
    row.put("Name", "John");
    row.put("Active", true);
    row.put("Date", new Date());
    data.add(row);

    String filePath = tempDir.resolve("unsupported_types.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals(1, result.size());
    assertEquals("John", result.get(0).get("Name"));
    assertEquals("", result.get(0).get("Active")); // Unsupported type results in empty cell
  }

  @Test
  void testSpecialCharacters() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row = new HashMap<>();
    row.put("Text", "Line1\nLine2, \"Quote\"");
    data.add(row);

    String filePath = tempDir.resolve("special_chars.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals("Line1\nLine2, \"Quote\"", result.get(0).get("Text"));
  }

  @Test
  void testNumericValues() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row = new HashMap<>();
    row.put("Integer", 25);
    row.put("Double", 25.5);
    data.add(row);

    String filePath = tempDir.resolve("numeric.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals("25", result.get(0).get("Integer"));
    assertTrue(result.get(0).get("Double").replace(",", ".").equals("25.5"));
  }

  @Test
  void testReadEmptyFile() {
    ExcelService service = new ExcelService();
    String filePath = tempDir.resolve("empty.xlsx").toString();

    assertThrows(IOException.class,
        () -> service.readExcelFile(filePath));
  }

  @Test
  void testXlsxFileExtension() throws IOException {
    ExcelService service = new ExcelService();
    String filePath = tempDir.resolve("correct_extension.xlsx").toString();
    service.createExcelFile(createTestData(), filePath);

    assertTrue(new File(filePath).exists());
  }

  @Test
  void testReadFileWithEmptyCells() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("Name", "John");
    row1.put("Age", 30);
    row1.put("Role", "Developer");
    data.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    row2.put("Name", "Jane");
    row2.put("Age", 28);
    row2.put("Role", ""); // Empty cell
    data.add(row2);

    String filePath = tempDir.resolve("empty_cell.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals(2, result.size());
    assertEquals("Developer", result.get(0).get("Role"));
    assertEquals("", result.get(1).get("Role"));
  }

  @Test
  void testReadFileWithEmptyRows() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("Name", "John");
    row1.put("Age", 30);
    row1.put("Role", "Developer");
    data.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    data.add(row2); // Empty row

    Map<String, Object> row3 = new HashMap<>();
    row3.put("Name", "Jane");
    row3.put("Age", 28);
    row3.put("Role", "Designer");
    data.add(row3);

    String filePath = tempDir.resolve("empty_row.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals(3, result.size());
    assertEquals("John", result.get(0).get("Name"));
    assertEquals("", result.get(1).get("Name"));
    assertEquals("Designer", result.get(2).get("Role"));
  }

  @Test
  void testReadFileWithEmptyRowsAndCells() throws IOException {
    ExcelService service = new ExcelService();
    List<Map<String, Object>> data = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("Name", "John");
    row1.put("Age", 30);
    row1.put("Role", "Developer");
    data.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    data.add(row2); // Empty row

    Map<String, Object> row3 = new HashMap<>();
    row3.put("Name", "Jane");
    row3.put("Age", 28);
    row3.put("Role", ""); // Empty cell
    data.add(row3);

    String filePath = tempDir.resolve("empty_row_cell.xlsx").toString();
    service.createExcelFile(data, filePath);

    List<Map<String, String>> result = service.readExcelFile(filePath);
    assertEquals(3, result.size());
    assertEquals("John", result.get(0).get("Name"));
    assertEquals("", result.get(1).get("Name"));
    assertEquals("", result.get(2).get("Role"));
  }
}