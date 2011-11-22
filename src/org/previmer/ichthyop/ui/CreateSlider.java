package org.previmer.ichthyop.ui;

/**
 *
 * @author mariem
 */
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class CreateSlider{
  JSlider slider;
  JLabel label;
  public static void main(String[] args){
  CreateSlider cs = new CreateSlider();
  }

  public CreateSlider(){
  JFrame frame = new JFrame("Slider Frame");
  slider = new JSlider();
  slider.setValue(70);
  slider.addChangeListener(new MyChangeAction());
  label = new JLabel("Roseindia.net");
  JPanel panel = new JPanel();
  panel.add(slider);
  panel.add(label);
  frame.add(panel, BorderLayout.CENTER);
  frame.setSize(400, 400);
  frame.setVisible(true);
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public class MyChangeAction implements ChangeListener{
  public void stateChanged(ChangeEvent ce){
  int value = slider.getValue();
  String str = Integer.toString(value);
  label.setText(str);
  }
  }
}