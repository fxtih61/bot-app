package com.openjfx.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExcelToPDFServiceTest {
    private final ExcelToPDFService service = new ExcelToPDFService();

    @Test
    void testReadExcel() throws IOException {
        String testExcelPath = "test.xlsx";
        createTestExcelFile(testExcelPath);

        List<String[]> data = service.readExcel(testExcelPath);

        assertNotNull(data);
        assertFalse(data.isEmpty());
        assertArrayEquals(new String[]{"Name", "Age"}, data.get(0));
        assertArrayEquals(new String[]{"Alice", "30"}, data.get(1));

        new File(testExcelPath).delete();
    }

    @Test
    void testCreatePDF() throws IOException {
        String testPdfPath = "test.pdf";
        List<String[]> data = Arrays.asList(
                new String[]{"Name", "Age"},
                new String[]{"Alice", "30"}
        );

        service.createPDF(testPdfPath, data);

        File pdfFile = new File(testPdfPath);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);

        pdfFile.delete();
    }

    private void createTestExcelFile(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Name");
        headerRow.createCell(1).setCellValue("Age");
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Alice");
        dataRow.createCell(1).setCellValue("30");

        try (var fos = new java.io.FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();
    }
}
