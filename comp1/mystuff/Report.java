package sem2;

import java.io.*;

public class Report {

  public static String getLine(String filename, int line) {
    String s = null;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
      
      for (int i=0; i<line; ++i) {
        s = reader.readLine();
      }
      return s;
    }    
    catch (Exception e) {
      return null;
    }
  }

  public static String markerString(String s, int column, char marker) {
    StringBuffer sb = new StringBuffer(s.length());
    for (int i=0; i<column-1; ++i) {
      switch (s.charAt(i)) {
        case '\t':
          sb.append('\t');
          break;
        default:
          sb.append(' ');
          break;
      }
    }
    sb.append(marker);
    return sb.toString();
  }
}
        

