import java.io.*;
import java.util.*;

public class IO {

  /* Void move is the thing the opponent does
   * at the begining if you start.
   * Invalid move is either out of bounds, or
   * the opponent chose a "bad" field.
   */
  public static final int MOVE_VOID = -1;
  public static final int MOVE_ILLEGAL = -2;

  public static final int MAX_TRIES = 3;

  public static boolean meFirst;
  private static int moveNum = 0;
  private Player player;

  /* THE FIELD */

  /* (0, 0) is left-bottom field! */

  /* Due to speed certain stuff is suboptimal:
   * FIELD_EMPTY and FIELD_FORBIDDEN are used in the table
   * (BEWARE!), totalMoves is not calculated in runtime but
   * is a constant value!
   */

  /* What is on a certain field? */
  public static final int FIELD_EMPTY = 0;
  public static final int FIELD_FORBIDDEN = 1;
  public static final int FIELD_MY = 2;
  public static final int FIELD_OPPONENT = 3;

  private static int[][] table = {
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }
  };
  private static final int totalMoves = 15*15 - 4*(4*4); /* All field - 4 * forbidden fields */

  /* Ig_K_Pot_N.txt */
  private final String IG = "Ig_";
  private final String POT = "_Pot_";
  private final String EXT = ".txt";

  /* Constructors */
  public IO(Player player) {
    this(player, true); 
  }

  public IO(Player player, boolean meFirst) {
    this.meFirst = meFirst;
    this.player = player;
  }

  /* After initialization call this method */
  public void start() {
    if (!player.ID().equals("OK")) {
      System.err.println("[IO] Player is not OK!");
      return;
    }

    if (meFirst) {
      player.opponent(MOVE_VOID, MOVE_VOID); 
    }
    while (true) {
      /* This method calls opponent() again */
      opponentWait();
    }
  }

  public static int getMoveNum() {
    return moveNum;
  }

  public static int getTotalMoves() {
    return totalMoves;
  }

  public static int getFieldWidth() {
    return table[0].length;
  }

  public static int getFieldHeight() {
    return table.length;
  }

  /* Get the sign on the specified field */
  public int get(final int x, final int y) {
    /* x is normal */
    /* y is vice-versa */
    int newY = getFieldHeight() - y - 1;

    /*
     * It's faster if we just access the field and catch a possible exception!
    if ((x < 0) || (x >= getFieldWidth()) || (y < 0) || (y >= getFieldHeight())) {
      System.err.println("[IO] Index out of bounds - getting element ("+x+", "+y+")");
      return MOVE_ILLEGAL;
    }
    */
   
    try {
      return table[newY][x];
    }
    catch (IndexOutOfBoundsException e) {
      System.err.println("[IO] Index out of bounds - getting element ("+x+", "+y+")");
      return MOVE_ILLEGAL;
    }

  }

  /* Try to set a sign on the specified field 
   * Return whether the move was legal.
   */
  private boolean set(final int x, final int y, final int sign) {
    /* x is normal */
    /* y is vice-versa */
    int newY = getFieldHeight() - y - 1;

    /*
     * It's faster if we just access the field and catch a possible exception!
    if ((x < 0) || (x >= getFieldWidth()) || (y < 0) || (y >= getFieldHeight())) {
      System.err.println("[IO] Index out of bounds - setting element ("+x+", "+y+")");
      return false;
    }
    */

    try {
      if (table[newY][x] == FIELD_EMPTY) {
        table[newY][x] = sign;
        return true;
      } else {
        System.err.println("[IO] Field is not empty but "+table[newY][x]+" - setting element ("+x+", "+y+")");
        return false;
      }
    }
    catch (IndexOutOfBoundsException e) {
      System.err.println("[IO] Index out of bounds - setting element ("+x+", "+y+")");
      return false;
    }
  }

  /*
   * Try to make a move.
   */
  public void move(final int x, final int y) {
    int myX = x, myY = y;

    if (moveNum++ > totalMoves) {
      System.err.println("[IO] No more moves possible!");
      return;
    }

    if (!set(myX, myY, FIELD_MY)) {
      System.err.println("[IO] I am about to make an illegal move. Rescue me!");

      /* Place my sign on first empty field! */
      outer:
      for (myY=0; myY<getFieldHeight(); ++myY) {
        for (myX=0; myX<getFieldWidth(); ++myX) {
          if (table[myY][myX] == FIELD_EMPTY) {
	    //System.out.println("("+myX+", "+myY+")");
	    table[myY][myX] = FIELD_MY;
	    myY = getFieldHeight() - myY - 1;
	    System.err.println("[IO] Rescued with ("+myX+", "+myY+")");
	    break outer;
	  }
	}
      }
      if ((myX == getFieldWidth()) || (myY == getFieldHeight())) {
        System.out.println("[IO] Rescuing failed! No more space on the field?");
      }
    }

    /* Write my move to a file */

    int myNum = meFirst?(1):(2);

    for (int tryn=1; tryn <= MAX_TRIES; ++tryn) {
      try {
        PrintStream p = new PrintStream(new 
  		         FileOutputStream(IG+myNum+POT+moveNum+EXT));
        p.println(myX);
        p.println(myY);

        p.close();
        /* Everything was fine */
        break;
      }

      catch (Exception e) {
        System.err.println("[IO] Couldn't write my move (try: "+tryn+") - "+e.toString());
        try {
	  Thread.sleep(50);
	}
        catch (InterruptedException ie) {
          System.err.println("[IO] We got interrupted while waiting - "+
	  		    ie.toString());
        }
      }
    }
  }

  /* Waits for the opponent to do some move and parse his move file */
  public void opponentWait() {
    final int DELAY = 200; /* 200 ms delay is required */
    final int LIMIT = 51; /* 51 * 200 ms = 10,2 s */

    int waitCycle = 0; 
    int opNum = meFirst?(2):(1);
    boolean warned = false;
    int x, y;

    if (moveNum++ > totalMoves) {
      /* No more possible moves */
      return;
    }

    File f = new File(IG+opNum+POT+moveNum+EXT);

    /* Is it OK if we first delete the old file existing in the dir with such a name? 
    try {
      f.delete();
    }
    catch (Exception e) {}
    */

    /* Be polite and wait for the opponent to do something */
    while (!f.exists()) {
      if (++waitCycle >= LIMIT) {
	if (!warned) {
          System.err.println("[IO] Opponent took a too long time with the answer!");
	  warned = true;
	}
      }

      try {
        Thread.sleep(DELAY);
      }

      catch (InterruptedException ie) {
        System.err.println("[IO] We got interrupted while waiting - "+
			ie.toString());
      }
    } 

    System.out.println("[IO] Opponent's answer (move "+moveNum+") took: "+(waitCycle*DELAY)+" ms.");

    /* Parse opponent's file */
    x = y = MOVE_ILLEGAL;

    for (int tryn=1; tryn <= MAX_TRIES; ++tryn) {
      try {
        BufferedReader in = new BufferedReader(new FileReader(f));
        StringTokenizer st;
        st = new StringTokenizer(in.readLine());
        x = Integer.parseInt(st.nextToken());
        st = new StringTokenizer(in.readLine());
        y = Integer.parseInt(st.nextToken());
        in.close();
	/* Everything was fine */
	break;
      }
      catch (Exception e) {
        System.err.println("[IO] Can't read opponent's file (try: "+tryn+") - "
  		           +e.toString());
        try {
	  Thread.sleep(50);
	}
        catch (InterruptedException ie) {
          System.err.println("[IO] We got interrupted while waiting - "+
	  		    ie.toString());
        }
      }
    }

    if (x != MOVE_ILLEGAL && !set(x, y, FIELD_OPPONENT)) {
      System.err.println("[IO] Opponent made an illegal move!");
      x = MOVE_ILLEGAL; y = MOVE_ILLEGAL;
    }
   
    player.opponent(x, y); 
  }


  /*** Stuff useful for testing ***/

  public static final int RESULT_DRAW = 0;
  public static final int RESULT_WON  = 1;
  public static final int RESULT_NONE = 2;

  /* There need to be 5 chars in a row to win */
  public static final int WIN = 5;

  /* If player puts sign on position x, y what will happen? */
  /* Returns: IO.RESULT_WON - player wins the game,
   * IO.RESULT_NONE - nothing, or IO.RESULT_DRAW the game is a draw.
   */
  public int checkState(final int x, final int y) {
    int count;
    int currentX, currentY;
    int sign = get(x, y);

    /* Check in the row */
    currentX = x; count = 0;
    //System.out.println("Row check: ");
    for (currentY=Math.max(y-(WIN-1), 0); currentY<=Math.min(y+(WIN-1), getFieldHeight()-1); ++currentY) {
      //System.out.println("("+currentX+","+currentY+")");
      if (get(currentX, currentY) == sign) {
        if (++count >= WIN) {
	  return IO.RESULT_WON;
	}
      } else {
        count = 0;
      }
    }

    /* Check in the line */
    currentY = y; count = 0;
    //System.out.println("Line check: ");
    for (currentX=Math.max(x-(WIN-1), 0); currentX<=Math.min(x+(WIN-1), getFieldWidth()-1); ++currentX) {
      //System.out.println("("+currentX+","+currentY+")");
      if (get(currentX, currentY) == sign) {
        if (++count >= WIN) {
	  return IO.RESULT_WON;
	}
      } else {
        count = 0;
      }
    }

    int distX, distY;

    /* Check in the first diagonal */
    count = 0;
    distX = (getFieldWidth()-1) - x;
    distY = (getFieldHeight()-1) - y;

    if ((distX >= (WIN-1)) && (distY >= (WIN-1))) {
      currentY = y + (WIN-1);
      currentX = x + (WIN-1);
    } else {
      currentY = y + Math.min(distX, distY);
      currentX = x + Math.min(distX, distY);
    }

    //System.out.println("First diagonal check: ");
    while ((currentX >= Math.max(0, x-(WIN-1))) && 
           (currentY >= Math.max(0, y-(WIN-1)))) {
      //System.out.println("("+currentX+","+currentY+")");
      if (get(currentX--, currentY--) == sign) {
        if (++count >= WIN) {
	  return IO.RESULT_WON;
	}
      } else {
        count = 0;
      }
    }

    /* Check in the second diagonal */
    count = 0;

    distX = (getFieldWidth()-1) - x;
    distY = y;

    if ((distX >= (WIN-1)) && (distY >= (WIN-1))) {
      currentY = y - (WIN-1);
      currentX = x + (WIN-1);
    } else {
      currentY = y - Math.min(distX, distY);
      currentX = x + Math.min(distX, distY);
    }

    //System.out.println("Second diagonal check: ");
    while ((currentX >= Math.max(0, x-(WIN-1))) && 
	   (currentY <= Math.min(getFieldHeight()-1, y+(WIN-1)))) {
      //System.out.println("("+currentX+","+currentY+")");
      if (get(currentX--, currentY++) == sign) {
        if (++count >= WIN) {
	  return IO.RESULT_WON;
	}
      } else {
        count = 0;
      }
    }
    if (moveNum >= totalMoves) {
      return IO.RESULT_DRAW;
    }

    return IO.RESULT_NONE;
  }  
}
