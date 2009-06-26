package ichthyop.util.param;

import java.awt.*;
import javax.swing.*;

abstract class Parameter
    extends JComponent {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private String title;
  public JFormattedTextField txtField;
  String unit;
  private boolean enabled;
  private JLabel lblTitle;
  private JLabel lblUnit;
  private boolean HAS_CHANGED;

  Number default_value, value, valMin, valMax, valTmp;

  //----------------------------------------------------------------------------
  abstract void init();

  //----------------------------------------------------------------------------
  public void setAttributes(String title, String unit, boolean enabled) {
    this.title = title;
    this.unit = unit;
    this.enabled = enabled;
  }

//------------------------------------------------------------------------------
  public void setUnit(String unit) {
    this.unit = unit;
    lblUnit.setText(unit);
  }

//------------------------------------------------------------------------------
  public JPanel createGUI() {

    JPanel pnlParam = new JPanel(new BorderLayout(10, 5));
    lblTitle = new JLabel(title);
    lblUnit = new JLabel(unit);
    setEnabled(enabled);
    //txtField.setPreferredSize(new Dimension(50, 20));
    txtField.setHorizontalAlignment(JTextField.RIGHT);

    pnlParam.add(lblTitle, BorderLayout.WEST);
    pnlParam.add(txtField);
    pnlParam.add(lblUnit, BorderLayout.EAST);

    init();

    return pnlParam;
  }

  //----------------------------------------------------------------------------
  public Number getValue() {
    return value;
  }

  //----------------------------------------------------------------------------
  public void setValue(Number value) {
    this.value = value;
    txtField.setValue(value);
  }

  //----------------------------------------------------------------------------
  public void setDefaultValue(Number default_value) {
    this.default_value = default_value;
  }

  //----------------------------------------------------------------------------
  public Number getDefaultValue() {
    return default_value;
  }

  //----------------------------------------------------------------------------
  public void setBoundary(Number valMin, Number valMax) {
    this.valMin = valMin;
    this.valMax = valMax;
  }

  //----------------------------------------------------------------------------
  public void setEnabled(boolean enabled) {

    txtField.setEnabled(enabled);
    if (! (lblTitle == null)) {
      lblTitle.setEnabled(enabled);
      lblUnit.setEnabled(enabled);
    }
    this.enabled = enabled;
  }

  //----------------------------------------------------------------------------
  public void setHasChanged(boolean bln) {
    HAS_CHANGED = bln;
  }

  //----------------------------------------------------------------------------
  public boolean hasChanged() {
    return HAS_CHANGED;
  }
}
