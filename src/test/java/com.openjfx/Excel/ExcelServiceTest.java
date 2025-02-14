package com.openjfx.Excel;

import com.openjfx.services.ExcelService;
import java.io.File;
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
    String filePath = tempDir.resolve("test.xls").toString();
    service.createExcelFile(createTestData(), filePath);

    assertTrue(new File(filePath).exists());
  }
}