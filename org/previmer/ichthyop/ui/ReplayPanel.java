/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class ReplayPanel extends JPanel {

    private List<BufferedImage> avatars = null;
    private boolean loadingDone = false;
    private Thread picturesFinder = null;
    private Timer faderTimer = null;
    private float veilAlphaLevel = 0.0f;
    private float alphaLevel = 0.0f;
    private int avatarIndex;
    private boolean damaged = true;
    private FocusGrabber focusGrabber;
    private Snapshots snapshots;

    public ReplayPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        addComponentListener(new DamageManager());

        initInputListeners();
        addInputListeners();
    }

    /*@Override
    public Dimension getPreferredSize() {
        try {
            int width = avatars.get(0).getWidth() + getInsets().left + getInsets().right;
            int height = avatars.get(0).getHeight() + getInsets().bottom + getInsets().top;
            new Dimension(width, height);
        } catch (Exception ex) {
            Logger.getLogger(ReplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.getPreferredSize();
    }

    @Override
    public int getWidth() {
        try {
            return avatars.get(0).getWidth() + getInsets().left + getInsets().right;
        } catch (Exception ex) {
            Logger.getLogger(ReplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        try {
            return avatars.get(0).getHeight() +getInsets().bottom + getInsets().top + 20;
        } catch (Exception ex) {
            Logger.getLogger(ReplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.getHeight();
    }*/

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

        try {
            BufferedImage img = avatars.get(getAvatarIndex());
            setSize(img.getWidth(), img.getHeight());
            g2.drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
        } catch (Exception ex) {
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

    private void findAvatars(String runId) {
        avatars = new ArrayList<BufferedImage>();
        picturesFinder = new Thread(new PicturesFinderThread(runId.concat("*.png")));
        picturesFinder.start();
    }

    public void setSnapshots(Snapshots snapshots) {
        this.snapshots = snapshots;
        if (snapshots != null) {
            findAvatars(snapshots.getId());
        } else {
            avatars = new ArrayList<BufferedImage>();
            damaged = true;
            repaint();
        }
        //damaged = true;
    }

    public int getIndexMax() {
        return snapshots.getNumberImages() - 1;
    }

    public int getAvatarIndex() {
        return avatarIndex;
    }

    public void setAvatarIndex(int index) {
        avatarIndex = index;
        repaint();
    }

    private class PicturesFinderThread implements Runnable {

        String strFilter;

        PicturesFinderThread(String strFilter) {
            this.strFilter = strFilter;
        }

        public void run() {

            try {
                MetaFilenameFilter filter = new MetaFilenameFilter(strFilter);

                List<File> files = Arrays.asList(new File("./img").listFiles(filter));
                Collections.sort(files);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    BufferedImage image = ImageIO.read(file);
                    avatars.add(image);
                    if (i > 2) {
                        damaged = true;
                        setAvatarIndex(0);
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
