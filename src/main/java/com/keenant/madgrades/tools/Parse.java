package com.keenant.madgrades.tools;

import com.keenant.madgrades.data.Schedule;
import com.keenant.madgrades.entries.CourseNameEntry;
import com.keenant.madgrades.entries.DirEntry;
import com.keenant.madgrades.entries.GradesEntry;
import com.keenant.madgrades.entries.SectionEntry;
import com.keenant.madgrades.entries.SectionGradesEntry;
import com.keenant.madgrades.entries.SubjectEntry;
import com.keenant.madgrades.utils.DaySchedule;
import com.keenant.madgrades.utils.GradeType;
import com.keenant.madgrades.utils.Room;
import com.keenant.madgrades.utils.SectionType;
import com.keenant.madgrades.utils.TimeSchedule;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parse {
  /**
   * Parse a row to a stream of dir report entries.
   *
   * @param row the table row
   * @return the stream of entries processed from the row
   */
  public static Stream<DirEntry> dirEntry(List<String> row) {
    String joined = row.stream().collect(Collectors.joining());

    // some rows have nothing in them
    if (joined.length() == 0)
      return Stream.empty();

    // extract subject code
    if (joined.contains("SUBJECT:")) {
      String subjectCode = joined.substring(joined.length() - 4, joined.length() - 1);
      return Stream.of(new SubjectEntry(subjectCode));
    }

    int courseNumber;

    // at this point, we just try parsing an integer. if it
    // works then we know we have a section
    try {
      courseNumber = Integer.parseInt(row.get(1));
    } catch (NumberFormatException e) {
      return Stream.empty();
    }

    SectionType sectionType = SectionType.valueOf(row.get(2));
    int sectionNumber = Integer.parseInt(row.get(3));
    TimeSchedule times = TimeSchedule.parse(row.get(5));
    DaySchedule days = DaySchedule.parse(row.get(6));
    Room rooms = Room.parse(row.get(7));

    Integer instructorId = null;
    String instructorName = null;

    // rarely there is no instructor
    if (row.get(10).length() > 0) {
      instructorId = Integer.parseInt(row.get(10));
    }

    if (row.get(11).length() > 0) {
      instructorName = row.get(11);
    }

    Schedule schedule = new Schedule(times, days);

    SectionEntry sectionEntry = new SectionEntry(
        courseNumber,
        sectionType,
        sectionNumber,
        schedule,
        rooms,
        instructorId,
        instructorName
    );

    return Stream.of(sectionEntry);
  }

  /**
   * Parse a row to a stream of grade entries.
   *
   * NOTE: Course names are separate from grade entries, they arrive AFTER the sequence of
   * section which the name correspond to.
   *
   * @param row the table row
   * @return the stream of entries processed from the row
   */
  public static Stream<GradesEntry> gradeEntry(List<String> row) {
    String joined = row.stream().collect(Collectors.joining());

    // some rows have nothing in them
    if (joined.length() == 0)
      return Stream.empty();

    // line which has this also has the subject code
    if (joined.contains("GradesGPA")) {
      String subjectCode = joined.substring(0, 3);
      return Stream.of(new SubjectEntry(subjectCode));
    }

    String courseName = row.get(0);

    int courseNumber;
    int sectionNumber;

    try {
      courseNumber = Integer.parseInt(row.get(1));

      // note: sometimes section number is 00A, we don't add those grades
      sectionNumber = Integer.parseInt(row.get(2));
    } catch (NumberFormatException e) {
      // no int present, but "Course Total" is, that means we just got the name of the course
      if (!courseName.isEmpty() && joined.contains("Total")) {
        return Stream.of(new CourseNameEntry(courseName));
      }
      return Stream.empty();
    }

    double gradeCount = (double) Integer.parseInt(row.get(3));

    Map<GradeType, Integer> grades = new LinkedHashMap<>();

    for (int i = 0; i < GradeType.values().length; i++) {
      GradeType gradeType = GradeType.values()[i];
      double percent;

      try {
        percent = Double.parseDouble(row.get(i + 5));
      } catch (NumberFormatException e) {
        grades.put(gradeType, 0);
        continue;
      }

      int count = (int) Math.round(gradeCount * (percent / 100.0));
      grades.put(gradeType, count);
    }

    SectionGradesEntry entry = new SectionGradesEntry(courseNumber, sectionNumber, grades);

    if (courseName.isEmpty()) {
      return Stream.of(entry);
    }
    else {
      return Stream.of(entry, new CourseNameEntry(courseName));
    }
  }
}