package com.openjfx.Excel;

import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomExcelExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoomExcelExportServiceTest {

    private RoomExcelExportService roomExcelExportService;

    @BeforeEach
    void setUp() {
        roomExcelExportService = new RoomExcelExportService(new ExcelService());
    }

    @Test
    void testExportDataFromImage(@TempDir Path tempDir) throws IOException {
        // Temporäre Datei für den Test erstellen
        Path tempFile = tempDir.resolve("test.xlsx");

        // Methode aufrufen
        roomExcelExportService.exportDataFromImage(tempFile.toString());

        // Überprüfen, ob die Datei erstellt wurde
        File file = tempFile.toFile();
        assertTrue(file.exists(), "Die Excel-Datei wurde nicht erstellt.");
        assertTrue(file.length() > 0, "Die Excel-Datei ist leer.");

        // Optional: Dateiinhalt prüfen
        byte[] fileContent = Files.readAllBytes(tempFile);
        assertNotNull(fileContent, "Dateiinhalt ist null.");
        assertTrue(fileContent.length > 0, "Dateiinhalt ist leer.");
    }
}
