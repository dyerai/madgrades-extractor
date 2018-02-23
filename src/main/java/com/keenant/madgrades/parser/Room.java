package com.keenant.madgrades.parser;

import com.keenant.madgrades.data.RoomBean;
import java.util.Objects;

/**
 *
 */
public class Room {
  public static Room ONLINE = new Room("ONLINE", null);
  public static Room OFF_CAMPUS = new Room("OFF CAMPUS", null);
  public static Room NONE = new Room("NONE", null);

  private final String facilityCode;
  private final String roomCode;

  public Room(String facilityCode, String roomCode) {
    this.facilityCode = facilityCode;
    this.roomCode = roomCode;
  }

  public static Room parse(String roomStr) {
    if (roomStr.equals("ONLINE"))
      return ONLINE;
    else if (roomStr.equals("OFF CAMPUS"))
      return OFF_CAMPUS;
    else if (roomStr.equals(""))
      return NONE;

    String[] split = roomStr.split(" ");

    if (split.length == 1) {
      String firstFive = roomStr.substring(0, 5);
      String room = roomStr.substring(5, roomStr.length());
      return new Room(firstFive, room);
    }

    return new Room(split[0], split[1]);
  }

  @Override
  public String toString() {
    if (roomCode != null)
      return facilityCode + "-" + roomCode;
    return facilityCode;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Room) {
      Room other = (Room) o;

      return Objects.equals(facilityCode, other.facilityCode) &&
          Objects.equals(roomCode, other.roomCode);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(facilityCode, roomCode);
  }

  public RoomBean toBean() {
    return new RoomBean(facilityCode, roomCode);
  }
}