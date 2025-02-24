package com.openjfx.controllers.Import;

import com.openjfx.models.Choice;
import com.openjfx.services.ChoiceService;
import com.openjfx.services.ExcelService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChoiceImportHandler implements ImportHandler<Choice> {
    private final ChoiceService choiceService;

    public ChoiceImportHandler(ExcelService excelService) {
        this.choiceService = new ChoiceService(excelService);
    }

    @Override
    public List<Pair<String, String>> getColumns() {
        return List.of(
            new Pair<>("Class", "classRef"),
            new Pair<>("First Name", "firstName"),
            new Pair<>("Last Name", "lastName"),
            new Pair<>("Choice 1", "choice1"),
            new Pair<>("Choice 2", "choice2"),
            new Pair<>("Choice 3", "choice3"),
            new Pair<>("Choice 4", "choice4"),
            new Pair<>("Choice 5", "choice5"),
            new Pair<>("Choice 6", "choice6")
        );
    }

    @Override
    public List<Choice> loadData() {
        return choiceService.loadChoices();
    }

    @Override
    public void importData(File selectedFile) throws IOException {
        File tempFile = TempFileManager.createTempFile(selectedFile);
        try {
            List<Choice> choices = choiceService.loadFromExcel(tempFile.getAbsolutePath());
            clearData();
            choices.forEach(choiceService::saveChoice);
        } finally {
            TempFileManager.deleteTempFile(tempFile);
        }
    }

    @Override
    public boolean matchesSearch(Choice choice, String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return choice.getClassRef().toLowerCase().contains(lowerTerm) ||
               choice.getFirstName().toLowerCase().contains(lowerTerm) ||
               choice.getLastName().toLowerCase().contains(lowerTerm) ||
               matchesChoiceNumber(choice, lowerTerm);
    }

    private boolean matchesChoiceNumber(Choice choice, String searchTerm) {
        return choice.getChoice1().toLowerCase().contains(searchTerm) ||
               choice.getChoice2().toLowerCase().contains(searchTerm) ||
               choice.getChoice3().toLowerCase().contains(searchTerm) ||
               choice.getChoice4().toLowerCase().contains(searchTerm) ||
               choice.getChoice5().toLowerCase().contains(searchTerm) ||
               choice.getChoice6().toLowerCase().contains(searchTerm);
    }

    @Override
    public String getImportButtonText() {
        return "Import Choices";
    }

    @Override
    public void clearData() {
        choiceService.clearChoices();
    }

    @Override
    public ExcelService getExcelService() {
        return choiceService.getExcelService();
    }
}