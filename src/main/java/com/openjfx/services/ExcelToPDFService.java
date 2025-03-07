package com.openjfx.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to handle conversion of data to PDF.
 */
public class ExcelToPDFService {

    public void exportToPdf(List<?> data, String outputPath) throws IOException {
        System.out.println("Input Data: " + data);
        List<String[]> convertedData = convertData(data);
        System.out.println("Converted Data: " + convertedData);
        createPDF(outputPath, convertedData);
    }

    private List<String[]> convertData(List<?> data) {
        List<String[]> converted = new ArrayList<>();
        for (Object item : data) {
            converted.add(convertObjectToStringArray(item));
        }
        return converted;
    }

    private String[] convertObjectToStringArray(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof Map) {
            // Handle Map objects
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                values.add(entry.getKey() + "=" + entry.getValue());
            }
        } else {
            // Handle other types of objects (if needed)
            values.add(obj.toString());
        }
        return values.toArray(new String[0]);
    }

    private void createPDF(String outputPath, List<String[]> data) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700);

            for (String[] row : data) {
                contentStream.showText(String.join(" | ", row));
                contentStream.newLineAtOffset(0, -20);
            }

            contentStream.endText();
            contentStream.close();
            document.save(outputPath);
            System.out.println("PDF successfully created: " + outputPath);
        }
    }
}