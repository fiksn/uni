public class Board {
   private int price; 

   private static final int NUM = 15;
   private static final int WIN = 5;

   /* Forbidden rows and cols around the actual field! */
   private int[][] table = {
    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
  };

  public int[] rowsValue = new int[NUM];
  public int[] colsValue = new int[NUM];
  public int[] diag1Value = new int[NUM];
  public int[] diag2Value = new int[NUM];
  
  public Board() {
    price = 0;
  }
 
  private int getFieldWidth() {
    return (table[0].length)-2;
  }

  private int getFieldHeight() {
    return (table.length)-2;
  }

  public int boardValue() {
    int value = 0;
    int i;

    for (i=0; i<rowsValue.length; ++i) {
      value+=rowsValue[i];
    }

    for (i=0; i<colsValue.length; ++i) {
      value+=colsValue[i];
    }

    for (i=0; i<diag1Value.length; ++i) {
      value+=diag1Value[i];
    }

    for (i=0; i<diag2Value.length; ++i) {
      value+=diag2Value[i];
    }

    return Math.max(AIPlayer.MINWIN, Math.min(AIPlayer.MAXWIN, value));
  }

  public void debug() {
    int i;

    for (i=0; i<rowsValue.length; ++i) {
      System.out.println("Row i = "+i+" "+rowsValue[i]);
    }

    for (i=0; i<colsValue.length; ++i) {
      System.out.println("Col i = "+i+" "+colsValue[i]);
    }

    for (i=0; i<diag1Value.length; ++i) {
      System.out.println("Diag1 i = "+i+" "+diag1Value[i]);
    }

    for (i=0; i<diag2Value.length; ++i) {
      System.out.println("Diag2 i = "+i+" "+diag2Value[i]);
    }
  }

  public int evalPrice(int sign, int count, boolean capped) {
    /* If I'm the second one I'm a bit more defensive */
	/* 20, 500 */
    int bonus1 = (IO.meFirst)?(0):(90);
    int bonus2 = (IO.meFirst)?(0):(900);

    switch (count) {
      case 1:
        break;
      case 2: 
        if (capped) {
          return (sign==IO.FIELD_MY)?(5):(-5);
        } else {
          return (sign==IO.FIELD_MY)?(15):(-15);
	}
      case 3:
        if (capped) {
          return (sign==IO.FIELD_MY)?(20):(-25);
        } else {
          return (sign==IO.FIELD_MY)?(300):(-400);
	}
      case 4:
        if (capped) {
          return (sign==IO.FIELD_MY)?(500):(-500);
        } else {
          return (sign==IO.FIELD_MY)?(50000):(-55000);
	}
      default:
	/* Better winning positions are fine aswell */
        if (count >= 5) {
	  return (sign==IO.FIELD_MY)?(AIPlayer.MAXWIN):(AIPlayer.MINWIN);
        }
     }

     /* Not reached */
     return 0;
   }

  public void updatePrice(Position p) {
    /* Update line */
    int countEmpty, countMy, countOpponent;
    int potentialMy, potentialOpponent;

    /*
    System.out.println("*");
    for (int i=table.length-1; i>=0; --i) {
      for (int j=0; j<table.length; ++j) {
        System.out.print(getRaw(j, i)+" ");
      }
      System.out.println();
    }

    System.out.println("*");
    */

    /* Clear values */

    rowsValue[p.y] = 0;
    colsValue[p.x] = 0;
    diag1Value[p.x] = 0;
    diag2Value[p.x] = 0;

    /* Line check */

    boolean capped = false; 
    int x = 1;
    int y = p.y + 1;
    potentialOpponent = potentialMy = 0;
    countEmpty = 0;

    while (x < table[0].length) {
      capped = false;
      switch (getRaw(x, y)) {
        case IO.FIELD_EMPTY: 
	  countEmpty = 0;
	  while (getRaw(x, y) == IO.FIELD_EMPTY) {
            countEmpty++;
	    x++;
	  }
	  break;
	case IO.FIELD_OPPONENT:
	  countOpponent = 0; 
	  potentialOpponent = 0;
	  if (getRaw(x-1, y) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialOpponent = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_OPPONENT) {
            countOpponent++;
	    potentialOpponent++;
	    x++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x++;
	    }
	    potentialOpponent += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialOpponent >= WIN) {
	     rowsValue[p.y] += evalPrice(IO.FIELD_OPPONENT, countOpponent, capped);
          }
	  break;
	case IO.FIELD_MY:
	  countMy = 0; 
	  potentialMy = 0;
	  if (getRaw(x-1, y) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialMy = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_MY) {
            countMy++;
	    potentialMy++;
	    x++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x++;
	    }
	    potentialMy += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialMy >= WIN) {
	     rowsValue[p.y] += evalPrice(IO.FIELD_MY, countMy, capped);
          }
	  break;
	case IO.FIELD_FORBIDDEN:
          x++; 
      }
    }

    /* Row check */
    capped = false; 
    x = p.x + 1;
    y = 1;
    potentialOpponent = potentialMy = 0;
    countEmpty = 0;

    while (y < table.length) {
      capped = false;
      switch (getRaw(x, y)) {
        case IO.FIELD_EMPTY: 
	  countEmpty = 0;
	  while (getRaw(x, y) == IO.FIELD_EMPTY) {
            countEmpty++;
	    y++;
	  }
	  break;
	case IO.FIELD_OPPONENT:
	  countOpponent = 0; 
	  potentialOpponent = 0;
	  if (getRaw(x, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialOpponent = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_OPPONENT) {
            countOpponent++;
	    potentialOpponent++;
	    y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      y++;
	    }
	    potentialOpponent += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialOpponent >= WIN) {
             colsValue[p.x] += evalPrice(IO.FIELD_OPPONENT, countOpponent, capped);
          }
	  break;
	case IO.FIELD_MY:
	  countMy = 0; 
	  potentialMy = 0;
	  if (getRaw(x, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialMy = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_MY) {
            countMy++;
	    potentialMy++;
	    y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      y++;
	    }
	    potentialMy += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialMy >= WIN) {
             colsValue[p.x] += evalPrice(IO.FIELD_MY, countMy, capped);
          }
	  break;
	case IO.FIELD_FORBIDDEN:
          y++; 
      }
    }

    /* Diagonal check 1 */
    capped = false; 
    x = p.x + 1;
    y = p.y + 1; 
    int min = Math.min(x, y);
    x -= min;
    y -= min;
    potentialOpponent = potentialMy = 0;
    countEmpty = 0;
    
    while (x < table[0].length &&  y < table.length) {
      capped = false;
      switch (getRaw(x, y)) {
        case IO.FIELD_EMPTY: 
	  countEmpty = 0;
	  while (getRaw(x, y) == IO.FIELD_EMPTY) {
            countEmpty++;
	    x++; y++;
	  }
	  break;
	case IO.FIELD_OPPONENT:
	  countOpponent = 0; 
	  potentialOpponent = 0;
	  if (getRaw(x-1, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialOpponent = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_OPPONENT) {
            countOpponent++;
	    potentialOpponent++;
	    x++; y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x++; y++;
	    }
	    potentialOpponent += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialOpponent >= WIN) {
             diag1Value[p.x] += evalPrice(IO.FIELD_OPPONENT, countOpponent, capped);
          }
	  break;
	case IO.FIELD_MY:
	  countMy = 0; 
	  potentialMy = 0;
	  if (getRaw(x-1, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialMy = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_MY) {
            countMy++;
	    potentialMy++;
	    x++; y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x++; y++;
	    }
	    potentialMy += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialMy >= WIN) {
             diag1Value[p.x] += evalPrice(IO.FIELD_MY, countMy, capped);
          }
	  break;
	case IO.FIELD_FORBIDDEN:
          x++; y++; 
      }
    }

    /* Diagonal check 2 - don't try to get this indexes */

    capped = false; 
    x = p.x;
    y = p.y; 

    min = Math.min((table[0].length-2)-x, y);
    
    x += min;
    y -= min;

    x++; y++;
    potentialOpponent = potentialMy = 0;
    countEmpty = 0;
    
    while (x > 0 &&  y < table.length) {
      capped = false;
      switch (getRaw(x, y)) {
        case IO.FIELD_EMPTY: 
	  countEmpty = 0;
	  while (getRaw(x, y) == IO.FIELD_EMPTY) {
            countEmpty++;
	    x--; y++;
	  }
	  break;
	case IO.FIELD_OPPONENT:
	  countOpponent = 0; 
	  potentialOpponent = 0;
	  if (getRaw(x+1, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialOpponent = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_OPPONENT) {
            countOpponent++;
	    potentialOpponent++;
	    x--; y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x--; y++;
	    }
	    potentialOpponent += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialOpponent >= WIN) {
             diag2Value[p.x] += evalPrice(IO.FIELD_OPPONENT, countOpponent, capped);
          }
	  break;
	case IO.FIELD_MY:
	  countMy = 0; 
	  potentialMy = 0;
	  if (getRaw(x+1, y-1) == IO.FIELD_EMPTY) {
            capped = false;
	    potentialMy = countEmpty;
	  } else {
	    capped = true;
	  }
	  while (getRaw(x, y) == IO.FIELD_MY) {
            countMy++;
	    potentialMy++;
	    x--; y++;
	  }
	  if (getRaw(x, y) == IO.FIELD_EMPTY) {
	    countEmpty = 0;
	    while (getRaw(x, y) == IO.FIELD_EMPTY) {
              countEmpty++;
	      x--; y++;
	    }
	    potentialMy += countEmpty;
	  } else {
            capped = true;
          }
	  if (potentialMy >= WIN) {
             diag2Value[p.x] += evalPrice(IO.FIELD_MY, countMy, capped);
          }
	  break;
	case IO.FIELD_FORBIDDEN:
          x--; y++; 
      }
    }
  }

  public int get(final int x, final int y) {
  	return table[(x+1)][(table.length - y - 2)];
  }
  
  public void set(final int x, final int y, final int sign) {
  	table[(x+1)][(table.length - y - 2)] = sign;
  }

  public int getRaw(final int x, final int y) {
  	return table[x][(table.length - y - 1)];
  }

  public void setRaw(final int x, final int y, final int sign) {
  	table[x][(table.length - y - 1)] = sign;
  }

  /* If player puts sign on position x, y what will happen? */
  /* Returns: IO.RESULT_WON - player wins the game,
   * IO.RESULT_NONE - nothing, or IO.RESULT_DRAW the game is a draw.
   */
  public int checkState(final int x, final int y, final int movesLeft) {
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

    if (movesLeft <= 0) {
      return IO.RESULT_DRAW;
    }

    return IO.RESULT_NONE;
  }  
}
