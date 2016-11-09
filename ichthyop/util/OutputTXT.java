package ichthyop.util;

import java.io.*;
import ichthyop.*;
/**
 *
 * <p>Title: Output text</p>
 *
 * <p>Description: Generate a text output file.</p>
 * Class under construction. Soon available.
 *
 * <p>Copyright: Copyright (c) Philippe VERLEY 2007</p>
 *
 */
public class OutputTXT {

  private static FileWriter fwriter;

  //----------------------------------------------------------------------------
  public OutputTXT() {
  }

  //----------------------------------------------------------------------------
  public static void createFile(int i_t0, int nb_t0) {

    String strFileOut;
    int n = 10;
    int nf_simu = 1;
    int nf_isimu = 1;
    while ( (nb_t0 % n) != nb_t0) {
      n *= 10;
      nf_simu++;
    }
    n = 10;
    while ( (i_t0 % n) != i_t0) {
      n *= 10;
      nf_isimu++;
    }
    strFileOut = GetConfig.getDirectorOut() + "balance_";
    for (int i = 0; i < (nf_simu - nf_isimu); i++) {
      strFileOut += String.valueOf(0);
    }
    strFileOut += String.valueOf(i_t0) + ".txt";

    try {
      fwriter = new FileWriter(strFileOut);
    }
    catch (IOException ex) {}

  }

  //----------------------------------------------------------------------------
}
