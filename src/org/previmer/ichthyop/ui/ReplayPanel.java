/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.JXImagePanel;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class ReplayPanel extends JXImagePanel {

    private List<BufferedImage> pictures = null;
    private List<String> pictureNames = null;
    private Thread picturesFinder = null;
    private int index;
    private File folder;
    private ImageIcon bgIcon;

    public ReplayPanel() {
        this.setOpaque(false);
        this.setStyle(JXImagePanel.Style.CENTERED);
    }

    public int getIndexMax() {
        return pictures.size() - 1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        if (pictures != null && !pictures.isEmpty()) {
            setImage(pictures.get(getIndex()));
        }
    }

    void setFolder(File folder) {
        this.folder = folder;
        pictures = new ArrayList();
        pictureNames = new ArrayList();
        if (null != folder && folder.isDirectory()) {
            picturesFinder = new Thread(new PicturesFinderThread(folder, "*.png"));
            picturesFinder.start();
        } else {
            bgIcon = IchthyopApp.getApplication().getMainView().getResourceMap().getImageIcon("step.Animation.bgicon");
            setImage(bgIcon.getImage());
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
                        setIndex(0);
                    }

                }
            } catch (IOException e) {
            }

        }
    }
}