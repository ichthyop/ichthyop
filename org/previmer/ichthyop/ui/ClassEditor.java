/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

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
import org.previmer.ichthyop.Simulation;

/**
 *
 * @author pverley
 */
public class ClassEditor extends DefaultCellEditor {

    JComboBox cbBox;

    public ClassEditor() {
        super(new JComboBox());
        cbBox = (JComboBox) getComponent();
        try {
            String packageName = Simulation.class.getPackage().getName();
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (Class aClass : getClasses(packageName)) {
                try {
                    if (null != aClass && null != aClass.getCanonicalName()) {
                        model.addElement(aClass.getCanonicalName());
                    }
                } catch (Exception ex) {
                }
            }
            cbBox.setModel(model);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClassEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClassEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Cette méthode permet de lister toutes les classes d'un package donné
     *
     * @param packageName Le nom du package à lister
     * @return La liste des classes
     */
    public List<Class> getClasses(String pckgname) throws ClassNotFoundException, IOException {
        // Création de la liste qui sera retournée
        ArrayList<Class> classes = new ArrayList<Class>();

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

    /**
     * Cette méthode retourne la liste des classes présentes
     * dans un répertoire du classpath et dans un package donné
     *
     * @param directory Le répertoire où chercher les classes
     * @param packageName Le nom du package
     * @return La liste des classes
     */
    private Collection<Class> scanDirectory(String directory, String packageName) throws ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<Class>();

        // On génère le chemin absolu du package
        StringBuffer sb = new StringBuffer(directory);
        String[] repsPkg = packageName.split("\\.");
        for (int i = 0; i < repsPkg.length; i++) {
            sb.append(System.getProperty("file.separator") + repsPkg[i]);
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

    /**
     * Cette méthode retourne la liste des classes présentes dans un jar du classpath et dans un package donné
     *
     * @param directory Le jar où chercher les classes
     * @param packageName Le nom du package
     * @return La liste des classes
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Collection<Class> scanJar(String jar, String packageName) throws IOException, ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<Class>();

        JarFile jfile = new JarFile(jar);
        String pkgpath = packageName.replace(".", "/");


        // Pour chaque entrée du Jar
        for (Enumeration<JarEntry> entries = jfile.entries(); entries.hasMoreElements();) {
            JarEntry element = entries.nextElement();

            // Si le nom de l'entrée commence par le chemin du package et finit par .class
            if (element.getName().startsWith(pkgpath)
                    && element.getName().endsWith(".class")) {

                String nomFichier = element.getName().substring(packageName.length() + 1);

                classes.add(Class.forName(packageName + "." + nomFichier.split("\\.")[0]));

            }

        }

        return classes;
    }

    /**
     * Cette classe permet de filtrer les fichiers d'un répertoire. Il n'accepte que les fichiers .class.
     */
    private class DotClassFilter implements FilenameFilter {

        public boolean accept(File arg0, String arg1) {
            return arg1.endsWith(".class");
        }
    }
}
