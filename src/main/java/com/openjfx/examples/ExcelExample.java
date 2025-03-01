package com.openjfx.examples;

import com.openjfx.services.ExcelService;
import java.io.IOException;
import java.util.*;

/**
 * The `ExcelExample` class demonstrates how to use the `ExcelService` to create and read an Excel
 * file.
 *
 * @author mian
 */
public class ExcelExample {

  /**
   * The main method that runs the example.
   *
   * @param args the command line arguments
   * @author mian
   */
  public static void main(String[] args) {
    ExcelService excelService = new ExcelService();

    List<Map<String, Object>> employees = new ArrayList<>();

    Map<String, Object> employee1 = new HashMap<>();
    employee1.put("Name", "John Smith");
    employee1.put("Department", "Engineering");
    employee1.put("Salary", 75000);
    employees.add(employee1);

    Map<String, Object> employee2 = new HashMap<>();
    employee2.put("Name", "Sarah Johnson");
    employee2.put("Department", "Marketing");
    employee2.put("Salary", 65000);
    employees.add(employee2);

    try {
      // Write to Excel
      String filePath = "employees.xlsx";
      excelService.createExcelFile(employees, filePath);
      System.out.println("Excel file created successfully!");

      // Read from Excel
      List<Map<String, String>> readData = excelService.readExcelFile(filePath);

      // Print the read data
      System.out.println("\nData read from Excel:");
      for (Map<String, String> row : readData) {
        System.out.println("Employee: " + row.get("Name") +
            ", Department: " + row.get("Department") +
            ", Salary: $" + row.get("Salary"));
      }

    } catch (IOException e) {
      System.err.println("Error processing Excel file: " + e.getMessage());
    }
  }
}