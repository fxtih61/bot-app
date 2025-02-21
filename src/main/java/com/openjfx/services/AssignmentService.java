package com.openjfx.services;

import com.openjfx.models.Choice;
import com.openjfx.models.Event;
import com.openjfx.models.Room;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for assigning students to events based on their choices and priorities.
 * The service handles the logic for distributing students across events while respecting maximum
 * capacity constraints and student preferences.
 *
 * <p>The assignment process follows these steps:
 * <ol>
 *   <li>Load student choices, events, and room data from Excel files</li>
 *   <li>Initialize empty slots for each event</li>
 *   <li>Assign students to their first choice events where possible</li>
 *   <li>Assign remaining students to their subsequent choices (2-6) if space is available</li>
 * </ol>
 *
 * <p>Example usage:
 * <pre>
 * AssignmentService service = new AssignmentService(choiceService, eventService, roomService);
 * service.runAssignment("choices.xlsx", "events.xlsx", "rooms.xlsx");
 * </pre>
 */
public class AssignmentService {

  private final ChoiceService choiceService;
  private final EventService eventService;
  private final RoomService roomService;

  // Track assignments: <EventID, List<StudentChoices>>
  private final Map<Integer, List<Choice>> assignments = new HashMap<>();

  /**
   * Constructs a new AssignmentService with the required dependencies.
   *
   * @param choiceService service for handling student choices data
   * @param eventService  service for handling event data
   * @param roomService   service for handling room data
   */
  public AssignmentService(ChoiceService choiceService, EventService eventService,
      RoomService roomService) {
    this.choiceService = choiceService;
    this.eventService = eventService;
    this.roomService = roomService;
  }

  /**
   * Executes the assignment process using data from the specified Excel files.
   *
   * @param choicesPath path to the Excel file containing student choices
   * @param eventsPath  path to the Excel file containing event information
   * @param roomsPath   path to the Excel file containing room information
   * @throws IOException if there is an error reading the Excel files
   */
  public void runAssignment(String choicesPath, String eventsPath, String roomsPath)
      throws IOException {
    List<Choice> choices = choiceService.loadFromExcel(choicesPath);
    List<Event> events = eventService.loadFromExcel(eventsPath);
    List<Room> rooms = roomService.loadFromExcel(roomsPath);

    // Initializes empty assignment slots for each event.
    initializeEventslots(events);

    // Assigns students to their first choice events where possible.
    assignFirstChoices(choices, events);

    // Assigns remaining students to their subsequent choices (2-6) if space is available.
    assignRemainingChoices(choices, events);

    //print the assignments
    for (Map.Entry<Integer, List<Choice>> entry : assignments.entrySet()) {
      System.out.println("Event ID: " + entry.getKey());
      for (Choice choice : entry.getValue()) {
        System.out.println(choice);
      }
    }

    //TODO: generate outputs(Schedule, Attendance list, etc.)
  }

  /**
   * Initializes empty assignment slots for each event.
   *
   * @param events list of events to initialize slots for
   */
  private void initializeEventslots(List<Event> events) {
    for (Event event : events) {
      assignments.put(event.getId(), new ArrayList<>());
    }
  }

  /**
   * Assigns students to their first choice events where possible.
   *
   * @param choices list of student choices
   * @param events  list of available events
   */
  private void assignFirstChoices(List<Choice> choices, List<Event> events) {
    for (Choice choice : choices) {
      String firstChoice = choice.getChoice1();
      if (firstChoice.isEmpty()) {
        continue;
      }

      Event event = findEventByChoice(firstChoice, events);
      if (event == null || hasCapacity(event)) {
        continue;
      }

      assignments.get(event.getId()).add(choice);
    }
  }

  /**
   * Finds an event by the choice string provided.
   *
   * @param choice the choice string to search for
   * @param events the list of events to search
   * @return the event matching the choice, or null if not found
   */
  private Event findEventByChoice(String choice, List<Event> events) {
    try {
      int eventId = Integer.parseInt(choice.replaceAll("[^0-9]", ""));
      return events.stream()
          .filter(e -> e.getId() == eventId)
          .findFirst()
          .orElse(null);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Checks if an event has reached its maximum capacity.
   *
   * @param event the event to check
   * @return true if the event is full, false otherwise
   */
  private boolean hasCapacity(Event event) {
    int current = assignments.get(event.getId()).size();
    return current >= event.getMaxParticipants();
  }

  /**
   * Assigns remaining students to their subsequent choices (2-6) if space is available.
   *
   * @param choices list of student choices
   * @param events  list of available events
   */
  private void assignRemainingChoices(List<Choice> choices, List<Event> events) {
    for (int priority = 2; priority <= 6; priority++) {
      for (Choice choice : choices) {
        String choiceStr = getChoiceByPriority(choice, priority);
        if (choiceStr.isEmpty()) {
          continue;
        }

        Event event = findEventByChoice(choiceStr, events);
        if (event == null || hasCapacity(event)) {
          continue;
        }

        if (!isAlreadyAssigned(choice, event)) {
          assignments.get(event.getId()).add(choice);
        }
      }
    }
  }

  /**
   * Returns the choice string based on the priority provided.
   *
   * @param choice   the choice object to extract from
   * @param priority the priority of the choice to return
   * @return the choice string based on the priority
   */
  private String getChoiceByPriority(Choice choice, int priority) {
    switch (priority) {
      case 2:
        return choice.getChoice2();
      case 3:
        return choice.getChoice3();
      case 4:
        return choice.getChoice4();
      case 5:
        return choice.getChoice5();
      case 6:
        return choice.getChoice6();
      default:
        return "";
    }
  }

  /**
   * Checks if a student is already assigned to an event.
   *
   * @param student the student to check
   * @param event   the event to check
   * @return true if the student is already assigned, false otherwise
   */
  private boolean isAlreadyAssigned(Choice student, Event event) {
    return assignments.get(event.getId()).contains(student);
  }
}
