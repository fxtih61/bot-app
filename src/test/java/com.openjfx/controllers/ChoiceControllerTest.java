package com.openjfx.controllers;

import com.openjfx.models.Choice;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChoiceControllerTest {

  private TestChoiceService testChoiceService;
  private ChoiceController controller;

  private static class TestChoiceService extends ChoiceService {

    private final List<Choice> choicesToReturn;
    private final List<Choice> savedChoices;
    private IOException exceptionToThrow;

    public TestChoiceService(List<Choice> choicesToReturn) {
      super(new ExcelService());
      this.choicesToReturn = choicesToReturn;
      this.savedChoices = new ArrayList<>();
    }

    @Override
    public List<Choice> loadFromExcel(String path) throws IOException {
      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
      return choicesToReturn;
    }

    @Override
    public void saveChoice(Choice choice) {
      savedChoices.add(choice);
    }

    public void setExceptionToThrow(IOException exception) {
      this.exceptionToThrow = exception;
    }

    public List<Choice> getSavedChoices() {
      return savedChoices;
    }
  }

  @BeforeEach
  void setUp() {
    // Initialize with empty list by default
    testChoiceService = new TestChoiceService(new ArrayList<>());
    controller = new ChoiceController(testChoiceService);
  }

  @Test
  void loadChoices_Success() {
    // Arrange
    List<Choice> testChoices = Arrays.asList(
        new Choice("Class1", "John", "Doe", "Choice1", "Choice2", "Choice3", "Choice4", "Choice5",
            "Choice6"),
        new Choice("Class2", "Jane", "Smith", "Choice1", "Choice2", "Choice3", "Choice4", "Choice5",
            "Choice6")
    );
    testChoiceService = new TestChoiceService(testChoices);
    controller = new ChoiceController(testChoiceService);

    // Act
    assertDoesNotThrow(() -> controller.loadChoices());

    // Assert
    List<Choice> savedChoices = testChoiceService.getSavedChoices();
    assertEquals(2, savedChoices.size());
    assertEquals(testChoices.get(0), savedChoices.get(0));
    assertEquals(testChoices.get(1), savedChoices.get(1));
  }

  @Test
  void loadChoices_EmptyList() {

    // Act
    assertDoesNotThrow(() -> controller.loadChoices());

    // Assert
    assertTrue(testChoiceService.getSavedChoices().isEmpty());
  }

  @Test
  void loadChoices_IOExceptionThrown() {
    // Arrange
    IOException testException = new IOException("Test exception");
    testChoiceService.setExceptionToThrow(testException);

    // Act & Assert
    RuntimeException thrown = assertThrows(
        RuntimeException.class,
        () -> controller.loadChoices()
    );

    // Verify the exception was wrapped correctly
    assertEquals(testException, thrown.getCause());
    assertTrue(testChoiceService.getSavedChoices().isEmpty());
  }

  @Test
  void loadChoices_NullChoiceInList() {
    // Arrange
    Choice validChoice = new Choice("Class1", "John", "Doe", "Choice1", "Choice2", "Choice3",
        "Choice4", "Choice5", "Choice6");
    List<Choice> choicesWithNull = Arrays.asList(validChoice, null);
    testChoiceService = new TestChoiceService(choicesWithNull);
    controller = new ChoiceController(testChoiceService);

    // Act
    assertDoesNotThrow(() -> controller.loadChoices());

    // Assert
    List<Choice> savedChoices = testChoiceService.getSavedChoices();
    assertEquals(1, savedChoices.size());
    assertEquals(validChoice, savedChoices.get(0));
  }
}
