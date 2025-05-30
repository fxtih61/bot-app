package com.openjfx.services;

import com.openjfx.models.Choice;
import com.openjfx.models.Event;
import com.openjfx.models.WorkshopDemand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.openjfx.config.DatabaseConfig;

/**
 * Service responsible for calculating workshop demand based on student choices.
 *
 * @author mian
 */
public class WorkshopDemandService {

  /**
   * Calculates how many workshops are needed for each event based on student assignments.
   *
   * @param events list of events
   * @param studentAssignments map of student IDs to their assigned choices
   * @return a map of event IDs to the number of workshops needed for each event
   * @author mian
   */
  public Map<Integer, Integer> calculateWorkshopsNeeded(List<Event> events,
      Map<Integer, List<Choice>> studentAssignments) {
      Map<Integer, Integer> assignmentCounts = countAssignments(studentAssignments);
      Map<Integer, Integer> workshopsNeeded = new HashMap<>();

      // Calculate total workshops needed for each event
      for (Event event : events) {
          int eventId = event.getId();
        int assignedCount = assignmentCounts.getOrDefault(eventId, 0);
          int maxCapacity = event.getMaxParticipants();
          workshopsNeeded.put(eventId, calculateAdditionalWorkshops(assignedCount, maxCapacity));
      }

      return workshopsNeeded;
  }

  /**
   * Counts all assignments for each event.
   *
   * @param studentAssignments map of student IDs to their assigned choices
   * @return map of event IDs to number of assigned students
   * @author mian
   */
  private Map<Integer, Integer> countAssignments(Map<Integer, List<Choice>> studentAssignments) {
      Map<Integer, Integer> counts = new HashMap<>();

      for (List<Choice> assignments : studentAssignments.values()) {
          for (Choice assignment : assignments) {
              String eventStr = assignment.getChoice1(); // Assuming choice1 contains the assigned event
              if (eventStr != null && !eventStr.isEmpty()) {
                  try {
                      int eventId = Integer.parseInt(eventStr.replaceAll("[^0-9]", ""));
                      counts.merge(eventId, 1, Integer::sum);
                  } catch (NumberFormatException e) {
                      // Ignore invalid assignments
                  }
              }
          }
      }

      return counts;
  }

  /**
   * Counts all choices for each event across all priority levels.
   *
   * @author mian
   */
  private Map<Integer, Integer> countAllChoices(List<Choice> choices) {
    Map<Integer, Integer> counts = new HashMap<>();

    for (Choice choice : choices) {
      countEventChoice(counts, choice.getChoice1());
      countEventChoice(counts, choice.getChoice2());
      countEventChoice(counts, choice.getChoice3());
      countEventChoice(counts, choice.getChoice4());
      countEventChoice(counts, choice.getChoice5());
      countEventChoice(counts, choice.getChoice6());
    }

    return counts;
  }

  /**
   * Counts a single event choice.
   *
   * @author mian
   */
  private void countEventChoice(Map<Integer, Integer> counts, String choice) {
    if (choice == null || choice.isEmpty()) {
      return;
    }
    try {
      int eventId = Integer.parseInt(choice.replaceAll("[^0-9]", ""));
      counts.merge(eventId, 1, Integer::sum);
    } catch (NumberFormatException e) {
      // Ignore invalid choices
    }
  }

  /**
   * Calculates how many additional workshops are needed based on demand and capacity.
   *
   * @author mian
   */
  private int calculateAdditionalWorkshops(int demand, int capacity) {
    if (demand <= capacity) {
      return 1;
    }
    return (int) Math.ceil((double) (demand - capacity) / capacity) + 1;
  }

  /**
   * Counts the number of choices for a specific event.
   *
   * @author mian
   */
  public int countChoicesForEvent(List<Choice> choices, int eventId) {
    Map<Integer, Integer> counts = countAllChoices(choices);
    return counts.getOrDefault(eventId, 0);
  }

  /**
   * Saves workshop demand data to the database.
   *
   * @param workshopDemand Map of event IDs to demand count
   * @return true if saving was successful, false otherwise
   * @author mian
   */
  public boolean saveDemandToDatabase(Map<Integer, Integer> workshopDemand) {
    String sql = "MERGE INTO workshop_demand (event_id, demand) VALUES (?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      for (Map.Entry<Integer, Integer> entry : workshopDemand.entrySet()) {
        stmt.setInt(1, entry.getKey());
        stmt.setInt(2, entry.getValue());
        stmt.addBatch();
      }

      stmt.executeBatch();
      conn.commit();
      System.out.println("Workshop demand saved to database successfully");
      return true;
    } catch (SQLException e) {
      System.err.println("Error saving workshop demand: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Loads workshop demand data from the database.
   *
   * @return Map of event IDs to demand count
   * @author mian
   */
  public Map<Integer, Integer> loadDemandFromDatabase() {
    String sql = "SELECT event_id, demand FROM workshop_demand";
    Map<Integer, Integer> workshopDemand = new HashMap<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        int eventId = rs.getInt("event_id");
        int demand = rs.getInt("demand");
        workshopDemand.put(eventId, demand);
      }

      System.out.println(
          "Loaded " + workshopDemand.size() + " workshop demand entries from database");
    } catch (SQLException e) {
      System.err.println("Error loading workshop demand: " + e.getMessage());
      e.printStackTrace();
    }

    return workshopDemand;
  }

  /**
   * Gets all workshop demand records from the database as WorkshopDemand objects.
   *
   * @return List of WorkshopDemand objects
   * @author mian
   */
  public List<WorkshopDemand> getAllWorkshopDemands() {
    String sql = "SELECT wd.event_id, wd.demand, e.company, e.subject " +
        "FROM workshop_demand wd " +
        "JOIN events e ON wd.event_id = e.id";
    List<WorkshopDemand> demands = new ArrayList<>();

    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        int eventId = rs.getInt("event_id");
        int demand = rs.getInt("demand");
        String companyName = rs.getString("company");
        String subject = rs.getString("subject");
        demands.add(new WorkshopDemand(eventId, demand, companyName, subject));
      }

      System.out.println("Loaded " + demands.size() + " workshop demands as objects");
    } catch (SQLException e) {
      System.err.println("Error loading workshop demands: " + e.getMessage());
      e.printStackTrace();
    }

    return demands;
  }

}