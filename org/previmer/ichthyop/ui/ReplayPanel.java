/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class ReplayPanel extends JPanel {

    private List<BufferedImage> pictures = null;
    private List<String> pictureNames = null;
    private boolean loadingDone = false;
    private Thread picturesFinder = null;
    private Timer faderTimer = null;
    private float veilAlphaLevel = 0.0f;
    private float alphaLevel = 0.0f;
    private int index;
    private FocusGrabber focusGrabber;
    private File folder;
    private Icon bgIcon;
    private boolean damaged = true;

    public ReplayPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        addComponentListener(new DamageManager());

        initInputListeners();
        addInputListeners();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                veilAlphaLevel));
        super.paintChildren(g);
        g2.setComposite(oldComposite);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (pictures == null || pictures.isEmpty()) {
            if (bgIcon != null) {
                bgIcon.paintIcon(this, g, (getWidth() - bgIcon.getIconWidth()) / 2, (getHeight() - bgIcon.getIconHeight()) / 2);
            }
        }

        if (!loadingDone && faderTimer == null) {
            return;
        }

        Insets insets = getInsets();

        int x = insets.left;
        int y = insets.top;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComposite = g2.getComposite();

        if (pictures != null && !pictures.isEmpty()) {
            try {
                BufferedImage img = pictures.get(getIndex());
                //setSize(img.getWidth(), img.getHeight());
                //x = x + (getWidth() - img.getWidth()) / 2;
                //y = y + (getHeight() - img.getHeight()) / 2;
                g2.drawImage(img, x, y, getWidth(), getHeight(), null);
            } catch (Exception ex) {
            }
        }

        g2.setComposite(oldComposite);
    }

    private void startFader() {
        faderTimer = new Timer(35, new FaderAction());
        faderTimer.start();
    }

    private void addInputListeners() {
        addMouseListener(focusGrabber);
    }

    private void initInputListeners() {
        // input listeners are all stateless
        // hence they can be instantiated once
        focusGrabber = new FocusGrabber();
    }

    private void removeInputListeners() {
        removeMouseListener(focusGrabber);
    }

    public int getIndexMax() {
        return pictures.size() - 1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        repaint();
    }

    void setFolder(File folder) {
        this.folder = folder;
        pictures = new ArrayList();
        pictureNames = new ArrayList();
        if (null != folder && folder.isDirectory()) {
            picturesFinder = new Thread(new PicturesFinderThread(folder, "*.png"));
            picturesFinder.start();
        } else {
            bgIcon = IchthyopApp.getApplication().getMainView().getResourceMap().getIcon("step.Animation.bgicon");
            damaged = true;
            repaint();
        }
    }

    public String getTime() {
        try {
            String[] tokens = pictureNames.get(index).split("_");
            String date = tokens[tokens.length - 1];
            date = date.substring(0, date.indexOf(".png"));
            String[] dateToken = date.split("-");
            StringBuffer time = new StringBuffer("Year ");
            time.append(dateToken[0].substring(3));
            time.append(" Month ");
            time.append(dateToken[1]);
            time.append(" Day ");
            time.append(dateToken[2]);
            time.append(" - ");
            time.append(dateToken[3]);
            time.append(":");
            time.append(dateToken[4]);
            return time.toString();
        } catch (Exception e) {
            return "Time";
        }
    }

    public File getFolder() {
        return folder;
    }

    private class PicturesFinderThread implements Runnable {

        String strFilter;
        File folder;

        PicturesFinderThread(File folder, String strFilter) {
            this.strFilter = strFilter;
            this.folder = folder;
        }

        public void run() {

            try {
                MetaFilenameFilter filter = new MetaFilenameFilter(strFilter);

                List<File> files = Arrays.asList(folder.listFiles(filter));
                Collections.sort(files);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    BufferedImage image = ImageIO.read(file);
                    pictures.add(image);
                    pictureNames.add(file.getName());
                    if (i > 2) {
                        damaged = true;
                        setIndex(0);
                        startFader();
                    }

                }
            } catch (IOException e) {
            }

            loadingDone = true;
        }
    }

    private class FaderAction implements ActionListener {

        private long start = 0;

        private FaderAction() {
            alphaLevel = 0.0f;
        }

        public void actionPerformed(ActionEvent e) {
            if (start == 0) {
                start = System.currentTimeMillis();
            }

            alphaLevel = (System.currentTimeMillis() - start) / 500.0f;

            if (alphaLevel > 1.0f) {
                alphaLevel = 1.0f;
                faderTimer.stop();
            }

            repaint();
        }
    }

    void initAnim() {

        removeInputListeners();
    }

    void endAnim() {

        addInputListeners();
    }

    private class FocusGrabber extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            requestFocus();
        }
    }

    private class DamageManager extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            damaged = true;
        }
    }
}
