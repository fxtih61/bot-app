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

/**
 * Testklasse für RoomExcelToPDFService.
 * Diese Klasse enthält Unit-Tests für verschiedene Methoden des Service,
 * die die Konvertierung von Excel-Daten in PDF-Dokumente durchführen.
 *
 * @author Batuhan
 */
class RoomExcelToPDFServiceTest {

    /**
     * Testet die Erstellung einer neuen PDF-Seite.
     * @throws IOException Falls ein Fehler beim Erstellen des Dokuments auftritt.
     */
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

    /**
     * Testet das Hinzufügen eines Titels und einer Einführung zum PDF-Dokument.
     * @throws IOException Falls ein Fehler beim Schreiben des Inhalts auftritt.
     * @author batuhan
     */
    @Test
    void testAddTitleAndIntro() throws IOException {
        RoomExcelToPDFService service = new RoomExcelToPDFService();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        service.addTitleAndIntro(contentStream);
        assertNotNull(contentStream);

        contentStream.close();
        document.close();
    }

    /**
     * Testet die Berechnung der Spaltenbreiten für eine Tabelle.
     * @throws IOException Falls ein Fehler beim Erstellen des Inhaltsstroms auftritt.
     * @author batuhan
     */
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
        assertTrue(colWidths[0] > 0);

        contentStream.close();
        document.close();
    }
}