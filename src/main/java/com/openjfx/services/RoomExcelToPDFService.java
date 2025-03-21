package com.openjfx.services;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse bietet Funktionen zum Exportieren von Raumdaten in eine PDF-Datei.
 * Die Daten werden in einer Tabelle dargestellt, die in der PDF-Datei erstellt wird.
 */
public class RoomExcelToPDFService {

    /**
     * Exportiert die übergebenen Daten in eine PDF-Datei.
     *
     * @param data       Eine Liste von Maps, die die Daten für die Tabelle enthalten.
     *                   Jede Map repräsentiert eine Zeile in der Tabelle.
     * @param outputPath Der Pfad, unter dem die PDF-Datei gespeichert werden soll.
     * @throws IOException Wenn ein Fehler beim Erstellen oder Speichern der PDF-Datei auftritt.
     * @author batuhan
     */
    public void roomExportToPdf(List<Map<String, Object>> data, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = createNewPage(document);
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.OVERWRITE, true);

            // Titel & Einführungstext setzen
            addTitleAndIntro(contentStream);

            // Tabellen-Parameter
            float startX = 50;
            float startY = 500;
            float rowHeight = 15;
            float tableWidth = 700;

            String[] headers = {"Unternehmen", "8:45 – 9:30 (A)", "9:50 – 10:35 (B)", "10:35 – 11:20 (C)", "11:40 – 12:25 (D)", "12:25 – 13:10 (E)"};
            float[] colWidths = calculateColumnWidths(contentStream, headers, data);

            // Tabelle zeichnen mit Grautönen
            drawTable(contentStream, startX, startY, tableWidth, rowHeight, colWidths, headers, data, document);

