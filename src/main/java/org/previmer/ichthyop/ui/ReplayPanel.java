/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
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
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXImageView;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class ReplayPanel extends JXImageView {

    private List<BufferedImage> pictures = null;
    private List<String> pictureNames = null;
    private Thread picturesFinder = null;
    private int index;
    private File folder;
    private ImageIcon bgIcon;
    private int indexMax;

    public ReplayPanel() {
        setOpaque(false);
        pictures = new ArrayList();
        pictureNames = new ArrayList();
        setFolder(null);
    }

    public List<BufferedImage> getImages() {
        return pictures;
    }

    public int getIndexMax() {
        return indexMax;
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
        pictures.clear();
        pictureNames.clear();
        indexMax = -1;
        if (null != folder && folder.isDirectory()) {
            picturesFinder = new Thread(new PicturesFinderThread(folder, "*.png"));
            picturesFinder.start();
        } else {
            bgIcon = Application.getInstance().getContext().getResourceMap(IchthyopView.class).getImageIcon("step.Animation.bgicon");
            setImage(bgIcon.getImage());
        }
    }

    public String getTime() {
        try {
            String[] tokens = pictureNames.get(index).split("_");
            String date = tokens[tokens.length - 1];
            date = date.substring(0, date.indexOf(".png"));
            String[] dateToken = date.split("-");
            StringBuilder time = new StringBuilder("Year ");
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

        @Override
        public void run() {

            try {
                MetaFilenameFilter filter = new MetaFilenameFilter(strFilter);

                List<File> files = Arrays.asList(folder.listFiles(filter));
                indexMax = files.size() - 1;
                Collections.sort(files);
                int indexTrigger = Math.max(files.size() / 2, 1);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    BufferedImage image = ImageIO.read(file);
                    pictures.add(image);
                    pictureNames.add(file.getName());
                    if (i > indexTrigger) {
                        setIndex(0);
                    }

                }
            } catch (IOException e) {
            }

        }
    }
}
