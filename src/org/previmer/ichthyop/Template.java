/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class Template {

    public static void createTemplate(String templateName, File destination) throws IOException {
        String path = "templates/" + templateName;
        URL url = Template.class.getResource(path);
        try {
            writeTemplate(url, destination.getAbsolutePath());
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to create template at " + destination + " ==> " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
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
