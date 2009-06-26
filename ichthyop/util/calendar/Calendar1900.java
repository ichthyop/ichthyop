package ichthyop.util.calendar;

import java.util.*;

public class Calendar1900
    extends Calendar {

  int[] epoch_fields;
  private static final int ONE_SECOND = 1000;
  private static final int ONE_MINUTE = 60 * ONE_SECOND;
  private static final int ONE_HOUR = 60 * ONE_MINUTE;
  private static final long ONE_DAY = 24 * ONE_HOUR;
  private static final long ONE_WEEK = 7 * ONE_DAY;

  private static final int NUM_DAYS[]
      = {
      0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334}; // 0-based, for day-in-year
  private static final int LEAP_NUM_DAYS[]
      = {
      0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335}; // 0-based, for day-in-year
  private static final int MONTH_LENGTH[]
      = {
      31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based
  private static final int LEAP_MONTH_LENGTH[]
      = {
      31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based

  //----------------------------------------------------------------------------
  public Calendar1900() {
    this(1900, JANUARY, 1, 0, 0, 0);
  }

  //----------------------------------------------------------------------------
  public Calendar1900(int year, int month, int day, int hour, int min, int sec) {
    epoch_fields = new int[FIELD_COUNT];
    fields = new int[FIELD_COUNT];
    setEpoch(YEAR, year);
    setEpoch(MONTH, month);
    setEpoch(DAY_OF_MONTH, day);
    setEpoch(HOUR_OF_DAY, hour);
    setEpoch(MINUTE, min);
    setEpoch(SECOND, sec);

    set(YEAR, year);
    set(MONTH, month);
    set(DAY_OF_MONTH, day);
    set(HOUR_OF_DAY, hour);
    set(MINUTE, min);
    set(SECOND, sec);

    //time = 0L;
  }

  //----------------------------------------------------------------------------
  public void setEpoch(int field, int value) {
    epoch_fields[field] = value;
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
  protected void computeFields() {
    int rawYear, year, month, dayOfMonth, dayOfYear;
    boolean isLeap;
    long timeInDay = millisToDay(time);
    //System.out.println("cf timeInDay " + timeInDay);
    int n400, n4, n1;

    timeInDay += isLeap(epoch_fields[YEAR])
        ? LEAP_NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1
        : NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1;

    n400 = (int) (timeInDay / 146097);
    dayOfYear = (int) (timeInDay % 146097);
    n4 = dayOfYear / 1461;
    dayOfYear %= 1461;
    n1 = dayOfYear / 365;
    dayOfYear %= 365; // zero-based day of year
    rawYear = 400 * n400 + 4 * n4 + n1;
    //System.out.println("n400 " + n400 + " n4 " + n4 + " n1 " + n1);
    //System.out.println("raw " + rawYear);
    //System.out.println("cf dayOfYear " + dayOfYear);

    rawYear += epoch_fields[YEAR];

    isLeap = isLeap(rawYear);

    /*dayOfYear += isLeap(epoch_fields[YEAR])
        ? LEAP_NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1
        : NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1;
*/
    if (dayOfYear > (isLeap ? 365 : 364)) {
      dayOfYear -= (isLeap ? 366 : 365);
      rawYear++;
    }

    int correction = 0;
    int march1 = isLeap ? 60 : 59; // zero-based DAY for March 1
    if (dayOfYear >= march1) {
      correction = isLeap ? 1 : 2;
    }

    month = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
    dayOfMonth = dayOfYear -
        (isLeap ? LEAP_NUM_DAYS[month] : NUM_DAYS[month]) + 1; // one-based DOM

    year = rawYear;
    set(YEAR, year);
    set(MONTH, month); // 0-based
    set(DAY_OF_MONTH, dayOfMonth);
    set(DAY_OF_YEAR, ++dayOfYear);

    int millisInDay = (int) (time - (millisToDay(time) * ONE_DAY));
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
    set(AM_PM, millisInDay / 12); // Assume AM == 0
    set(HOUR, millisInDay % 12);

  }

  //----------------------------------------------------------------------------
  private boolean isLeap(int year) {
    return ( (year % 4 == 0) && ( (! (year % 100 == 0)) | (year % 400 == 0)));
  }

  //----------------------------------------------------------------------------
  protected void computeTime() {

    int n400, n4, n1;
    int yearOn = fields[YEAR];
    int monthOn = fields[MONTH];
    //int dayOn = fields[DAY_OF_MONTH];
    boolean isLeap = isLeap(yearOn);
    long time2Day = (long)(fields[DAY_OF_MONTH] - 1) ;
    time2Day += isLeap
        ? (long)(LEAP_NUM_DAYS[monthOn])
        : (long)(NUM_DAYS[monthOn]);

    int deltaYear = yearOn - epoch_fields[YEAR];

    n400 = (int) (deltaYear / 400);
    deltaYear = (int) (deltaYear % 400);
    n4 = (int)(deltaYear / 4);
    n1 = deltaYear % 4;
    //System.out.println("n4 " + n4 + " n1 " + n1);

    /*time2Day += n4 > 0
        ? (long)(n400 * 146097L + (n4 - 1) * 1461L + (n1 + 4) * 365L)
        : (long)(n400 * 146097L + n1 * 365L);*/
    time2Day += (long)(n400 * 146097L + n4 * 1461L + n1 * 365L);

    time2Day -= (epoch_fields[DAY_OF_MONTH] - 1);
    time2Day -= isLeap(epoch_fields[YEAR])
        ? LEAP_NUM_DAYS[epoch_fields[MONTH]]
        : NUM_DAYS[epoch_fields[MONTH]];

    //System.out.println("ct days from origin " + time2Day);

    long millis = dayToMillis(time2Day);
    int millisInDay = fields[MILLISECOND] + 1000 * (fields[SECOND] + 60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
    time = millis + millisInDay;
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
