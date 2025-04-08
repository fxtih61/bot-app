package com.openjfx.services;

import com.openjfx.dao.FulfillmentScoreDAO;
import com.openjfx.models.FulfillmentScore;
import com.openjfx.models.StudentAssignment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for calculating and managing student fulfillment scores.
 *
 * <p>This service handles the calculation of student choice fulfillment scores
 * based on their assignments and choice preferences. It uses a weighted scoring system where
 * earlier choices are worth more points.</p>
 *
 * @author mian
 */
public class FulfillmentScoreService {

  private final StudentAssignmentService studentAssignmentService;
  private final FulfillmentScoreDAO fulfillmentScoreDAO;
  private static final int MAX_SCORE_PER_STUDENT = 21;
  private static final int[] WEIGHTS = {6, 5, 4, 3, 2, 1};

  /**
   * Constructs a new FulfillmentScoreService with the specified student assignment service.
   *
   * @author mian
   */
  public FulfillmentScoreService(StudentAssignmentService studentAssignmentService) {
    this.studentAssignmentService = studentAssignmentService;
    this.fulfillmentScoreDAO = new FulfillmentScoreDAO();
  }

  /**
   * Calculates the overall fulfillment score for all student assignments.
   *
   * <p>This method processes all student assignments, calculates individual and overall
   * fulfillment scores based on choice preferences, and persists the results. Each choice is
   * weighted differently, with earlier choices worth more points.</p>
   *
   * @return the overall fulfillment percentage as a double
   * @throws RuntimeException if saving fulfillment scores fails
   * @author mian
   */
  public double calculateFulfillmentScore() {
    List<StudentAssignment> assignments = studentAssignmentService.getAllAssignments();
    Map<String, Map<Integer, Integer>> studentChoiceScores = new HashMap<>();
    Map<String, StudentAssignment> studentDetails = new HashMap<>();

    // Initialize and collect scores
    for (StudentAssignment assignment : assignments) {
      String studentId = getStudentId(assignment);
      studentDetails.put(studentId, assignment);
      studentChoiceScores.putIfAbsent(studentId, new HashMap<>());

      int choiceNo = assignment.getChoiceNo();
      if (choiceNo > 0 && choiceNo <= 6) {
        studentChoiceScores.get(studentId).put(choiceNo, WEIGHTS[choiceNo - 1]);
      }
    }

    int totalStudents = studentChoiceScores.size();
    if (totalStudents == 0) {
      return 0.0;
    }

    int totalScore = 0;
    double maxPossibleScore = totalStudents * MAX_SCORE_PER_STUDENT;
    LocalDateTime calculationTime = LocalDateTime.now();

    // Calculate and save individual scores
    for (Map.Entry<String, Map<Integer, Integer>> entry : studentChoiceScores.entrySet()) {
      String studentId = entry.getKey();
      Map<Integer, Integer> scores = entry.getValue();
      StudentAssignment student = studentDetails.get(studentId);

      FulfillmentScore score = new FulfillmentScore();
      score.setStudentId(studentId);
      score.setClassRef(student.getClassRef());
      score.setFirstName(student.getFirstName());
      score.setLastName(student.getLastName());

      int studentTotal = 0;
      for (int i = 1; i <= 6; i++) {
        int choiceScore = scores.getOrDefault(i, 0);
        switch (i) {
          case 1:
            score.setChoice1Score(choiceScore);
            break;
          case 2:
            score.setChoice2Score(choiceScore);
            break;
          case 3:
            score.setChoice3Score(choiceScore);
            break;
          case 4:
            score.setChoice4Score(choiceScore);
            break;
          case 5:
            score.setChoice5Score(choiceScore);
            break;
          case 6:
            score.setChoice6Score(choiceScore);
            break;
        }
        studentTotal += choiceScore;
      }

      score.setStudentTotalScore(studentTotal);
      score.setCalculationTimestamp(calculationTime);
      score.setTotalStudents(totalStudents);
      score.setTotalScore(totalScore);
      score.setMaxPossibleScore(maxPossibleScore);

      totalScore += studentTotal;
      double fulfillmentPercentage = (totalScore / maxPossibleScore) * 100;
      score.setOverallFulfillmentPercentage(fulfillmentPercentage);

      try {
        fulfillmentScoreDAO.saveFulfillmentScore(score);
      } catch (SQLException e) {
        throw new RuntimeException("Failed to save fulfillment score", e);
      }
    }

    return (totalScore / maxPossibleScore) * 100;
  }

