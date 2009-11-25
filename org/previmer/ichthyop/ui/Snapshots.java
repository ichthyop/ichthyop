/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class Snapshots {

    private String id;
    private File path = new File("./img");
    private File[] listFiles;
    private static SimpleDateFormat dtformatterId = new SimpleDateFormat("yyyyMMddHHmm");
    private static SimpleDateFormat dtformatterReadableId = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Snapshots(String id) {
        this.id = id;
    }

    public static String newId() {
        StringBuffer strBfRunId = new StringBuffer("ichthyop-run");
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        dtformatterId.setCalendar(calendar);
        strBfRunId.append(dtformatterId.format(calendar.getTime()));
        return strBfRunId.toString();
    }

    public static String getIdFromFile(File file) {
        String filename = file.getName();
        return filename.split("_")[0];
    }

    public static String getReadableIdFromFile(File file) {
        return idToReadableId(getIdFromFile(file));
    }

    public static String idToReadableId(String id) {
        String strId = id.substring(id.indexOf("run") + 3);
        try {
            return "Run " + dtformatterReadableId.format(dtformatterId.parse(strId));

        } catch (ParseException ex) {
            Logger.getLogger(IchthyopApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String readableIdToId(String readableId) {
        String strReadableId = readableId.substring(4);
        try {
            return "ichthyop-run" + dtformatterId.format(dtformatterReadableId.parse(strReadableId));

        } catch (ParseException ex) {
            Logger.getLogger(IchthyopApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getReadableId() {
        return idToReadableId(getId());
    }

    private File[] getImages() {

        if (listFiles != null) {
            return listFiles;
        } else {
            return listFiles = path.listFiles(new MetaFilenameFilter(getId() + "*.png"));
        }
    }

    public int getNumberImages() {
        return getImages().length;
    }

    public void setPath(String strPath) {
        this.path = new File(strPath);
        if (!path.isDirectory()) {
            try {
                throw new IOException(path.toString() + " is not a valid directory");
            } catch (IOException ex) {
                Logger.getLogger(Snapshots.class.getName()).log(Level.SEVERE, null, ex);
            }
            path = new File("./img");
        }
        listFiles = null;
    }
}
