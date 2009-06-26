package ichthyop.util;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

public class ViewPanel
    extends JPanel {

  private Image imgSimu;
  private int hi, wi;

  //----------------------------------------------------------------------------
  public ViewPanel(Image bImageSimu) {
    this.imgSimu = bImageSimu;
    hi = this.getHeight();
    wi = this.getWidth();
  }

  //----------------------------------------------------------------------------
  public void setImage(Image img) {
    if (hi != this.getHeight() | wi != this.getWidth()) {
      imgSimu = img.getScaledInstance(this.getWidth(), this.getHeight(),
                                         Image.SCALE_FAST);
    } else imgSimu = img;
    repaint();
  }

  //----------------------------------------------------------------------------
  public void paintComponent(Graphics g) {
    if (imgSimu != null) {
      g.drawImage(imgSimu, 0, 0, null);
    }
  }
}
