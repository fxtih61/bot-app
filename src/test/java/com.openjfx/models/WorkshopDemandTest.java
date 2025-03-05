package com.openjfx.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the WorkshopDemand class.
 *
 * @author mian
 */
public class WorkshopDemandTest {

  @Test
  void constructor_initializesCorrectly() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    assertEquals(1, demand.getEventId());
    assertEquals(100, demand.getDemand());
    assertEquals("CompanyX", demand.getCompanyName());
  }

  @Test
  void setCompanyName_updatesCompanyName() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    demand.setCompanyName("CompanyY");
    assertEquals("CompanyY", demand.getCompanyName());
  }

  @Test
  void setEventId_updatesEventId() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    demand.setEventId(2);
    assertEquals(2, demand.getEventId());
  }

  @Test
  void setDemand_updatesDemand() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    demand.setDemand(200);
    assertEquals(200, demand.getDemand());
  }

  @Test
  void equals_sameObject_returnsTrue() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    assertTrue(demand.equals(demand));
  }

  @Test
  void equals_differentObjectSameValues_returnsTrue() {
    WorkshopDemand demand1 = new WorkshopDemand(1, 100, "CompanyX");
    WorkshopDemand demand2 = new WorkshopDemand(1, 100, "CompanyX");
    assertTrue(demand1.equals(demand2));
  }

  @Test
  void equals_differentObjectDifferentValues_returnsFalse() {
    WorkshopDemand demand1 = new WorkshopDemand(1, 100, "CompanyX");
    WorkshopDemand demand2 = new WorkshopDemand(2, 200, "CompanyY");
    assertFalse(demand1.equals(demand2));
  }

  @Test
  void hashCode_sameValues_returnsSameHashCode() {
    WorkshopDemand demand1 = new WorkshopDemand(1, 100, "CompanyX");
    WorkshopDemand demand2 = new WorkshopDemand(1, 100, "CompanyX");
    assertEquals(demand1.hashCode(), demand2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHashCode() {
    WorkshopDemand demand1 = new WorkshopDemand(1, 100, "CompanyX");
    WorkshopDemand demand2 = new WorkshopDemand(2, 200, "CompanyY");
    assertNotEquals(demand1.hashCode(), demand2.hashCode());
  }

  @Test
  void toString_returnsCorrectStringRepresentation() {
    WorkshopDemand demand = new WorkshopDemand(1, 100, "CompanyX");
    String expected = "WorkshopDemand{eventId=1, demand=100}";
    assertEquals(expected, demand.toString());
  }
}