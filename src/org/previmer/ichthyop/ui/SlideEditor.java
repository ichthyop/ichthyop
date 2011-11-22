package org.previmer.ichthyop.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.DecimalFormat;
import javax.swing.table.TableCellEditor;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

public class SlideEditor extends AbstractCellEditor implements TableCellEditor, ActionListener{
    
        final DecimalFormat df = new DecimalFormat("0.###");
        final JTextField text = new JTextField(20);
        final DoubleJSlider slider = new DoubleJSlider(0, 100, 0, 100);
                
        public SlideEditor(){
            ToolTipManager.sharedInstance().setInitialDelay(0);
            slider.addChangeListener(new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    text.setText(df.format(slider.getScaledValue()));
                    slider.setToolTipText(Double.toString((slider.getScaledValue())));
                }
            });
            text.addKeyListener(new KeyAdapter(){
                @Override
                public void keyReleased(KeyEvent ke) {
                    String typed = text.getText();
                   slider.setValue(Integer.parseInt(typed));
                    if(!typed.matches("\\d+(\\.\\d*)?")) {
                        return;
                    }
                    double value = Double.parseDouble(typed)*slider.scale;
                    slider.setValue((int)value);
                    
                }
            });            
        }

         public Object getCellEditorValue() {
                  return text.getText();
         }

         public void actionPerformed(ActionEvent e) {
             String typed = text.getText();
                    slider.setValue(0);
                    if(!typed.matches("\\d+(\\.\\d*)?")) {
                        return;
                    }
                    double value = Double.parseDouble(typed)*slider.scale;
                    slider.setValue((int)value);               
                    
         }

         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return slider;
         }

}

class DoubleJSlider extends JSlider {

    final int scale;

    public DoubleJSlider(int min, int max, int value, int scale) {
        super(min, max, value);
        this.scale = scale;
    }

    public double getScaledValue() {
        return ((double)super.getValue()) / this.scale;
    }
}