package com.openjfx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class TempFileManagerTest {

  private File sourceFile;

  @BeforeEach
  void setUp() throws IOException {
    sourceFile = File.createTempFile("source", ".txt");
    Files.write(sourceFile.toPath(), "Hello, World!".getBytes());
  }

  @AfterEach
  void tearDown() {
    TempFileManager.cleanupTempDirectory();
    if (sourceFile != null && sourceFile.exists()) {
      sourceFile.delete();
    }
  }

  @Test
  void testCreateTempFile() throws IOException {
    File tempFile = TempFileManager.createTempFile(sourceFile);

    assertTrue(tempFile.exists());
    assertEquals(Files.readAllLines(sourceFile.toPath()), Files.readAllLines(tempFile.toPath()));
  }

  @Test
  void testDeleteTempFile() throws IOException {
    File tempFile = TempFileManager.createTempFile(sourceFile);

    TempFileManager.deleteTempFile(tempFile);

    assertFalse(tempFile.exists());
  }

  @Test
  void testCleanupTempDirectory() throws IOException {
    File tempFile1 = TempFileManager.createTempFile(sourceFile);
    File tempFile2 = TempFileManager.createTempFile(sourceFile);

    TempFileManager.cleanupTempDirectory();

    assertFalse(tempFile1.exists());
    assertFalse(tempFile2.exists());
    assertTrue(Files.list(Path.of(TempFileManager.TEMP_DIR)).count() == 0);
  }

  @Test
  void testCreateTempFileWithNullSource() {
    assertThrows(NullPointerException.class, () -> TempFileManager.createTempFile(null));
  }

  @Test
  void testDeleteTempFileWithNullFile() {
    assertDoesNotThrow(() -> TempFileManager.deleteTempFile(null));
  }
}