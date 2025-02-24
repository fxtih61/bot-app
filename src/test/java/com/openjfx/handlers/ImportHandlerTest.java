package com.openjfx.handlers;

import com.openjfx.services.ExcelService;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportHandlerTest {

  @Mock
  private ExcelService excelService;

  private ImportHandler<String> importHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    importHandler = new ImportHandler<>() {
      @Override
      public List<Pair<String, String>> getColumns() {
        return Arrays.asList(new Pair<>("Column1", "Property1"),
            new Pair<>("Column2", "Property2"));
      }

      @Override
      public List<String> loadData() {
        return Arrays.asList("Data1", "Data2");
      }

      @Override
      public void importData(File selectedFile) throws IOException {

      }

      @Override
      public boolean matchesSearch(String item, String searchTerm) {
        return item.contains(searchTerm);
      }

      @Override
      public String getImportButtonText() {
        return "Import Data";
      }

      @Override
      public void clearData() {
        // Clear data implementation
      }

      @Override
      public ExcelService getExcelService() {
        return excelService;
      }
    };
  }

  @Test
  void testGetColumns() {
    List<Pair<String, String>> columns = importHandler.getColumns();
    assertNotNull(columns);
    assertEquals(2, columns.size());
    assertEquals("Column1", columns.get(0).getKey());
    assertEquals("Property1", columns.get(0).getValue());
    assertEquals("Column2", columns.get(1).getKey());
    assertEquals("Property2", columns.get(1).getValue());
  }

  @Test
  void testLoadData() {
    List<String> data = importHandler.loadData();
    assertNotNull(data);
    assertEquals(2, data.size());
    assertEquals("Data1", data.get(0));
    assertEquals("Data2", data.get(1));
  }

  @Test
  void testMatchesSearch() {
    assertTrue(importHandler.matchesSearch("Data1", "Data"));
    assertFalse(importHandler.matchesSearch("Data1", "NotInData"));
  }

  @Test
  void testGetImportButtonText() {
    assertEquals("Import Data", importHandler.getImportButtonText());
  }

  @Test
  void testClearData() {
    assertDoesNotThrow(() -> importHandler.clearData());
  }

  @Test
  void testGetExcelService() {
    assertEquals(excelService, importHandler.getExcelService());
  }
}