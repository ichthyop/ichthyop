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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.ichthyop.Simulation;

/**
 *
 * @author pverley
 */
public class ClassEditor extends DefaultCellEditor {

    JComboBox cbBox;

    public ClassEditor() throws Exception {
        super(new JComboBox());
        cbBox = (JComboBox) getComponent();
        String packageName = Simulation.class.getPackage().getName();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Class aClass : getClasses(packageName)) {
            if (null != aClass && null != aClass.getCanonicalName()) {
                model.addElement(aClass.getCanonicalName());
            }
        }
        cbBox.setModel(model);
    }

    /*
     * Cette méthode permet de lister toutes les classes d'un package donné
     *
     * @param packageName Le nom du package à lister
     * @return La liste des classes
     */
    private List<Class> getClasses(String pckgname) throws Exception {
        // Création de la liste qui sera retournée
        ArrayList<Class> classes = new ArrayList();

        // On récupère toutes les entrées du CLASSPATH
        String[] entries = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

        // Pour toutes ces entrées, on verifie si elles contiennent
        // un répertoire ou un jar
        for (int i = 0; i < entries.length; i++) {

            if (entries[i].endsWith(".jar")) {
                classes.addAll(scanJar(entries[i], pckgname));
            } else {
                classes.addAll(scanDirectory(entries[i], pckgname));
            }

        }

        return classes;
    }

    /*
     * Cette méthode retourne la liste des classes présentes
     * dans un répertoire du classpath et dans un package donné
     *
     * @param directory Le répertoire où chercher les classes
     * @param packageName Le nom du package
     * @return La liste des classes
     */
    private Collection<Class> scanDirectory(String directory, String packageName) throws Exception {
        
        ArrayList<Class> classes = new ArrayList();

        // On génère le chemin absolu du package
        StringBuilder sb = new StringBuilder(directory);
        String[] repsPkg = packageName.split("\\.");
        for (int i = 0; i < repsPkg.length; i++) {
            sb.append(System.getProperty("file.separator")).append(repsPkg[i]);
        }
        File rep = new File(sb.toString());

        // Si le chemin existe, et que c'est un dossier, alors, on le liste
        if (rep.exists() && rep.isDirectory()) {
            FilenameFilter filter = new DotClassFilter();
            File[] liste = rep.listFiles(filter);
            // Pour chaque classe présente dans le package, on l'ajoute à la liste
            for (int i = 0; i < liste.length; i++) {
                classes.add(Class.forName(packageName + "." + liste[i].getName().split("\\.")[0]));
            }
            File[] subdirectories = rep.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (File subdirectory : subdirectories) {
                String subpckgname = packageName + "." + subdirectory.getName();
                classes.addAll(scanDirectory(directory, subpckgname));
            }
        }
        return classes;
    }

    /*
     * Cette méthode retourne la liste des classes présentes dans un jar du classpath et dans un package donné
     *
     * @param directory Le jar où chercher les classes
     * @param packageName Le nom du package
     * @return La liste des classes
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Collection<Class> scanJar(String jar, String packageName) throws Exception {
        try {
            ArrayList<Class> classes = new ArrayList();

            JarFile jfile = new JarFile(jar);
            String pkgpath = packageName.replace(".", "/");

            // Pour chaque entrée du Jar
            for (Enumeration<JarEntry> entries = jfile.entries(); entries.hasMoreElements();) {
                JarEntry element = entries.nextElement();

                // Si le nom de l'entrée commence par le chemin du package et finit par .class
                if (element.getName().startsWith(pkgpath)
                        && element.getName().endsWith(".class")) {
                    /*
                    String nomFichier = element.getName().substring(packageName.length() + 1);
                    classes.add(Class.forName(packageName + "." + nomFichier.split("\\.")[0]));
 
                    barrier.n, 2017-08-04:
                    in the .jar file, the class appears as org/ichthyop/dataset/Roms3dDataset.class
                    therefore, the org.ichthyop.dataset.Roms3dDataset class should be loaded
                    however, with the above, nomFichier becomes dataset/Roms3dDataset.class
                    thus the class org.ichthyop.dataset/Roms3dDataset is being loaded, which is wrong.
                    
                    Now we recover the element name org/ichthyop/dataset/Roms3dDataset.class
                    and we replace ".class" by "" and "/" by "." to have the proper class name
                     */
                    String nomFichier = element.getName().replace(".class", "").replace("/", ".");
                    classes.add(Class.forName(nomFichier));
                }
            }

            return classes;
        } catch (IOException ex) {
            Logger.getLogger(ClassEditor.class.getName()).log(Level.WARNING, null, ex);
        }
        return new ArrayList();
    }

    /**
     * Cette classe permet de filtrer les fichiers d'un répertoire. Il n'accepte
     * que les fichiers .class.
     */
    private class DotClassFilter implements FilenameFilter {

        @Override
        public boolean accept(File arg0, String arg1) {
            return arg1.endsWith(".class");
        }
    }
}
