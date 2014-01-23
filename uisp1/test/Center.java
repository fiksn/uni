/* Ugly dummy file */
import java.util.*;

public class Center {
  private static int count = 1;

  public static ArrayList forbidden = new ArrayList();
 
  public static boolean isForbidden(int x, int y) {
    for (int i=0; i<forbidden.size(); ++i) {
      Position temp = (Position)forbidden.get(i);
      if (temp.x == x && temp.y == y)
        return true;
    }
    return false;
  }

  public static void center(int n, int x, int y) {
    if (n <= 0) {
      System.out.println("validMoves.add(new PerspectivePosition("+x+", "+y+", "+161+"));");
    } else {
      for (int i=y-n; i<=y+n; ++i) {
        for (int j=x-n; j<=x+n; ++j) {
          if (j==x-n || j==x+n || i==x-n || i==x+n) {
            if (!isForbidden(j, i)) {
              System.out.println("validMoves.add(new PerspectivePosition("+j+", "+i+", "+(161-count)+"));");
	      count++;
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    int i, j;
    for (i=0; i<4; ++i) {
      for (j=0; j<4; ++j) {
        forbidden.add(new Position(i, j));
      }
    }

    for (i=0; i<4; ++i) {
      for (j=0; j<4; ++j) {
        forbidden.add(new Position(i, j));
      }
    }

    for (i=11; i<15; ++i) {
      for (j=0; j<4; ++j) {
        forbidden.add(new Position(i, j));
      }
    }

    for (i=0; i<4; ++i) {
      for (j=11; j<15; ++j) {
        forbidden.add(new Position(i, j));
      }
    }

    for (i=11; i<15; ++i) {
      for (j=11; j<15; ++j) {
        forbidden.add(new Position(i, j));
      }
    }

    for (i=0; i<=7; ++i) {
      center(i, 7, 7);
    }
  }
}
