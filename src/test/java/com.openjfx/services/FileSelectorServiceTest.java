package com.openjfx.services;

import javafx.stage.FileChooser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileSelecterServiceTest {

  private FileSelecterService fileSelecterService;

  @BeforeEach
  void setUp() {
    fileSelecterService = new FileSelecterService();
  }

  @Test
  void testConfigureFileChooser() {
    FileChooser fileChooser = fileSelecterService.getFileChooser();

    assertEquals("Select Excel File", fileChooser.getTitle());

    assertEquals(2, fileChooser.getExtensionFilters().size());
    assertEquals("Excel Files", fileChooser.getExtensionFilters().get(0).getDescription());
    assertEquals("*.xlsx", fileChooser.getExtensionFilters().get(0).getExtensions().get(0));
    assertEquals("*.xls", fileChooser.getExtensionFilters().get(0).getExtensions().get(1));
    assertEquals("All Files", fileChooser.getExtensionFilters().get(1).getDescription());
    assertEquals("*.*", fileChooser.getExtensionFilters().get(1).getExtensions().get(0));

    String userHome = System.getProperty("user.home");
    File expectedDirectory = new File(userHome + File.separator + "Documents");
    if (expectedDirectory.exists()) {
      assertEquals(expectedDirectory, fileChooser.getInitialDirectory());
    }
  }

  @Test
  void testSetInitialDirectory() {
    File tempDir = new File("tempDir");
    tempDir.mkdir();

    fileSelecterService.setInitialDirectory(tempDir);

    assertEquals(tempDir, fileSelecterService.getFileChooser().getInitialDirectory());

    tempDir.delete();
  }

  @Test
  void testSetInitialDirectoryWithNull() {
    fileSelecterService.setInitialDirectory(null);

    String userHome = System.getProperty("user.home");
    File expectedDirectory = new File(userHome + File.separator + "Documents");
    if (expectedDirectory.exists()) {
      assertEquals(expectedDirectory, fileSelecterService.getFileChooser().getInitialDirectory());
    }
  }

  @Test
  void testSetTitle() {
    fileSelecterService.setTitle("New Title");

    assertEquals("New Title", fileSelecterService.getFileChooser().getTitle());
  }
}