            contentStream.close();
            document.save(outputPath);
        }
    }

    /**
     * Erstellt eine neue Seite im PDF-Dokument.
     *
     * @param document Das PDF-Dokument, zu dem die Seite hinzugefügt werden soll.
     * @return Die erstellte Seite.
     * @author batuhan
     */
    public PDPage createNewPage(PDDocument document) {
        PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        document.addPage(page);
        return page;
    }

    /**
     * Fügt der PDF-Datei einen Titel und eine Einführung hinzu.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um den Text hinzuzufügen.
     * @throws IOException Wenn ein Fehler beim Hinzufügen des Textes auftritt.
     * @author batuhan
     *
     */
    public void addTitleAndIntro(PDPageContentStream contentStream) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 550);
        contentStream.showText("Organisationsplan für den Berufsorientierungstag");
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 530);
        contentStream.showText("8:30 bis 8:45 Uhr Begrüßung und Einführung in der Aula");
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("13:10 bis 13:20 Uhr Abschluss im Klassenverbund");
        contentStream.endText();
    }

    /**
     * Berechnet die Breiten der Spalten basierend auf den Überschriften und den Daten.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Schriftart zu setzen.
     * @param headers       Die Überschriften der Tabelle.
     * @param data          Die Daten, die in der Tabelle angezeigt werden sollen.
     * @return Ein Array von Floats, das die Breiten der Spalten enthält.
     * @throws IOException Wenn ein Fehler beim Berechnen der Spaltenbreiten auftritt.
     * @author batuhan
     */
    public float[] calculateColumnWidths(PDPageContentStream contentStream, String[] headers, List<Map<String, Object>> data) throws IOException {
        float[] colWidths = new float[headers.length];
        contentStream.setFont(PDType1Font.HELVETICA, 10);

        float maxUnternehmenWidth = 0;
        for (Map<String, Object> row : data) {
            String unternehmen = (String) row.get("Unternehmen");
            float width = PDType1Font.HELVETICA.getStringWidth(unternehmen) / 1000f * 10 + 10;
            if (width > maxUnternehmenWidth) {
                maxUnternehmenWidth = width;
            }
        }

        colWidths[0] = Math.max(maxUnternehmenWidth, PDType1Font.HELVETICA.getStringWidth(headers[0]) / 1000f * 10 + 10);
        for (int i = 1; i < headers.length; i++) {
            colWidths[i] = PDType1Font.HELVETICA.getStringWidth(headers[i]) / 1000f * 10 + 10;
        }

        return colWidths;
    }

    /**
     * Zeichnet die Tabelle in die PDF-Datei.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Tabelle zu zeichnen.
     * @param x             Die X-Koordinate, an der die Tabelle beginnt.
     * @param y             Die Y-Koordinate, an der die Tabelle beginnt.
     * @param tableWidth    Die Breite der Tabelle.
     * @param rowHeight     Die Höhe einer Zeile in der Tabelle.
     * @param colWidths     Die Breiten der Spalten.
     * @param headers       Die Überschriften der Tabelle.
     * @param data          Die Daten, die in der Tabelle angezeigt werden sollen.
     * @param document      Das PDF-Dokument, zu dem die Tabelle hinzugefügt wird.
     * @throws IOException Wenn ein Fehler beim Zeichnen der Tabelle auftritt.
     * @author batuhan
     */
    public void drawTable(PDPageContentStream contentStream, float x, float y, float tableWidth, float rowHeight, float[] colWidths, String[] headers, List<Map<String, Object>> data, PDDocument document) throws IOException {
        float currentY = y;

        // **Header zeichnen (Dunkelgrau)**
        contentStream.setNonStrokingColor(0.8f); // Dunkelgrau
        contentStream.fillRect(x, currentY - rowHeight, tableWidth, rowHeight);
        contentStream.setNonStrokingColor(0); // Zurück zu Schwarz

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        drawRow(contentStream, x, currentY, colWidths, headers);
        drawTableBorders(contentStream, x, currentY, rowHeight, tableWidth, colWidths);
        currentY -= rowHeight;

        // **Datenreihen zeichnen (abwechselnd Hellgrau/Weiß)**
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        boolean alternate = false;

        for (Map<String, Object> row : data) {
            if (currentY < 50) { // Neue Seite nötig
                contentStream.close();
                PDPage page = createNewPage(document);
                contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.OVERWRITE, true);

                currentY = 750;
                contentStream.setNonStrokingColor(0.8f);
                contentStream.fillRect(x, currentY - rowHeight, tableWidth, rowHeight);
                contentStream.setNonStrokingColor(0);

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawRow(contentStream, x, currentY, colWidths, headers);
                drawTableBorders(contentStream, x, currentY, rowHeight, tableWidth, colWidths);
                currentY -= rowHeight;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
            }

            if (alternate) {
                contentStream.setNonStrokingColor(0.9f); // Hellgrau
                contentStream.fillRect(x, currentY - rowHeight, tableWidth, rowHeight);
                contentStream.setNonStrokingColor(0);
            }

            String[] rowData = {
                    (String) row.get("Unternehmen"),
                    (String) row.getOrDefault("Zeit 1", ""),
                    (String) row.getOrDefault("Zeit 2", ""),
                    (String) row.getOrDefault("Zeit 3", ""),
                    (String) row.getOrDefault("Zeit 4", ""),
                    (String) row.getOrDefault("Zeit 5", "")
            };

            drawRow(contentStream, x, currentY, colWidths, rowData);
            drawTableBorders(contentStream, x, currentY, rowHeight, tableWidth, colWidths);
            currentY -= rowHeight;
            alternate = !alternate;
        }

        contentStream.close();
    }

    /**
     * Zeichnet eine Zeile in die Tabelle.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Zeile zu zeichnen.
     * @param x             Die X-Koordinate, an der die Zeile beginnt.
     * @param y             Die Y-Koordinate, an der die Zeile beginnt.
     * @param colWidths     Die Breiten der Spalten.
     * @param columns       Die Daten, die in der Zeile angezeigt werden sollen.
     * @throws IOException Wenn ein Fehler beim Zeichnen der Zeile auftritt.
     * @author batuhan
     */
    public void drawRow(PDPageContentStream contentStream, float x, float y, float[] colWidths, String[] columns) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10); // Schriftart auf normal setzen
        float currentX = x;

        for (int i = 0; i < columns.length; i++) {
            float textX;
            float textY = y - 10;

            if (i == 0) {
                // Erste Spalte (Unternehmen) linksbündig
                textX = currentX + 5;
            } else {
                // Numerische Werte zentrieren
                float textWidth = PDType1Font.HELVETICA.getStringWidth(columns[i]) / 1000f * 10;
                textX = currentX + (colWidths[i] - textWidth) / 2;
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(columns[i] != null ? columns[i] : "");
            contentStream.endText();

            currentX += colWidths[i]; // Update der aktuellen X-Position für die nächste Spalte
        }
    }

    /**
     * Zeichnet die Ränder der Tabelle.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Ränder zu zeichnen.
     * @param x             Die X-Koordinate, an der die Tabelle beginnt.
     * @param y             Die Y-Koordinate, an der die Tabelle beginnt.
     * @param rowHeight     Die Höhe einer Zeile in der Tabelle.
     * @param tableWidth    Die Breite der Tabelle.
     * @param colWidths     Die Breiten der Spalten.
     * @throws IOException Wenn ein Fehler beim Zeichnen der Ränder auftritt.
     * @author batuhan
     */
    public void drawTableBorders(PDPageContentStream contentStream, float x, float y, float rowHeight, float tableWidth, float[] colWidths) throws IOException {
        contentStream.setStrokingColor(0); // Schwarz für Linien

        // Äußere Linien der Tabelle zeichnen
        drawHorizontalLine(contentStream, x, y, tableWidth); // Obere Linie
        drawHorizontalLine(contentStream, x, y - rowHeight, tableWidth); // Untere Linie
        drawVerticalLines(contentStream, x, y, rowHeight, colWidths); // Vertikale Linien

        // Linke und rechte äußere Linien zeichnen
        contentStream.moveTo(x, y);
        contentStream.lineTo(x, y - rowHeight);
        contentStream.stroke();

        contentStream.moveTo(x + tableWidth, y);
        contentStream.lineTo(x + tableWidth, y - rowHeight);
        contentStream.stroke();
    }

    /**
     * Zeichnet eine horizontale Linie in der Tabelle.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Linie zu zeichnen.
     * @param x             Die X-Koordinate, an der die Linie beginnt.
     * @param y             Die Y-Koordinate, an der die Linie beginnt.
     * @param width         Die Länge der Linie.
     * @throws IOException Wenn ein Fehler beim Zeichnen der Linie auftritt.
     * @author batuhan
     */
    private void drawHorizontalLine(PDPageContentStream contentStream, float x, float y, float width) throws IOException {
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.stroke();
    }

    /**
     * Zeichnet vertikale Linien in der Tabelle.
     *
     * @param contentStream Der PDPageContentStream, der verwendet wird, um die Linien zu zeichnen.
     * @param x             Die X-Koordinate, an der die Linien beginnen.
     * @param y             Die Y-Koordinate, an der die Linien beginnen.
     * @param rowHeight     Die Höhe einer Zeile in der Tabelle.
     * @param colWidths     Die Breiten der Spalten.
     * @throws IOException Wenn ein Fehler beim Zeichnen der Linien auftritt.
     * @author batuhan
     */
    private void drawVerticalLines(PDPageContentStream contentStream, float x, float y, float rowHeight, float[] colWidths) throws IOException {
        float currentX = x;
        for (float colWidth : colWidths) {
            contentStream.moveTo(currentX, y);
            contentStream.lineTo(currentX, y - rowHeight);
            contentStream.stroke();
            currentX += colWidth;
        }
    }
}