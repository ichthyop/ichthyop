package ichthyop.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class IBMFileFilter extends FileFilter {

    private String filter;

    //--------------------------------------------------------------------------
    public IBMFileFilter(String extension) {
      this.filter = extension.toLowerCase();
    }

    //--------------------------------------------------------------------------
    public boolean accept(File f) {
      if (f != null) {
        if (f.isDirectory()) {
          return true;
        }
        String extension = getExtension(f);
        if (extension != null && extension.matches(filter)) {
          return true;
        }
      }
      return false;
    }

    //--------------------------------------------------------------------------
    private String getExtension(File f) {
      if (f != null) {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
          return filename.substring(i + 1).toLowerCase();
        }
      }
      return null;
    }

    //--------------------------------------------------------------------------
    public File addExtension(File f) {
      if (accept(f) && !f.isDirectory()) {
        return f;
      }
      return new File(f.toString() + "." + filter);
    }

    //--------------------------------------------------------------------------
    public String getDescription() {
      if (filter.matches(Resources.EXTENSION_CONFIG))
        return "Configuration file (*." + Resources.EXTENSION_CONFIG + ")";
      else if (filter.matches(Resources.EXTENSION_DRIFTER))
        return "Drifters file (*." + Resources.EXTENSION_DRIFTER + ")";
      return "[No description] (*." + filter + ")";
    }
  }
