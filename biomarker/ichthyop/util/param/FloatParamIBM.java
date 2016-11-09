package ichthyop.util.param;

import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FloatParamIBM
    extends Parameter implements FocusListener {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private NumberFormat nbFormat;

  //----------------------------------------------------------------------------
  public FloatParamIBM(String title, float default_value,
      String unit, boolean enabled) {

    super.setAttributes(title, unit, enabled);
    txtField = new JFormattedTextField(nbFormat =
        NumberFormat.getInstance(Locale.
        getDefault()));
    this.default_value = default_value;
    this.valMin = Float.MIN_VALUE;
    this.valMax = Float.MAX_VALUE;
  }

  //----------------------------------------------------------------------------
  void init() {
    value = default_value;
    txtField.setValue(value.floatValue());
    txtField.setPreferredSize(new Dimension(50, 20));
    txtField.addFocusListener(this);
    setHasChanged(false);
  }

  //----------------------------------------------------------------------------
  public void setFormatPolicy(int minDigits, int maxDigits, int minFraction,
      int maxFraction) {
    nbFormat.setMinimumIntegerDigits(minDigits);
    nbFormat.setMaximumIntegerDigits(maxDigits);
    nbFormat.setMinimumFractionDigits(minFraction);
    nbFormat.setMaximumFractionDigits(maxFraction);
  }

  //----------------------------------------------------------------------------
  public void focusGained(FocusEvent e) {
    try {
      valTmp = nbFormat.parse(txtField.getText()).floatValue();
    }
    catch (ParseException ex) {}

  }

  //----------------------------------------------------------------------------
  public void focusLost(FocusEvent e) {
    try {
      value = nbFormat.parse(txtField.getText()).floatValue();
    }
    catch (ParseException ex) {}
    if (value.floatValue() < valMin.floatValue()
        | value.floatValue() > valMax.floatValue()) {
      txtField.setValue(default_value.floatValue());
    }
    setHasChanged(!value.equals(valTmp));
  }
}
