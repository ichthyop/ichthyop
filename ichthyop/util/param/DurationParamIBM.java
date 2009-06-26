package ichthyop.util.param;

import java.text.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class DurationParamIBM
    extends Parameter implements FocusListener {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private static final int ONE_SECOND = 1000;
  private static final int ONE_MINUTE = 60 * ONE_SECOND;
  private static final int ONE_HOUR = 60 * ONE_MINUTE;
  private static final long ONE_DAY = 24 * ONE_HOUR;

  //private long default_value, value, valMin, valMax, valueTmp;
  private MaskFormatter dtFormat;

  //----------------------------------------------------------------------------
  public DurationParamIBM(String title, String str,
      String unit, boolean enabled) {

    super.setAttributes(title, unit, enabled);
    try {
      txtField = new JFormattedTextField(dtFormat =
          new MaskFormatter("####/##:##"));
      str = dtFormat.valueToString(str);
      default_value = stringToDuration(str);
    }
    catch (ParseException ex) {
      System.out.println("Duration format error");
      default_value = 0L;
    }
    txtField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    valMin = 0L;
    valMax = 9999L * ONE_DAY + 99L * ONE_HOUR + 99L * ONE_MINUTE;
    //
  }

  //----------------------------------------------------------------------------
  void init() {
    value = default_value;
    txtField.setValue(durationToString(value.longValue()));
    txtField.addFocusListener(this);
    txtField.setPreferredSize(new Dimension(80, 20));
    setHasChanged(false);
  }

  //----------------------------------------------------------------------------
  private String durationToString(long time) {

    //System.out.println("D2S " + time);

    String str;
    int nbDays, nbHours, nbMin;
    NumberFormat nbFormat = NumberFormat.getInstance();
    nbFormat.setParseIntegerOnly(true);
    nbFormat.setGroupingUsed(false);
    nbDays = (int) (time / ONE_DAY);
    time -= nbDays * ONE_DAY;
    nbHours = (int) (time / ONE_HOUR);
    time -= nbHours * ONE_HOUR;
    nbMin = (int) (time / ONE_MINUTE);

    nbFormat.setMinimumIntegerDigits(4);
    nbFormat.setMaximumIntegerDigits(4);
    str = nbFormat.format(nbDays);
    nbFormat.setMinimumIntegerDigits(2);
    nbFormat.setMaximumIntegerDigits(2);
    str += "/" + nbFormat.format(nbHours) + ":" + nbFormat.format(nbMin);

    return (str);
  }

  //----------------------------------------------------------------------------
  private long stringToDuration(String str) {
    long time = 0L;
    NumberFormat nbFormat = NumberFormat.getInstance();
    nbFormat.setParseIntegerOnly(true);
    nbFormat.setGroupingUsed(false);
    try {
      time = nbFormat.parse(str.substring(str.indexOf(":") + 1)).longValue() *
          ONE_MINUTE
          +
          nbFormat.parse(str.substring(str.indexOf("/") + 1, str.indexOf(":"))).
          longValue() *
          ONE_HOUR
          +
          nbFormat.parse(str.substring(0, str.indexOf("/"))).longValue() *
          ONE_DAY;
    }
    catch (ParseException ex) {
    }
    //System.out.println("S2D " + time);
    return time;
  }

  //----------------------------------------------------------------------------
  public void setValue(long value) {
    this.value = value;
    txtField.setValue(durationToString(value));
  }

  //----------------------------------------------------------------------------
  public void focusGained(FocusEvent e) {
    valTmp = stringToDuration(txtField.getText());
  }

  //----------------------------------------------------------------------------
  public void focusLost(FocusEvent e) {
    value = stringToDuration(txtField.getText());
    if (value.longValue() < valMin.longValue()
        | value.longValue() > valMax.longValue()) {
      value = default_value;
      txtField.setValue(durationToString(default_value.longValue()));
    }
    setHasChanged(!value.equals(valTmp));
  }
}
