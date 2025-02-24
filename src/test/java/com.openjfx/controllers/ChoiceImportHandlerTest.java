package com.openjfx.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.openjfx.controllers.Import.ChoiceImportHandler;
import com.openjfx.services.ExcelService;
import com.openjfx.services.ChoiceService;
import com.openjfx.models.Choice;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

class ChoiceImportHandlerTest {

  private ChoiceImportHandler choiceImportHandler;
  private ExcelService excelService;
  private ChoiceService choiceService;

  @BeforeEach
  void setUp() {
    excelService = mock(ExcelService.class);
    choiceService = mock(ChoiceService.class);
    choiceImportHandler = new ChoiceImportHandler(excelService);
  }

  @Test
  void getColumns_returnsCorrectColumns() {
    List<Pair<String, String>> columns = choiceImportHandler.getColumns();
    assertEquals(9, columns.size());
    assertEquals("Class", columns.get(0).getKey());
    assertEquals("classRef", columns.get(0).getValue());
  }

  @Test
  void importData_throwsIOException() {
    File file = new File("invalid.xlsx");
    assertThrows(IOException.class, () -> choiceImportHandler.importData(file));
  }

  @Test
  void matchesSearch_returnsTrueForMatchingItem() {
    Choice choice = new Choice("1", "John", "Doe", "1", "2", "3", "4", "5", "6");
    choice.setFirstName("John");
    assertTrue(choiceImportHandler.matchesSearch(choice, "John"));
  }

  @Test
  void getImportButtonText_returnsCorrectText() {
    assertEquals("Import Choices", choiceImportHandler.getImportButtonText());
  }

  @Test
  void getExcelService_returnsCorrectService() {
    assertEquals(excelService, choiceImportHandler.getExcelService());
  }
}