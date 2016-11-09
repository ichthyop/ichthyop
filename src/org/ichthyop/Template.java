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

package org.ichthyop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.ichthyop.io.IOTools;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class Template {

    public static void createTemplate(String templateName, File destination) throws IOException {
        try {
            writeTemplate(getTemplateURL(templateName), destination.getAbsolutePath());
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to create template at " + destination + " ==> " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    public static URL getTemplateURL(String templateName) {
        String path = "templates/" + templateName;
        return Template.class.getResource(path);
    }

    private static void writeTemplate(URL url, String destination) throws Exception {

        URLConnection connection = null;
        InputStream is = null;
        FileOutputStream destinationFile = null;

        //On crée une connection vers cet URL
        connection = url.openConnection();

        //On récupère la taille du fichier
        int length = connection.getContentLength();

        //Si le fichier est inexistant, on lance une exception
        if (length == -1) {
            throw new IOException("Template does not exist " + url.toExternalForm());
        }

        //On récupère le stream du fichier
        is = new BufferedInputStream(connection.getInputStream());

        //On prépare le tableau de bits pour les données du fichier
        byte[] data = new byte[length];

        //On déclare les variables pour se retrouver dans la lecture du fichier
        int currentBit = 0;
        int deplacement = 0;

        //Tant que l'on n'est pas à la fin du fichier, on récupère des données
        while (deplacement < length) {
            currentBit = is.read(data, deplacement, data.length - deplacement);
            if (currentBit == -1) {
                break;
            }
            deplacement += currentBit;
        }

        //Si on est pas arrivé à la fin du fichier, on lance une exception
        if (deplacement != length) {
            throw new IOException("Failed to read template file " + url.toExternalForm());
        }

        //On crée un stream sortant vers la destination
        IOTools.makeDirectories(destination);
        destinationFile = new FileOutputStream(destination);

        //On écrit les données du fichier dans ce stream
        destinationFile.write(data);

        //On vide le tampon et on ferme le stream
        destinationFile.flush();

        try {
            is.close();
            destinationFile.close();
        } catch (Exception e) {
        }

    }
}
