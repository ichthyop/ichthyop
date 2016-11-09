package ichthyop.util;

import java.util.Comparator;
import java.io.IOException;
import ucar.netcdf.*;
import ucar.multiarray.*;

//////////////////////////////////////////////////
public class NCComparator
    implements Comparator {

  private String strTime;

  //---------------------------------
  public NCComparator(String strTime) {
    this.strTime = strTime;
  }

  //----------------------------------------
  public int compare(Object nc1, Object nc2) {
    Double
        n1 = new Double(0),
        n2 = new Double(0);
    MultiArray timeArr;
    try {
      timeArr = (new NetcdfFile((String)nc1, true)).get(strTime);
      n1 = timeArr.getDouble(new int[] {0});
      timeArr = (new NetcdfFile((String)nc2, true)).get(strTime);
      n2 = timeArr.getDouble(new int[] {0});
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (NullPointerException e) {
      e.printStackTrace();
    }

    return n1.compareTo(n2);
  }
}
