package ichthyop.util.param;

import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class DateParamIBM
    extends Parameter implements FocusListener {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private SimpleDateFormat dtFormat;
  private MaskFormatter mskFormat;
  Calendar cld;

  //----------------------------------------------------------------------------
  public DateParamIBM(String title, Calendar cld,
      String unit, boolean enabled) {

    super.setAttributes(title, unit, enabled);
    this.cld = cld;
    dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    try {
      txtField = new JFormattedTextField(mskFormat =
          new MaskFormatter("####/##/## ##:##"));
    }
    catch (ParseException ex) {
    }
    dtFormat.setCalendar(cld);
    txtField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    this.default_value = 0; //cld.getTimeInMillis();
    this.valMin = 0L;
    this.valMax = Long.MAX_VALUE;
  }

  //----------------------------------------------------------------------------
  void init() {
    value = default_value;
    setValue(default_value);
    txtField.addFocusListener(this);
    txtField.setPreferredSize(new Dimension(120, 20));
    setHasChanged(false);
    //addInPanel();
  }

  //----------------------------------------------------------------------------
  public void setFormatPolicy(String pattern) {
    dtFormat.applyPattern(pattern);
  }

  //----------------------------------------------------------------------------
  public void setValue(long value) {
    this.value = value;
    cld.setTimeInMillis(value);
    txtField.setValue(dtFormat.format(cld.getTime()));
  }

  //----------------------------------------------------------------------------
  public void setCalendar(Calendar cld) {
    this.cld = cld;
    dtFormat.setCalendar(cld);
    this.unit = dtFormat.toPattern();
  }

//----------------------------------------------------------------------------
  public void focusGained(FocusEvent e) {
    try {
      dtFormat.parse(txtField.getText());
    }
    catch (ParseException ex) {
    }
    valTmp = cld.getTimeInMillis();

  }

  //----------------------------------------------------------------------------
  public void focusLost(FocusEvent e) {
    try {
      dtFormat.parse(txtField.getText());
    }
    catch (ParseException ex) {
    }
    value = cld.getTimeInMillis();
    //System.out.println("t " + value);
    setHasChanged(!value.equals(valTmp));
  }
}
