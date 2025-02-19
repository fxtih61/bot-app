package com.openjfx.controllers;

import com.openjfx.models.Choice;
import com.openjfx.services.ChoiceService;
import java.io.IOException;
import java.util.List;

public class ChoiceController {
  private final ChoiceService choiceService;

  public ChoiceController(ChoiceService choiceService) {
    this.choiceService = choiceService;
  }

  public void loadChoices() {
    try {
      List<Choice> choices = choiceService.loadFromExcel("daten/1 IMPORTS/IMPORT BOT2_Wahl.xlsx");
      for (Choice choice : choices) {
        if (choice != null) {
          choiceService.saveChoice(choice);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}