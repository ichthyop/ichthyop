package ichthyop.util.calendar;

import java.util.*;

public class ClimatoCalendar
    extends Calendar {

  private static final int ONE_SECOND = 1000;
  private static final int ONE_MINUTE = 60 * ONE_SECOND;
  private static final int ONE_HOUR = 60 * ONE_MINUTE;
  private static final long ONE_DAY = 24 * ONE_HOUR;
  private static final long ONE_MONTH = 30 * ONE_DAY;
  private static final long ONE_YEAR = 12 * ONE_MONTH;

  //----------------------------------------------------------------------------
  public ClimatoCalendar() {
    fields = new int[FIELD_COUNT];
    set(YEAR, 1);
    set(MONTH, 1);
    set(DAY_OF_MONTH, 1);
    set(HOUR_OF_DAY, 0);
    set(MINUTE, 0);
    set(SECOND, 0);
    //time = 0;

  }

  //----------------------------------------------------------------------------
  protected void computeFields() {

    int year, month, dayOfMonth, dayOfYear;
    long timeInDay = millisToDay(time);
    //System.out.println("cf timeInDay " + timeInDay);

    dayOfYear = (int) timeInDay % 360; // zero-based day of year
    year = (int)(timeInDay / 360L);
    month = dayOfYear / 30;
    dayOfMonth = dayOfYear - month * 30 + 1;
    //System.out.println("cf dayOfYear " + dayOfYear);

    set(YEAR, ++year);
    set(MONTH, month); // 0-based
    set(DAY_OF_MONTH, dayOfMonth);
    set(DAY_OF_YEAR, ++dayOfYear); // converted to 1-based

    int millisInDay = (int) (time - (timeInDay * ONE_DAY));
    if (millisInDay < 0) {
      millisInDay += ONE_DAY;
    }

    set(MILLISECOND, millisInDay % 1000);
    millisInDay /= 1000;
    set(SECOND, millisInDay % 60);
    millisInDay /= 60;
    set(MINUTE, millisInDay % 60);
    millisInDay /= 60;
    set(HOUR_OF_DAY, millisInDay);

  }

  //----------------------------------------------------------------------------
  protected void computeTime() {

    long time2Day = (long)(fields[DAY_OF_MONTH] - 1 + fields[MONTH] * 30 + (fields[YEAR] - 1) * 360);
    long millis = dayToMillis(time2Day);
    int millisInDay = 1000 * (fields[SECOND] + 60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
    time = millis + millisInDay;

  }

  //----------------------------------------------------------------------------
  private static final long millisToDay(long millis) {
    return (millis / ONE_DAY);
  }

  //----------------------------------------------------------------------------
  private static final long dayToMillis(long day) {
    return day * ONE_DAY;
  }

  //----------------------------------------------------------------------------
  public int getGreatestMinimum(int field) {
    return 0;
  }

  //----------------------------------------------------------------------------
  public int getLeastMaximum(int field) {
    return 0;
  }

  //----------------------------------------------------------------------------
  public int getMaximum(int field) {
    return 0;
  }

  //----------------------------------------------------------------------------
  public int getMinimum(int field) {
    return 0;
  }

  //----------------------------------------------------------------------------
  public void add(int field, int amount) {
  }

  //----------------------------------------------------------------------------
  public void roll(int field, boolean up) {
  }

}
