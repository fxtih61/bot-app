package com.openjfx.controllers.Import;

import com.openjfx.models.Room;
import com.openjfx.services.ExcelService;
import com.openjfx.services.RoomService;
import com.openjfx.utils.TempFileManager;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class RoomImportHandler implements ImportHandler<Room> {
    private final RoomService roomService;

    public RoomImportHandler(ExcelService excelService) {
        this.roomService = new RoomService(excelService);
    }

    @Override
    public List<Pair<String, String>> getColumns() {
        return List.of(
            new Pair<>("Room", "name"),
            new Pair<>("Capacity", "capacity")
        );
    }

    @Override
    public List<Room> loadData() {
        return roomService.loadRooms();
    }

    @Override
    public void importData(File selectedFile) throws IOException {
        File tempFile = TempFileManager.createTempFile(selectedFile);
        try {
            List<Room> rooms = roomService.loadFromExcel(tempFile.getAbsolutePath());
            clearData();
            rooms.forEach(roomService::saveRoom);
        } finally {
            TempFileManager.deleteTempFile(tempFile);
        }
    }

    @Override
    public boolean matchesSearch(Room room, String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return room.getName().toLowerCase().contains(lowerTerm) ||
               String.valueOf(room.getCapacity()).contains(lowerTerm);
    }

    @Override
    public String getImportButtonText() {
        return "Import Rooms";
    }

    @Override
    public void clearData() {
        roomService.clearRooms();
    }

    @Override
    public ExcelService getExcelService() {
        return roomService.getExcelService();
    }
}