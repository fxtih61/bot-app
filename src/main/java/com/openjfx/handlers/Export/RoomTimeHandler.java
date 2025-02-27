package com.openjfx.handlers.Export;

import com.openjfx.services.EventService;

/**
 * Handler for exporting RoomTime data to an Excel file.
 */
public class RoomTimeHandler {

  private final EventService eventService;

  /**
   * Constructs a RoomTimeHandler with the specified EventService.
   *
   * @param eventService the Event service to use for exporting data
   */
  public RoomTimeHandler(EventService eventService) {
    this.eventService = eventService;
  }

  /**
   * Gets the columns to be displayed in the table.
   *
   * @return a list of pairs where each pair contains the column name and the corresponding property
   * name
   */

}
