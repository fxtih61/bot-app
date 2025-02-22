package com.openjfx.services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 * Service for handling file selection using the system's native file chooser.
 */
public class FileSelecterService {

  private final FileChooser fileChooser;

  /**
   * Constructs a new FileSelecterService with default settings.
   */
  public FileSelecterService() {
    fileChooser = new FileChooser();
    configureFileChooser();
  }

  /**
   * Configures the file chooser with default settings and Excel file filter.
   */
  private void configureFileChooser() {
    fileChooser.setTitle("Select Excel File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );

    // Set initial directory to user's documents folder
    String userHome = System.getProperty("user.home");
    File defaultDirectory = new File(userHome + File.separator + "Documents");
    if (defaultDirectory.exists()) {
      fileChooser.setInitialDirectory(defaultDirectory);
    }
  }

  /**
   * Opens the system's native file chooser dialog for selecting a file.
   *
   * @param stage the parent window for the file chooser
   * @return the selected File object, or null if no file was selected
   */
  public File selectFile(Stage stage) {
    return fileChooser.showOpenDialog(stage);
  }

  /**
   * Sets the initial directory for the file chooser.
   *
   * @param directory the directory to start in
   */
  public void setInitialDirectory(File directory) {
    if (directory != null && directory.exists()) {
      fileChooser.setInitialDirectory(directory);
    }
  }

  /**
   * Sets the title of the file chooser dialog.
   *
   * @param title the title to display
   */
  public void setTitle(String title) {
    fileChooser.setTitle(title);
  }

  /**
   * Returns the FileChooser object used by this service.
   *
   * @return the FileChooser object
   */
  public FileChooser getFileChooser() {
    return fileChooser;
  }
}