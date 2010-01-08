/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class Snapshots {

    private String id;
    private File path;
    private File[] listFiles;
    private static SimpleDateFormat dtformatterId = new SimpleDateFormat("yyyyMMddHHmm");
    private static SimpleDateFormat dtformatterReadableId = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Snapshots(String id) {
        this.id = id;
    }

    public Snapshots(String id, String folder) {
        this.id = id;
        String folderName = folder;
        if (!folderName.endsWith(File.separator)) {
            folderName += File.separator;
        }
        path = new File(folderName + id);
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
        return filename;
    }

    public static String getReadableIdFromFile(File file) {
        return idToReadableId(getIdFromFile(file));
    }

    public static String idToReadableId(String id) {
        String strId = id.substring(id.indexOf("ichthyop-run") + 12);
        String prefix = id.substring(0, id.indexOf("ichthyop-run"));
        prefix += prefix.length() > 0
                ? " run "
                : "Run ";
        try {
            return prefix + dtformatterReadableId.format(dtformatterId.parse(strId));

        } catch (ParseException ex) {
            Logger.getLogger(IchthyopApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String readableIdToId(String readableId) {
        String strReadableId = readableId.substring(readableId.toLowerCase().lastIndexOf("run") + 3);
        String prefix = readableId.substring(0, readableId.toLowerCase().lastIndexOf("run"));
        try {
            String strId = prefix.length() > 0
                    ? prefix + "_ichthyop-run"
                    : "ichthyop-run";
            return strId + dtformatterId.format(dtformatterReadableId.parse(strReadableId));

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

    public File[] getImages() {

        if (listFiles != null) {
            return listFiles;
        } else {
            try {
                listFiles = path.listFiles(new MetaFilenameFilter(getId() + "*.png"));
                Arrays.sort(listFiles);
            } catch (Exception e) {
                listFiles = new File[0];
            }
            return listFiles;
        }
    }

    public String getTime(int index) {
        if (index > getNumberImages() - 1 | index < 0) {
            return "";
        }
        String date = getImages()[index].getName().split("_")[1];
        date = date.substring(0, date.indexOf(".png"));
        String[] dateToken = date.split("-");
        StringBuffer time = new StringBuffer("Year ");
        time.append(dateToken[0]);
        time.append(" Month ");
        time.append(dateToken[1]);
        time.append(" Day ");
        time.append(dateToken[2]);
        time.append(" - ");
        time.append(dateToken[3]);
        time.append(":");
        time.append(dateToken[4]);
        return time.toString();
    }

    public int getNumberImages() {
        return getImages().length;
    }

    public String getPath() {
        return path.getAbsolutePath();
    }
}
