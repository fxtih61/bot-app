package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for the StudentAssignment model class.
 */
public class StudentAssignmentTest {

  @Test
  void constructorWithAllFields_initializesCorrectly() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    assertEquals(1, assignment.getEventId());
    assertEquals("John", assignment.getFirstName());
    assertEquals("Doe", assignment.getLastName());
    assertEquals("ClassA", assignment.getClassRef());
    assertEquals("CompanyX", assignment.getCompanyName());
    assertEquals("SubjectX", assignment.getSubject());
  }

  @Test
  void constructorWithoutCompanyName_initializesCorrectly() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", null,
        "SubjectX");
    assertEquals(1, assignment.getEventId());
    assertEquals("John", assignment.getFirstName());
    assertEquals("Doe", assignment.getLastName());
    assertEquals("ClassA", assignment.getClassRef());
    assertNull(assignment.getCompanyName());
    assertEquals("SubjectX", assignment.getSubject());
  }

  @Test
  void equals_sameObject_returnsTrue() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    assertTrue(assignment.equals(assignment));
  }

  @Test
  void equals_differentObjectSameValues_returnsTrue() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    StudentAssignment assignment2 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    assertTrue(assignment1.equals(assignment2));
  }

  @Test
  void equals_differentObjectDifferentValues_returnsFalse() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    StudentAssignment assignment2 = new StudentAssignment(2, "Jane", "Smith", "ClassB", "CompanyY",
        "SubjectY");
    assertFalse(assignment1.equals(assignment2));
  }

  @Test
  void hashCode_sameValues_returnsSameHashCode() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    StudentAssignment assignment2 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    assertEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHashCode() {
    StudentAssignment assignment1 = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    StudentAssignment assignment2 = new StudentAssignment(2, "Jane", "Smith", "ClassB", "CompanyY",
        "SubjectY");
    assertNotEquals(assignment1.hashCode(), assignment2.hashCode());
  }

  @Test
  void toString_returnsCorrectStringRepresentation() {
    StudentAssignment assignment = new StudentAssignment(1, "John", "Doe", "ClassA", "CompanyX",
        "SubjectX");
    String expected = "StudentAssignment{eventId=1, firstName='John', lastName='Doe', classRef='ClassA', companyName='CompanyX', subject='SubjectX', timeSlot='null', choice='null', roomId='null'}";
    assertEquals(expected, assignment.toString());
  }
}