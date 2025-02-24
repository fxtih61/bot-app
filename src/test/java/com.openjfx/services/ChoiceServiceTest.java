package com.openjfx.services;

import com.openjfx.models.Choice;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChoiceServiceTest {

  private ChoiceService choiceService;

  @BeforeEach
  void setUp() {
    ExcelService excelService = new ExcelService();
    choiceService = new ChoiceService(excelService);
  }

  @Test
  void testLoadChoicesFromExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("choices.xlsx");
    List<Choice> expectedChoices = List.of(
        new Choice("10A", "John", "Doe", "Event1", "Event2", "Event3", "Event4", "Event5",
            "Event6"),
        new Choice("10B", "Jane", "Smith", "Event2", "Event3", "Event1", "Event5", "Event4",
            "Event6")
    );

    choiceService.saveToExcel(expectedChoices, testFile.toString());
    List<Choice> actualChoices = choiceService.loadFromExcel(new File(testFile.toString()));

    assertEquals(expectedChoices.size(), actualChoices.size());
    for (int i = 0; i < expectedChoices.size(); i++) {
      assertEquals(expectedChoices.get(i).getClassRef(), actualChoices.get(i).getClassRef());
      assertEquals(expectedChoices.get(i).getFirstName(), actualChoices.get(i).getFirstName());
      assertEquals(expectedChoices.get(i).getLastName(), actualChoices.get(i).getLastName());
      assertEquals(expectedChoices.get(i).getChoice1(), actualChoices.get(i).getChoice1());
      assertEquals(expectedChoices.get(i).getChoice2(), actualChoices.get(i).getChoice2());
      assertEquals(expectedChoices.get(i).getChoice3(), actualChoices.get(i).getChoice3());
      assertEquals(expectedChoices.get(i).getChoice4(), actualChoices.get(i).getChoice4());
      assertEquals(expectedChoices.get(i).getChoice5(), actualChoices.get(i).getChoice5());
      assertEquals(expectedChoices.get(i).getChoice6(), actualChoices.get(i).getChoice6());
    }
  }

  @Test
  void testLoadChoicesFromExcel_WithOptionalFieldsEmpty(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("choices.xlsx");
    List<Choice> expectedChoices = List.of(
        new Choice("10A", "John", "Doe", "", "", "", "", "", ""),
        new Choice("10B", "Jane", "Smith", "", "", "", "", "", "")
    );

    choiceService.saveToExcel(expectedChoices, testFile.toString());
    List<Choice> actualChoices = choiceService.loadFromExcel(new File(testFile.toString()));

    assertEquals(expectedChoices.size(), actualChoices.size());
    for (int i = 0; i < expectedChoices.size(); i++) {
      assertEquals(expectedChoices.get(i).getClassRef(), actualChoices.get(i).getClassRef());
      assertEquals(expectedChoices.get(i).getFirstName(), actualChoices.get(i).getFirstName());
      assertEquals(expectedChoices.get(i).getLastName(), actualChoices.get(i).getLastName());
      assertTrue(actualChoices.get(i).getChoice1().isEmpty());
      assertTrue(actualChoices.get(i).getChoice2().isEmpty());
      assertTrue(actualChoices.get(i).getChoice3().isEmpty());
      assertTrue(actualChoices.get(i).getChoice4().isEmpty());
      assertTrue(actualChoices.get(i).getChoice5().isEmpty());
      assertTrue(actualChoices.get(i).getChoice6().isEmpty());
    }
  }

  @Test
  void testLoadChoicesFromExcel_FileNotFound() {
    String invalidPath = "nonexistent.xlsx";
    assertThrows(IOException.class, () -> choiceService.loadFromExcel(new File(invalidPath)));
  }

  @Test
  void testSaveChoicesToExcel_InvalidPath() {
    String invalidPath = "/invalid/path/choices.xlsx";
    List<Choice> choices = List.of(
        new Choice("10A", "John", "Doe", "Event1", "Event2", "Event3", "Event4", "Event5", "Event6")
    );

    assertThrows(IOException.class, () -> choiceService.saveToExcel(choices, invalidPath));
  }

  @Test
  void testLoadChoicesFromExcel_MissingRequiredFields(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("choices.xlsx");
    List<Choice> validChoices = List.of(
        new Choice("10A", "John", "Doe", "Event1", "", "", "", "", "")
    );

    choiceService.saveToExcel(validChoices, testFile.toString());
    List<Choice> loadedChoices = choiceService.loadFromExcel(new File(testFile.toString()));

    assertEquals(1, loadedChoices.size());
    Choice loadedChoice = loadedChoices.get(0);
    assertEquals("10A", loadedChoice.getClassRef());
    assertEquals("John", loadedChoice.getFirstName());
    assertEquals("Doe", loadedChoice.getLastName());
    assertEquals("Event1", loadedChoice.getChoice1());
    assertTrue(loadedChoice.getChoice2().isEmpty());
  }
}