package com.openjfx.controllers.Import;

import com.openjfx.services.ExcelService;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ImportHandler<T> {
  List<Pair<String, String>> getColumns();
  List<T> loadData();
  void importData(File selectedFile) throws IOException;
  boolean matchesSearch(T item, String searchTerm);
  String getImportButtonText();
  void clearData();
  ExcelService getExcelService();
}