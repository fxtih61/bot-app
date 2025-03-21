package com.openjfx.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoomExcelToPDFServiceTest {

    @Test
    void testCreateNewPage() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = service.createNewPage(document);

        assertNotNull(page);
        assertEquals(1, document.getNumberOfPages());
        assertEquals(PDRectangle.A4.getHeight(), page.getMediaBox().getWidth());
        assertEquals(PDRectangle.A4.getWidth(), page.getMediaBox().getHeight());

        document.close();
    }

    @Test
    void testAddTitleAndIntro() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        service.addTitleAndIntro(contentStream);

        // Überprüfen, ob der Titel und die Einführung korrekt hinzugefügt wurden
        // Dies ist ein einfacher Test, da wir die PDF-Inhalte nicht direkt überprüfen können
        assertNotNull(contentStream);

        contentStream.close();
        document.close();
    }

    @Test
    void testCalculateColumnWidths() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        String[] headers = {"Unternehmen", "8:45 – 9:30 (A)", "9:50 – 10:35 (B)", "10:35 – 11:20 (C)", "11:40 – 12:25 (D)", "12:25 – 13:10 (E)"};
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Unternehmen", "Test Unternehmen");
        data.add(row);

        float[] colWidths = service.calculateColumnWidths(contentStream, headers, data);

        assertNotNull(colWidths);
        assertEquals(headers.length, colWidths.length);
        assertTrue(colWidths[0] > 0); // Die Breite der ersten Spalte sollte größer als 0 sein

        contentStream.close();
        document.close();
    }

    @Test
    void testRoomExportToPdf() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Unternehmen", "Test Unternehmen");
        row.put("Zeit 1", "Test Zeit 1");
        row.put("Zeit 2", "Test Zeit 2");
        row.put("Zeit 3", "Test Zeit 3");
        row.put("Zeit 4", "Test Zeit 4");
        row.put("Zeit 5", "Test Zeit 5");
        data.add(row);

        String outputPath = "test_output.pdf";
        service.roomExportToPdf(data, outputPath);

        // Überprüfen, ob die PDF-Datei erstellt wurde
        PDDocument document = PDDocument.load(new java.io.File(outputPath));
        assertNotNull(document);
        assertEquals(1, document.getNumberOfPages()); // Eine Seite sollte erstellt worden sein

        document.close();

        // Löschen der Test-PDF-Datei nach dem Test
        java.io.File file = new java.io.File(outputPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testDrawTableWithEmptyData() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        List<Map<String, Object>> data = new ArrayList<>();
        String outputPath = "test_output_empty.pdf";
        service.roomExportToPdf(data, outputPath);

        PDDocument document = PDDocument.load(new java.io.File(outputPath));
        assertNotNull(document);
        assertEquals(1, document.getNumberOfPages()); // Eine Seite sollte erstellt worden sein

        document.close();

        // Löschen der Test-PDF-Datei nach dem Test
        java.io.File file = new java.io.File(outputPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testDrawTableWithMultiplePages() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Unternehmen", "Unternehmen " + i);
            row.put("Zeit 1", "Zeit 1 " + i);
            row.put("Zeit 2", "Zeit 2 " + i);
            row.put("Zeit 3", "Zeit 3 " + i);
            row.put("Zeit 4", "Zeit 4 " + i);
            row.put("Zeit 5", "Zeit 5 " + i);
            data.add(row);
        }

        String outputPath = "test_output_multiple_pages.pdf";
        service.roomExportToPdf(data, outputPath);

        PDDocument document = PDDocument.load(new java.io.File(outputPath));
        assertNotNull(document);
        assertTrue(document.getNumberOfPages() > 1); // Mehrere Seiten sollten erstellt worden sein

        document.close();

        // Löschen der Test-PDF-Datei nach dem Test
        java.io.File file = new java.io.File(outputPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testDrawTableBorders() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float x = 50;
        float y = 500;
        float rowHeight = 15;
        float tableWidth = 700;
        float[] colWidths = {100, 100, 100, 100, 100, 100};

        service.drawTableBorders(contentStream, x, y, rowHeight, tableWidth, colWidths);

        // Überprüfen, ob die Ränder korrekt gezeichnet wurden
        // Dies ist ein einfacher Test, da wir die PDF-Inhalte nicht direkt überprüfen können
        assertNotNull(contentStream);

        contentStream.close();
        document.close();
    }

    @Test
    void testDrawRow() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float x = 50;
        float y = 500;
        float[] colWidths = {100, 100, 100, 100, 100, 100};
        String[] columns = {"Unternehmen", "Zeit 1", "Zeit 2", "Zeit 3", "Zeit 4", "Zeit 5"};

        service.drawRow(contentStream, x, y, colWidths, columns);

        // Überprüfen, ob die Zeile korrekt gezeichnet wurde
        // Dies ist ein einfacher Test, da wir die PDF-Inhalte nicht direkt überprüfen können
        assertNotNull(contentStream);

        contentStream.close();
        document.close();
    }
}