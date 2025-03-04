package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for the StudentAssignment model class.
 *
 * @author mian
 */

public class StudentAssignmentTest {

  @Test
  void constructorWithAllFields_initializesCorrectly() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    assertEquals(1, assignment.getEventId());
    assertEquals("John", assignment.getFirstName());
    assertEquals("Doe", assignment.getLastName());
    assertEquals("ClassA", assignment.getClassRef());
    assertEquals("CompanyX", assignment.getCompanyName());
  }

  @Test
  void constructorWithoutCompanyName_initializesCorrectly() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA");
    assertEquals(1, assignment.getEventId());
    assertEquals("John", assignment.getFirstName());
    assertEquals("Doe", assignment.getLastName());
    assertEquals("ClassA", assignment.getClassRef());
    assertNull(assignment.getCompanyName());
  }

  @Test
  void equals_sameObject_returnsTrue() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    assertTrue(assignment.equals(assignment));
  }

  @Test
  void equals_differentObjectSameValues_returnsTrue() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    StudentAssignment assignment2 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    assertTrue(assignment1.equals(assignment2));
  }

  @Test
  void equals_differentObjectDifferentValues_returnsFalse() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    StudentAssignment assignment2 = new StudentAssignment(2, "Jane", "Smith", "ClassB", "CompanyY");
    assertFalse(assignment1.equals(assignment2));
  }

  @Test
  void hashCode_sameValues_returnsSameHashCode() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    StudentAssignment assignment2 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    assertEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHashCode() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    StudentAssignment assignment2 = new StudentAssignment(2, "Jane", "Smith", "ClassB", "CompanyY");
    assertNotEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void toString_returnsCorrectStringRepresentation() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX");
    String expected = "StudentAssignment{, eventId=1, firstName='John', lastName='Doe', classRef='ClassA', companyName='CompanyX'}";
    assertEquals(expected, assignment.toString());
  }
}