  /**
   * Generates a unique student identifier from assignment details.
   *
   * @param assignment the student assignment containing identification details
   * @return a unique string identifier combining first name, last name and class reference
   * @author mian
   */
  private String getStudentId(StudentAssignment assignment) {
    return String.format("%s_%s_%s",
        assignment.getFirstName(),
        assignment.getLastName(),
        assignment.getClassRef());
  }
  /**
   * Prepares fulfillment score data for Excel export
   -  * @param dataToExport List of FulfillmentScore objects
   -  * @return Map with "Headers" and "Students" keys
   -  * @author leon
   -  */
  public Map<String, Object> prepareDataForExportForFulfillmentScore(List<Object> dataToExport) {
    // 1. Create headers
    List<Map<String, String>> headers = new ArrayList<>();
    headers.add(Map.of("Class", "Class"));
    headers.add(Map.of("First Name", "First Name"));
    headers.add(Map.of("Last Name", "Last Name"));
    headers.add(Map.of("Choice 1 Score", "Choice 1 Score"));
    headers.add(Map.of("Choice 2 Score", "Choice 2 Score"));
    headers.add(Map.of("Choice 3 Score", "Choice 3 Score"));
    headers.add(Map.of("Choice 4 Score", "Choice 4 Score"));
    headers.add(Map.of("Choice 5 Score", "Choice 5 Score"));
    headers.add(Map.of("Choice 6 Score", "Choice 6 Score"));
    headers.add(Map.of("Total Score", "Total Score"));
    headers.add(Map.of("Overall %", "Overall %"));
    headers.add(Map.of("Class Total", "Class Total"));
    headers.add(Map.of("Max Possible", "Max Possible"));

    // 2. Prepare student data
    List<Map<String, Object>> students = new ArrayList<>();
    for (Object item : dataToExport) {
      if (item instanceof FulfillmentScore) {
        FulfillmentScore score = (FulfillmentScore) item;
        Map<String, Object> student = new HashMap<>();
        student.put("Class", score.getClassRef());
        student.put("First Name", score.getFirstName());
        student.put("Last Name", score.getLastName());
        student.put("Choice 1 Score", score.getChoice1Score());
        student.put("Choice 2 Score", score.getChoice2Score());
        student.put("Choice 3 Score", score.getChoice3Score());
        student.put("Choice 4 Score", score.getChoice4Score());
        student.put("Choice 5 Score", score.getChoice5Score());
        student.put("Choice 6 Score", score.getChoice6Score());
        student.put("Total Score", score.getStudentTotalScore());
        student.put("Overall %", Math.round(score.getOverallFulfillmentPercentage() * 100.0) / 100.0);
        student.put("Class Total", score.getTotalScore());
        student.put("Max Possible", score.getMaxPossibleScore());
        students.add(student);
      }
    }

    // 3. Return the structure
    Map<String, Object> result = new HashMap<>();
    result.put("Headers", headers);
    result.put("Students", students);
    return result;
  }
  /**
   * The file path where the exported Excel file will be saved for the Fulfillment Score.
   *
   */
  private String filePathScore = "Berechnung_Erfüllungsscore";

  /**
   * Returns the file path to which the data will be exported for the Fulfillment Score.
   *
   * @return The file path as a string.
   *
   * @author leon
   */
  public String getFilePathScore() {
    return filePathScore;
  }

  /**
   * Exports score data to an Excel file
   * @param filename The name of the output Excel file
   * @param scoreData The data to export containing headers and student records
   * @throws IOException If an error occurs during file writing
   *
   * @author leon
   */
  public void exportScoreData(String filename, Map<String, Object> scoreData) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Fulfillment Scores");

      CellStyle headerStyle = createHeaderStyle(workbook);

      @SuppressWarnings("unchecked")
      List<Map<String, String>> headers = (List<Map<String, String>>) scoreData.get("Headers");
      createHeaderRow(sheet, headers, headerStyle);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> students = (List<Map<String, Object>>) scoreData.get("Students");
      createDataRows(sheet, headers, students);

