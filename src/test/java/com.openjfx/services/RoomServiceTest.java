package com.openjfx.services;

import com.openjfx.models.Room;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest {

  private RoomService roomService;

  @BeforeEach
  void setUp() {
    ExcelService excelService = new ExcelService();
    roomService = new RoomService(excelService);
  }

  @Test
  void testLoadRoomsFromExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<Room> expectedRooms = List.of(
        new Room("Room1", 10),
        new Room("Room2", 20)
    );

    roomService.saveToExcel(expectedRooms, testFile.toString());

    List<Room> actualRooms = roomService.loadFromExcel(new File(testFile.toString()));

    assertEquals(expectedRooms.size(), actualRooms.size());
    for (int i = 0; i < expectedRooms.size(); i++) {
      assertEquals(expectedRooms.get(i).getName(), actualRooms.get(i).getName());
      assertEquals(expectedRooms.get(i).getCapacity(), actualRooms.get(i).getCapacity());
    }
  }

  @Test
  void testLoadRoomsFromExcel_FileNotFound() {
    String invalidPath = "nonexistent.xlsx";

    assertThrows(IOException.class, () -> roomService.loadFromExcel(new File(invalidPath)));
  }

  @Test
  void testSaveRoomsToExcel(@TempDir Path tempDir) throws IOException {
    Path testFile = tempDir.resolve("test.xlsx");
    List<Room> rooms = List.of(
        new Room("Room1", 10),
        new Room("Room2", 20)
    );

    roomService.saveToExcel(rooms, testFile.toString());

    assertTrue(testFile.toFile().exists());

    List<Room> loadedRooms = roomService.loadFromExcel(new File(testFile.toString()));
    assertEquals(rooms.size(), loadedRooms.size());
    for (int i = 0; i < rooms.size(); i++) {
      assertEquals(rooms.get(i).getName(), loadedRooms.get(i).getName());
      assertEquals(rooms.get(i).getCapacity(), loadedRooms.get(i).getCapacity());
    }
  }

  @Test
  void testSaveRoomsToExcel_InvalidPath() {
    String invalidPath = "/invalid/path/test.xlsx";
    List<Room> rooms = List.of(
        new Room("Room1", 10),
        new Room("Room2", 20)
    );

    assertThrows(IOException.class, () -> roomService.saveToExcel(rooms, invalidPath));
  }
}