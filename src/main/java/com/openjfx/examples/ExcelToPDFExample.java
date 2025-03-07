package com.openjfx.examples;

import com.openjfx.services.ExcelToPDFService;

import java.io.IOException;
import java.util.List;

/**
 * Einfache Testmethode zum manuellen Testen der Klasse `ExcelToPDFService`.
 */
public class ExcelToPDFExample {
    /*
    public static void main(String[] args) {
        ExcelToPDFService service = new ExcelToPDFService();
        String excelFilePath = "src/test/resources/test.xlsx";  // Pfad zur Excel-Datei
        String pdfOutputPath = "src/test/resources/output.pdf"; // Pfad zur PDF-Datei

        try {
            // Excel-Datei lesen
            System.out.println("Lese Excel-Datei...");
            List<String[]> data = service.readExcel(excelFilePath);
            System.out.println("Excel erfolgreich gelesen!");

            // Daten in PDF umwandeln
            System.out.println("Erstelle PDF...");
            service.createPDF(pdfOutputPath, data);
            System.out.println("PDF erfolgreich erstellt: " + pdfOutputPath);

        } catch (IOException e) {
            System.err.println("Fehler beim Verarbeiten der Datei: " + e.getMessage());
        }
    }

     */
}