      autoSizeColumns(sheet, headers.size());
      writeWorkbookToFile(workbook, filename);
    }
  }

  /**
   * Creates the style for header cells with gray background and bold text
   * @param workbook The Excel workbook to create the style in
   * @return The configured cell style for headers
   *
   * @author leon
   */
  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);

    return headerStyle;
  }

  /**
   * Creates the header row with column titles
   * @param sheet The Excel sheet to add headers to
   * @param headers List of column header definitions
   * @param headerStyle The style to apply to header cells
   *
   * @author leon
   */
  private void createHeaderRow(Sheet sheet, List<Map<String, String>> headers, CellStyle headerStyle) {
    Row headerRow = sheet.createRow(0);
    int colNum = 0;

    for (Map<String, String> header : headers) {
      Cell cell = headerRow.createCell(colNum++);
      cell.setCellValue(header.values().iterator().next());
      cell.setCellStyle(headerStyle);
    }
  }

  /**
   * Creates data rows with student score information
   * @param sheet The Excel sheet to add data to
   * @param headers List of column definitions for data mapping
   * @param students List of student data records
   *
   * @author leon
   */
  private void createDataRows(Sheet sheet, List<Map<String, String>> headers,
                              List<Map<String, Object>> students) {
    int rowNum = 1;

    for (Map<String, Object> student : students) {
      Row row = sheet.createRow(rowNum++);
      int colNum = 0;

      for (Map<String, String> header : headers) {
        String key = header.keySet().iterator().next();
        Object value = student.get(key);
        Cell cell = row.createCell(colNum++);

        if (value instanceof Number) {
          cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof String) {
          cell.setCellValue((String) value);
        }
      }
    }
  }

  /**
   * Automatically adjusts column widths to fit content
   * @param sheet The Excel sheet to format
   * @param columnCount Number of columns to adjust
   *
   * @author leon
   */
  private void autoSizeColumns(Sheet sheet, int columnCount) {
    for (int i = 0; i < columnCount; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  /**
   * Writes the workbook content to a file
   * @param workbook The Excel workbook to save
   * @param filename The target file name
   * @throws IOException If an error occurs during file writing
   *
   * @author leon
   */
  private void writeWorkbookToFile(Workbook workbook, String filename) throws IOException {
    try (FileOutputStream outputStream = new FileOutputStream(filename)) {
      workbook.write(outputStream);
    }
  }

  /**
   * Exports score data to an PDF file
   * @param filename The name of the output PDF file
   * @param scoreData The data to export containing headers and student records
   * @throws IOException If an error occurs during file writing
   *
   * @author leon
   */
  public void exportScoreDataToPDF(String filename, Map<String, Object> scoreData) throws IOException {
    try (PDDocument document = new PDDocument()) {
      // Create landscape page with extra width
      PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight() + 100, PDRectangle.A4.getWidth()));
      document.addPage(page);

      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      // Font configuration for better readability
      PDFont fontBold = PDType1Font.HELVETICA_BOLD;
      PDFont fontRegular = PDType1Font.HELVETICA;
      float fontSize = 9;  // Slightly smaller for better fit
      float margin = 30;   // Smaller margin for more content space
      float startY = page.getMediaBox().getHeight() - margin;

      // Add report title with dynamic underline
      String title = "Erfüllungsscore";
      contentStream.setFont(fontBold, 14);

      // Calculate exact title width
      float titleWidth = fontBold.getStringWidth(title) / 1000 * 14;

      // Draw title text
      contentStream.beginText();
      contentStream.newLineAtOffset(margin, startY);
      contentStream.showText(title);
      contentStream.endText();

      // Draw underline exactly matching text width
      contentStream.moveTo(margin, startY - 5);
      contentStream.lineTo(margin + titleWidth, startY - 5);
      contentStream.stroke();

      startY -= 30; // Move down for table

      // Prepare data from input map
      @SuppressWarnings("unchecked")
      List<Map<String, String>> headers = (List<Map<String, String>>) scoreData.get("Headers");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> students = (List<Map<String, Object>>) scoreData.get("Students");

      // Optimized column widths based on content
      float[] colWidths = {
              60,  // Class
              70,  // First Name
              80,  // Last Name
              50,  // Choice 1
              50,  // Choice 2
              50,  // Choice 3
              50,  // Choice 4
              50,  // Choice 5
              50,  // Choice 6
              60,  // Total Score
              60,  // Overall %
              70,  // Class Total
              60   // Max Possible
      };

      // Draw table header with two-line column titles
      float tableHeight = drawMultiLineTableHeader(contentStream, margin, startY, colWidths, headers, fontBold);
      startY -= tableHeight;

      // Draw all student rows
      for (Map<String, Object> student : students) {
        if (startY < margin + 20) { // Page break check
          contentStream.close();
          page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight() + 100, PDRectangle.A4.getWidth()));
          document.addPage(page);
          contentStream = new PDPageContentStream(document, page);
          startY = page.getMediaBox().getHeight() - margin;
          tableHeight = drawMultiLineTableHeader(contentStream, margin, startY, colWidths, headers, fontBold);
          startY -= tableHeight;
        }

        drawStudentRow(contentStream, margin, startY, colWidths, headers, student, fontRegular);
        startY -= 18; // Compact row height
      }

      contentStream.close();
      document.save(filename);
    }
  }

  /**
   * Draws a single student data row
   *
   * @param contentStream PDF content stream to draw on
   * @param x Starting X position
   * @param y Starting Y position
   * @param colWidths Array of column widths
   * @param headers List of header definitions
   * @param student Student data to display
   * @param font Font to use for data
   * @throws IOException If there's an error writing to the content stream
   *
   * @author batuhan
   */
  private void drawStudentRow(PDPageContentStream contentStream, float x, float y,
                              float[] colWidths, List<Map<String, String>> headers,
                              Map<String, Object> student, PDFont font) throws IOException {
    float fontSize = 9;

    // Draw row border
    contentStream.setLineWidth(0.5f);
    contentStream.moveTo(x, y);
    contentStream.lineTo(x + getTotalWidth(colWidths), y);
    contentStream.lineTo(x + getTotalWidth(colWidths), y - 18);
    contentStream.lineTo(x, y - 18);
    contentStream.lineTo(x, y);
    contentStream.stroke();

    // Draw cell content
    contentStream.setFont(font, fontSize);
    float currentX = x;

    for (int i = 0; i < headers.size(); i++) {
      String key = headers.get(i).keySet().iterator().next();
      Object value = student.get(key);
      String text = (value != null) ? value.toString() : "";

      // Center text in cell
      float textWidth = font.getStringWidth(text) / 1000 * fontSize;
      float textX = currentX + (colWidths[i] - textWidth) / 2;

      contentStream.beginText();
      contentStream.newLineAtOffset(textX, y - 14);
      contentStream.showText(text);
      contentStream.endText();

      // Draw vertical separator
      if (i < headers.size() - 1) {
        contentStream.moveTo(currentX + colWidths[i], y);
        contentStream.lineTo(currentX + colWidths[i], y - 18);
        contentStream.stroke();
      }
      currentX += colWidths[i];
    }
  }

  /**
   * Draws a multi-line table header with column titles
   *
   * @param contentStream PDF content stream to draw on
   * @param x Starting X position
   * @param y Starting Y position
   * @param colWidths Array of column widths
   * @param headers List of header definitions
   * @param font Font to use for headers
   * @return The height of the drawn header
   * @throws IOException If there's an error writing to the content stream
   *
   * @author bathan
   */
  private float drawMultiLineTableHeader(PDPageContentStream contentStream, float x, float y,
                                         float[] colWidths, List<Map<String, String>> headers,
                                         PDFont font) throws IOException {
    float fontSize = 9;

    // Draw table border
    contentStream.setLineWidth(1f);
    contentStream.moveTo(x, y);
    contentStream.lineTo(x + getTotalWidth(colWidths), y);
    contentStream.lineTo(x + getTotalWidth(colWidths), y - 30); // Taller header
    contentStream.lineTo(x, y - 30);
    contentStream.lineTo(x, y);
    contentStream.stroke();

    // Header content with two-line titles
    contentStream.setFont(font, fontSize);
    float currentX = x;

    // Main header texts (top line)
    String[] headerTexts = {
            "Class", "First", "Last", "Choice 1", "Choice 2", "Choice 3",
            "Choice 4", "Choice 5", "Choice 6", "Total", "Overall", "Class", "Max"
    };

    // Sub-header texts (bottom line)
    String[] subHeaderTexts = {
            "", "Name", "Name", "Score", "Score", "Score", "Score", "Score", "Score", "Score", "%", "Total", "Possible"
    };

    for (int i = 0; i < headers.size(); i++) {
      // Draw main header text (centered)
      float textWidth = font.getStringWidth(headerTexts[i]) / 1000 * fontSize;
      float textX = currentX + (colWidths[i] - textWidth) / 2;

      contentStream.beginText();
      contentStream.newLineAtOffset(textX, y - 12);
      contentStream.showText(headerTexts[i]);
      contentStream.endText();

      // Draw sub-header text if exists (centered)
      if (!subHeaderTexts[i].isEmpty()) {
        textWidth = font.getStringWidth(subHeaderTexts[i]) / 1000 * fontSize;
        textX = currentX + (colWidths[i] - textWidth) / 2;

        contentStream.beginText();
        contentStream.newLineAtOffset(textX, y - 24);
        contentStream.showText(subHeaderTexts[i]);
        contentStream.endText();
      }

      // Draw vertical separator
      if (i < headers.size() - 1) {
        contentStream.moveTo(currentX + colWidths[i], y);
        contentStream.lineTo(currentX + colWidths[i], y - 30);
        contentStream.stroke();
      }
      currentX += colWidths[i];
    }

    return 30; // Return header height
  }

  /**
   * Calculates total width of all columns
   *
   * @param colWidths Array of column widths
   * @return Sum of all column widths
   *
   * @author batuhan
   */
  private float getTotalWidth(float[] colWidths) {
    float total = 0;
    for (float width : colWidths) total += width;
    return total;
  }
}