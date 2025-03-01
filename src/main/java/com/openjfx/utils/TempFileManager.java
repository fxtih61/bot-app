package com.openjfx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for managing temporary files. Provides methods to create, delete, and clean up
 * temporary files and directories.
 *
 * @author mian
 */
public class TempFileManager {

  public static final String TEMP_DIR = "temp_imports";

  /**
   * Creates a temporary file by copying the source file to a temporary directory.
   *
   * @param sourceFile the source file to be copied
   * @return the created temporary file
   * @throws IOException if an I/O error occurs
   * @author mian
   */
  public static File createTempFile(File sourceFile) throws IOException {
    // Create temp directory if it doesn't exist
    Path tempDir = Path.of(TEMP_DIR);
    Files.createDirectories(tempDir);

    // Create temp file with original name
    String originalName = sourceFile.getName();
    Path tempPath = tempDir.resolve(originalName);

    // Copy the file
    Files.copy(sourceFile.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

    return tempPath.toFile();
  }

  /**
   * Deletes the specified temporary file.
   *
   * @param tempFile the temporary file to be deleted
   * @author mian
   */
  public static void deleteTempFile(File tempFile) {
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  /**
   * Cleans up the temporary directory by deleting all files within it.
   *
   * @author mian
   */
  public static void cleanupTempDirectory() {
    try {
      Files.walk(Path.of(TEMP_DIR))
          .filter(Files::isRegularFile)
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (IOException e) {
      System.err.println("Error cleaning temp directory: " + e.getMessage());
    }
  }
}