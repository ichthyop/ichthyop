/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXImageView;
import org.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class ReplayPanel extends JXImageView {

    private final List<BufferedImage> images;
    private final List<File> files;
    private File folder;
    private int nimg;
    public static final String IMAGE_INDEX = "imageIndex";
    private final BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    public ReplayPanel() {
        images = new ArrayList();
        files = new ArrayList();
        nimg = 0;
        setImage(emptyImage);
    }

    public int getNImage() {
        return nimg;
    }

    public void showImage(int index) {

        if (images.size() > index) {
            setImage(images.get(index));
            firePropertyChange(IMAGE_INDEX, null, timestamp(index));
        } else {
            setImage(emptyImage);
        }
    }

    public void clear() {
        this.folder = null;
        images.clear();
        files.clear();
        nimg = 0;
        setImage(emptyImage);
    }

    public void loadImages(File folder) throws IOException {
        this.folder = folder;
        images.clear();
        files.clear();
        if (null != folder && folder.isDirectory()) {
            MetaFilenameFilter filter = new MetaFilenameFilter("*.png");
            List<File> pngFiles = Arrays.asList(folder.listFiles(filter));
            nimg = pngFiles.size();
            Collections.sort(pngFiles);
            for (int i = 0; i < nimg; i++) {
                images.add(ImageIO.read(pngFiles.get(i)));
                files.add(pngFiles.get(i));
            }
        }
    }

    private String timestamp(int index) {
        try {
            String[] tokens = files.get(index).getName().split("_");
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

    public String toGIF(float nfps, boolean reverse) {

        AnimatedGifEncoder gif = new AnimatedGifEncoder();
        String filename = folder.getAbsolutePath();
        if (filename.endsWith(File.separator)) {
            filename = filename.substring(0, filename.length() - 1);
        }
        filename += ".gif";
        gif.start(filename);
        gif.setDelay((int) (1000 / nfps));
        if (reverse) {
            Collections.reverse(images);
        }
        for (BufferedImage img : images) {
            gif.addFrame(img);
        }
        gif.finish();
        return filename;
    }

    public void deleteImages() {
        for (File file : files) {
            file.delete();
        }
        if (folder.listFiles().length == 0) {
            folder.delete();
        }
        clear();
    }
}
