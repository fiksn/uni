import java.util.*;

public class RandomPlayer extends Player {
  private IO io;
  private Random r = new Random();

  public static void main(String[] args) {
    int n = 0;

    if (args.length < 1) {
      System.err.println("Sintaksa: program [1|2]\n1 pomeni da zacnem jaz, 2 zacne nasprotnik");
      return;
    }

    n = Integer.parseInt(args[0]);
    if (n < 1 || n > 2) {
      System.err.println("Neveljavna stevilka!");
      return;
    }

    new RandomPlayer(n);
  } 

  public RandomPlayer(int n) {
    io = new IO(this, n==1);
    io.start();
  }

  /* Neumen algoritem */
  public void opponent(int x, int y) {
    final int MAXTRIES = 5;
    int i, myX, myY, state;

    if (x == io.MOVE_VOID) {
      System.out.println("Jaz zacenjam!"+x+" "+y);
    } else {
      System.out.println("Nasprotnik je naredil potezo "+x+" "+y);
    }

    if (x >= 0 && y >= 0) {
      state = io.checkState(x, y);
      if (state == io.RESULT_WON) {
        System.out.println("Nasprotnik je zmagal!");
        return;
      } else if (state == io.RESULT_DRAW) {
        System.out.println("Neodloceno!");
      }
    }

    for (i=0; i<MAXTRIES; ++i) {
      myX = (int)(r.nextDouble()*(io.getFieldWidth()-1));  
      myY = (int)(r.nextDouble()*(io.getFieldHeight()-1));  
      if (io.get(myX, myY) == io.FIELD_EMPTY) {
	System.out.println("Nasel prazno polje "+myX+" "+myY+".");
        io.move(myX, myY);
	System.out.println("Postavil moj znak.");
	state = io.checkState(myX, myY);
        if (state == io.RESULT_WON) {
          System.out.println("Jaz sem zmagal!");
	  System.exit(1);
        } else if (state == io.RESULT_DRAW) {
          System.out.println("Remi!");
          System.exit(0);
        }

	break;
      }
    }

    if (i >= MAXTRIES) {
      /* Ni mi uspelo v random poskusih - Prepusti vso logiko IO. */
      System.out.println("Random AI je propadel - prepusti vse IO modulu.");
      io.move(io.MOVE_VOID, io.MOVE_VOID);
    }
  }
}
