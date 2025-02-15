package com.openjfx.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExcelExportService {

    private final ExcelService excelService;

    /**
     * Constructor with dependency injection.
     *
     * @param excelService an instance of ExcelService used to handle Excel file operations
     * @throws NullPointerException if excelService is null
     */
    public ExcelExportService(ExcelService excelService) {
        this.excelService = Objects.requireNonNull(excelService, "ExcelService must not be null");
    }

    /**
     * Exports the given data to an Excel file at the specified file path.
     *
     * @param data     a list of maps where each map represents a row of data.
     *                 The keys of the map are used as column headers.
     * @param filePath the destination file path for the exported Excel file
     * @throws IllegalArgumentException if data is null, empty, or does not contain valid headers;
     *                                  or if filePath is null or empty.
     * @throws IOException              if an I/O error occurs while writing the Excel file.
     */
    public void exportDataToExcel(List<Map<String, Object>> data, String filePath) throws IOException {
        // Validate that data is not null or empty
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list must not be null or empty");
        }

        // Validate that the first row contains at least one header
        Map<String, Object> firstRow = data.get(0);
        if (firstRow == null || firstRow.isEmpty()) {
            throw new IllegalArgumentException("Data must contain at least one row with valid headers");
        }

        // Validate that filePath is not null or blank
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }

        // Delegate the Excel file creation to the ExcelService
        excelService.createExcelFile(data, filePath);
    }
}
