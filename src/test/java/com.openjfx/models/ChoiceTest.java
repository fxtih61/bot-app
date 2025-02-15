package com.openjfx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ChoiceTest {

  @Test
  void testChoice() {
    Choice choice = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");

    assertEquals("1", choice.getClassRef());
    assertEquals("John", choice.getFirstName());
    assertEquals("Doe", choice.getLastName());
    assertEquals("A", choice.getChoice1());
    assertEquals("B", choice.getChoice2());
    assertEquals("C", choice.getChoice3());
    assertEquals("D", choice.getChoice4());
    assertEquals("E", choice.getChoice5());
    assertEquals("F", choice.getChoice6());

    choice.setClassRef("2");
    choice.setFirstName("Jane");
    choice.setLastName("Smith");
    choice.setChoice1("F");
    choice.setChoice2("E");
    choice.setChoice3("D");
    choice.setChoice4("C");
    choice.setChoice5("B");
    choice.setChoice6("A");

    assertEquals("2", choice.getClassRef());
    assertEquals("Jane", choice.getFirstName());
    assertEquals("Smith", choice.getLastName());
    assertEquals("F", choice.getChoice1());
    assertEquals("E", choice.getChoice2());
    assertEquals("D", choice.getChoice3());
    assertEquals("C", choice.getChoice4());
    assertEquals("B", choice.getChoice5());
    assertEquals("A", choice.getChoice6());
  }

  @Test
  void testEquals() {
    Choice choice1 = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");
    Choice choice2 = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");
    Choice choice3 = new Choice("2", "Jane", "Smith", "F", "E", "D", "C", "B", "A");

    assertEquals(choice1, choice2);
    assertNotEquals(choice1, choice3);
  }

  @Test
  void testHashCode() {
    Choice choice1 = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");
    Choice choice2 = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");
    Choice choice3 = new Choice("2", "Jane", "Smith", "F", "E", "D", "C", "B", "A");

    assertEquals(choice1.hashCode(), choice2.hashCode());
    assertNotEquals(choice1.hashCode(), choice3.hashCode());
  }

  @Test
  void testToString() {
    Choice choice = new Choice("1", "John", "Doe", "A", "B", "C", "D", "E", "F");
    assertEquals(
        "Choice{classRef='1', firstName='John', lastName='Doe', choice1='A', choice2='B', choice3='C', choice4='D', choice5='E', choice6='F'}",
        choice.toString());
  }
}
