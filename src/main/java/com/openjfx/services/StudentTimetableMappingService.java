package com.openjfx.services;

import com.openjfx.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class StudentTimetableMappingService {

  public void mapStudentsToTimetable() {
    Map<String, List<TimetableEvent>> timeSlotEvents = new HashMap<>();
    Map<Integer, Integer> eventParticipantCount = new HashMap<>();
    Map<Integer, Set<String>> studentCompanyAssignments = new HashMap<>();
    Map<Integer, Integer> studentToTimetableAssignment = new HashMap<>();

    // Load timetable assignments with company and capacity information
    String timetableSql =
        "SELECT ta.id, ta.event_id, ta.time_slot, e.company, e.max_participants " +
            "FROM timetable_assignments ta " +
            "JOIN events e ON ta.event_id = e.id " +
            "ORDER BY ta.time_slot";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(timetableSql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        TimetableEvent event = new TimetableEvent(
            rs.getInt("id"),
            rs.getInt("event_id"),
            rs.getString("company"),
            rs.getInt("max_participants")
        );
        String timeSlot = rs.getString("time_slot");
        timeSlotEvents.computeIfAbsent(timeSlot, k -> new ArrayList<>()).add(event);
        eventParticipantCount.put(event.timetableId, 0);
      }
    } catch (SQLException e) {
      System.err.println("Error loading timetable assignments: " + e.getMessage());
      return;
    }

    // Load student assignments
    String studentSql =
        "SELECT sa.id, sa.event_id, e.company " +
            "FROM student_assignments sa " +
            "JOIN events e ON sa.event_id = e.id";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(studentSql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int studentAssignmentId = rs.getInt("id");
        String companyName = rs.getString("company");
        studentCompanyAssignments.computeIfAbsent(studentAssignmentId, k -> new HashSet<>())
            .add(companyName);
      }
    } catch (SQLException e) {
      System.err.println("Error loading student assignments: " + e.getMessage());
      return;
    }

    // Rest of the code remains unchanged
    System.out.println("\nTime Slot Analysis and Student Assignments:");
    for (Map.Entry<String, List<TimetableEvent>> timeSlotEntry : timeSlotEvents.entrySet()) {
      String timeSlot = timeSlotEntry.getKey();
      List<TimetableEvent> events = timeSlotEntry.getValue();

      System.out.println("\nTime Slot: " + timeSlot);
      System.out.println("Number of events: " + events.size());

      for (Map.Entry<Integer, Set<String>> studentEntry : studentCompanyAssignments.entrySet()) {
        int studentId = studentEntry.getKey();
        if (studentToTimetableAssignment.containsKey(studentId)) {
          continue;
        }

        for (TimetableEvent event : events) {
          if (studentEntry.getValue().contains(event.company) &&
              eventParticipantCount.get(event.timetableId) < event.maxParticipants) {

            studentToTimetableAssignment.put(studentId, event.timetableId);
            eventParticipantCount.merge(event.timetableId, 1, Integer::sum);

            System.out.println("Student Assignment ID: " + studentId +
                " -> Timetable Assignment ID: " + event.timetableId +
                " (Company: " + event.company +
                ", Current participants: " + eventParticipantCount.get(event.timetableId) +
                "/" + event.maxParticipants + ")");
            break;
          }
        }
      }
    }
  }

  private static class TimetableEvent {

    final int timetableId;
    final int eventId;
    final String company;
    final int maxParticipants;

    TimetableEvent(int timetableId, int eventId, String company, int maxParticipants) {
      this.timetableId = timetableId;
      this.eventId = eventId;
      this.company = company;
      this.maxParticipants = maxParticipants;
    }
  }
}