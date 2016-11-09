package ichthyop.util.param;

import java.text.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class IntegerParamIBM
    extends Parameter implements FocusListener {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private NumberFormat nbFormat;

  //----------------------------------------------------------------------------
  public IntegerParamIBM(String title, int default_value,
      String unit, boolean enabled) {

    super.setAttributes(title, unit, enabled);
    txtField = new JFormattedTextField(nbFormat =
        NumberFormat.getInstance());
    nbFormat.setParseIntegerOnly(true);
    nbFormat.setGroupingUsed(false);
    this.default_value = default_value;
    this.valMin = 0;
    this.valMax = Integer.MAX_VALUE;
  }

  //----------------------------------------------------------------------------
  void init() {
    value = default_value;
    txtField.setValue(value.intValue());
    txtField.addFocusListener(this);
    txtField.setPreferredSize(new Dimension(50, 20));
    setHasChanged(false);
  }

  //----------------------------------------------------------------------------
  public void setFormatPolicy(int minDigits, int maxDigits) {
    nbFormat.setParseIntegerOnly(true);
    nbFormat.setMinimumIntegerDigits(minDigits);
    nbFormat.setMaximumIntegerDigits(maxDigits);
  }

//----------------------------------------------------------------------------
  public void focusGained(FocusEvent e) {
    txtField.selectAll();
    try {
      valTmp = nbFormat.parse(txtField.getText()).intValue();
      //System.out.println("gained " + valTmp);
    }
    catch (ParseException ex) {
    }

  }

  //----------------------------------------------------------------------------
  public void focusLost(FocusEvent e) {
    try {
      value = nbFormat.parse(txtField.getText()).intValue();
      //System.out.println("lost " + value);
    }
    catch (ParseException ex) {
    }
    if (value.intValue() < valMin.intValue()
        | value.intValue() > valMax.intValue()) {
      txtField.setValue(default_value.intValue());
    }
    setHasChanged(!value.equals(valTmp));
    //System.out.println("changed ? " + hasChanged());
  }
}
