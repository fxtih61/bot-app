package com.openjfx.services;

import com.openjfx.models.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

  private EventService eventService;

  @BeforeEach
  void setUp() {
    ExcelService excelService = new ExcelService();
    eventService = new EventService(excelService);
  }

  @Test
  void testLoadEventsFromExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<Event> expectedEvents = List.of(
        new Event(1, "Company A", "IT", 30, 10, "09:00"),
        new Event(2, "Company B", "Engineering", 20, 5, "10:30")
    );

    eventService.saveToExcel(expectedEvents, testFile.toString());
    List<Event> actualEvents = eventService.loadFromExcel(testFile.toString());

    assertEquals(expectedEvents.size(), actualEvents.size());
    for (int i = 0; i < expectedEvents.size(); i++) {
      assertEquals(expectedEvents.get(i).getId(), actualEvents.get(i).getId());
      assertEquals(expectedEvents.get(i).getCompany(), actualEvents.get(i).getCompany());
      assertEquals(expectedEvents.get(i).getSubject(), actualEvents.get(i).getSubject());
      assertEquals(expectedEvents.get(i).getMaxParticipants(), actualEvents.get(i).getMaxParticipants());
      assertEquals(expectedEvents.get(i).getMinParticipants(), actualEvents.get(i).getMinParticipants());
      assertEquals(expectedEvents.get(i).getEarliestStart(), actualEvents.get(i).getEarliestStart());
    }
  }

  @Test
  void testLoadEventsFromExcel_WithOptionalFieldsEmpty(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<Event> expectedEvents = List.of(
        new Event(1, "", "", 30, 10, ""),
        new Event(2, "", "", 20, 5, "")
    );

    eventService.saveToExcel(expectedEvents, testFile.toString());
    List<Event> actualEvents = eventService.loadFromExcel(testFile.toString());

    assertEquals(expectedEvents.size(), actualEvents.size());
    for (int i = 0; i < expectedEvents.size(); i++) {
      assertEquals(expectedEvents.get(i).getId(), actualEvents.get(i).getId());
      assertEquals(expectedEvents.get(i).getMaxParticipants(), actualEvents.get(i).getMaxParticipants());
      assertEquals(expectedEvents.get(i).getMinParticipants(), actualEvents.get(i).getMinParticipants());
      assertTrue(actualEvents.get(i).getCompany().isEmpty());
      assertTrue(actualEvents.get(i).getSubject().isEmpty());
      assertTrue(actualEvents.get(i).getEarliestStart().isEmpty());
    }
  }

  @Test
  void testLoadEventsFromExcel_FileNotFound() {
    String invalidPath = "nonexistent.xlsx";
    assertThrows(IOException.class, () -> eventService.loadFromExcel(invalidPath));
  }

  @Test
  void testSaveEventsToExcel_InvalidPath() {
    String invalidPath = "/invalid/path/test.xlsx";
    List<Event> events = List.of(
        new Event(1, "Company A", "IT", 30, 10, "09:00")
    );

    assertThrows(IOException.class, () -> eventService.saveToExcel(events, invalidPath));
  }

  @Test
  void testLoadEventsFromExcel_InvalidData(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    // First save some valid data
    List<Event> validEvents = List.of(
        new Event(1, "Company A", "IT", 30, 10, "09:00")
    );
    eventService.saveToExcel(validEvents, testFile.toString());

    // Load and verify we get only valid events
    List<Event> loadedEvents = eventService.loadFromExcel(testFile.toString());
    assertEquals(1, loadedEvents.size());

    Event loadedEvent = loadedEvents.get(0);
    assertEquals(1, loadedEvent.getId());
    assertEquals("Company A", loadedEvent.getCompany());
    assertEquals("IT", loadedEvent.getSubject());
    assertEquals(30, loadedEvent.getMaxParticipants());
    assertEquals(10, loadedEvent.getMinParticipants());
    assertEquals("09:00", loadedEvent.getEarliestStart());
  }

  @Test
  void testSaveEventsToExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<Event> events = List.of(
        new Event(1, "Company A", "IT", 30, 10, "09:00"),
        new Event(2, "Company B", "Engineering", 20, 5, "10:30")
    );

    eventService.saveToExcel(events, testFile.toString());
    assertTrue(testFile.toFile().exists());

    List<Event> loadedEvents = eventService.loadFromExcel(testFile.toString());
    assertEquals(events.size(), loadedEvents.size());
    for (int i = 0; i < events.size(); i++) {
      assertEquals(events.get(i).getId(), loadedEvents.get(i).getId());
      assertEquals(events.get(i).getCompany(), loadedEvents.get(i).getCompany());
      assertEquals(events.get(i).getSubject(), loadedEvents.get(i).getSubject());
      assertEquals(events.get(i).getMaxParticipants(), loadedEvents.get(i).getMaxParticipants());
      assertEquals(events.get(i).getMinParticipants(), loadedEvents.get(i).getMinParticipants());
      assertEquals(events.get(i).getEarliestStart(), loadedEvents.get(i).getEarliestStart());
    }
  }
}