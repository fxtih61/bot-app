package com.openjfx.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class AbstractExcelServiceTest {

  private TestModelService testService;

  @BeforeEach
  void setUp() {
    ExcelService excelService = new ExcelService();
    testService = new TestModelService(excelService);
  }

  @Test
  void testFindColumn() {
    Map<String, String> row = Map.of(
        "Test Column", "value",
        "Another Column", "value2"
    );

    assertEquals("Test Column", testService.findColumn(row, "test"));
    assertEquals("", testService.findColumn(row, "nonexistent"));
  }

  @Test
  void testLoadFromExcel_InvalidColumnMapping(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("invalid.xlsx");
    List<Map<String, Object>> invalidData = List.of(
        Map.of("Wrong Column", "value")
    );

    testService.excelService.createExcelFile(invalidData, testFile.toString());
    List<TestModel> result = testService.loadFromExcel(testFile.toString());

    assertTrue(result.isEmpty());
  }

  @Test
  void testSaveToExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<TestModel> models = List.of(
        new TestModel("Test1"),
        new TestModel("Test2")
    );

    testService.saveToExcel(models, testFile.toString());
    List<TestModel> result = testService.loadFromExcel(testFile.toString());

    assertEquals(models.size(), result.size());
    for (int i = 0; i < models.size(); i++) {
      assertEquals(models.get(i).getName(), result.get(i).getName());
    }
  }

  // Test implementation of AbstractExcelService
  private static class TestModelService extends AbstractExcelService<TestModel> {

    public TestModelService(ExcelService excelService) {
      super(excelService);
    }

    @Override
    protected Map<String, String> getColumnPrefixes() {
      return Map.of("name", "test");
    }

    @Override
    protected TestModel createModelFromRow(Map<String, String> row,
        Map<String, String> columnMappings) {
      String name = row.get(columnMappings.get("name"));
      return name != null ? new TestModel(name) : null;
    }

    @Override
    protected Map<String, Object> convertModelToRow(TestModel model) {
      return Map.of("Test Column", model.getName());
    }
  }

  private static class TestModel {

    private final String name;

    public TestModel(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}