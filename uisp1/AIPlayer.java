import java.util.*;

public class AIPlayer extends Player {
  private IO io;
  private ArrayList validMoves = new ArrayList();
  private ArrayList myMoves = new ArrayList();
  private ArrayList opponentMoves = new ArrayList();
  private Bounds myBounds = null;
  private Board myBoard = null;
  private ArrayList bestMoves = null;

  static final int MAXWIN = 1000000;
  static final int MINWIN = -MAXWIN;

  /* First square 5x5 at the centre (2 around the center field) */
  static final int FIRST_SQUARE = 2;

  /* Minimax depth */
  static int NORMAL_MINIMAX_DEPTH = 2;
  static int MAX_MINIMAX_DEPTH = 4;
  static int BIGGER_DEPTH = 40;

  static int MINIMAX_DEPTH;

  /* How many last position neighbours are considered perspective */
  static final int PERSPECTIVE_LAST = 3;

  /* How many fields around the bounds are still ok? */
  static final int AROUND_BOUNDS = 3;

  public static void main(String[] args) {
    int n = 0;
    int m = 0;

    if (args.length < 1) {
      System.err.println("Sintaksa: program [1|2] [pussy|normal|hard] [depth]\n1 pomeni da zacnem jaz, "
		         +"2 zacne nasprotnik");
      return;
    }

    n = Integer.parseInt(args[0]);
    if (n < 1 || n > 2) {
      System.err.println("Neveljavna stevilka!");
      return;
    }

    if (args[1].equalsIgnoreCase("PUSSY")) {
      NORMAL_MINIMAX_DEPTH = 2;
      MAX_MINIMAX_DEPTH = 2;
    } else if (args[1].equalsIgnoreCase("NORMAL")) {
      NORMAL_MINIMAX_DEPTH = 2;
      MAX_MINIMAX_DEPTH = 4;
    } else if (args[1].equalsIgnoreCase("HARD")) {
      NORMAL_MINIMAX_DEPTH = 4;
      MAX_MINIMAX_DEPTH = 4;
    }

    if (args.length >= 3) {
      m = Integer.parseInt(args[2]);
      if (m > 1 && m < 100) {
        BIGGER_DEPTH = m;
      }
    } 

    new AIPlayer(n);
  } 

  public AIPlayer(int n) {
    io = new IO(this, n==1);
    myBoard = new Board();
    initValidMoves();
    io.start();
  }

  /* Initalization of valid moves */ 
  private void initValidMoves() {
    validMoves.add(new PerspectivePosition(7,7));
    validMoves.add(new PerspectivePosition(6,6));
    validMoves.add(new PerspectivePosition(7,6));
    validMoves.add(new PerspectivePosition(8,6));
    validMoves.add(new PerspectivePosition(6,7));
    validMoves.add(new PerspectivePosition(8,7));
    validMoves.add(new PerspectivePosition(6,8));
    validMoves.add(new PerspectivePosition(7,8));
    validMoves.add(new PerspectivePosition(8,8));
    validMoves.add(new PerspectivePosition(5,5));
    validMoves.add(new PerspectivePosition(6,5));
    validMoves.add(new PerspectivePosition(7,5));
    validMoves.add(new PerspectivePosition(8,5));
    validMoves.add(new PerspectivePosition(9,5));
    validMoves.add(new PerspectivePosition(5,6));
    validMoves.add(new PerspectivePosition(9,6));
    validMoves.add(new PerspectivePosition(5,7));
    validMoves.add(new PerspectivePosition(9,7));
    validMoves.add(new PerspectivePosition(5,8));
    validMoves.add(new PerspectivePosition(9,8));
    validMoves.add(new PerspectivePosition(5,9));
    validMoves.add(new PerspectivePosition(6,9));
    validMoves.add(new PerspectivePosition(7,9));
    validMoves.add(new PerspectivePosition(8,9));
    validMoves.add(new PerspectivePosition(9,9));
    validMoves.add(new PerspectivePosition(4,4));
    validMoves.add(new PerspectivePosition(5,4));
    validMoves.add(new PerspectivePosition(6,4));
    validMoves.add(new PerspectivePosition(7,4));
    validMoves.add(new PerspectivePosition(8,4));
    validMoves.add(new PerspectivePosition(9,4));
    validMoves.add(new PerspectivePosition(10,4));
    validMoves.add(new PerspectivePosition(4,5));
    validMoves.add(new PerspectivePosition(10,5));
    validMoves.add(new PerspectivePosition(4,6));
    validMoves.add(new PerspectivePosition(10,6));
    validMoves.add(new PerspectivePosition(4,7));
    validMoves.add(new PerspectivePosition(10,7));
    validMoves.add(new PerspectivePosition(4,8));
    validMoves.add(new PerspectivePosition(10,8));
    validMoves.add(new PerspectivePosition(4,9));
    validMoves.add(new PerspectivePosition(10,9));
    validMoves.add(new PerspectivePosition(4,10));
    validMoves.add(new PerspectivePosition(5,10));
    validMoves.add(new PerspectivePosition(6,10));
    validMoves.add(new PerspectivePosition(7,10));
    validMoves.add(new PerspectivePosition(8,10));
    validMoves.add(new PerspectivePosition(9,10));
    validMoves.add(new PerspectivePosition(10,10));
    validMoves.add(new PerspectivePosition(4,3));
    validMoves.add(new PerspectivePosition(5,3));
    validMoves.add(new PerspectivePosition(6,3));
    validMoves.add(new PerspectivePosition(7,3));
    validMoves.add(new PerspectivePosition(8,3));
    validMoves.add(new PerspectivePosition(9,3));
    validMoves.add(new PerspectivePosition(10,3));
    validMoves.add(new PerspectivePosition(3,4));
    validMoves.add(new PerspectivePosition(11,4));
    validMoves.add(new PerspectivePosition(3,5));
    validMoves.add(new PerspectivePosition(11,5));
    validMoves.add(new PerspectivePosition(3,6));
    validMoves.add(new PerspectivePosition(11,6));
    validMoves.add(new PerspectivePosition(3,7));
    validMoves.add(new PerspectivePosition(11,7));
    validMoves.add(new PerspectivePosition(3,8));
    validMoves.add(new PerspectivePosition(11,8));
    validMoves.add(new PerspectivePosition(3,9));
    validMoves.add(new PerspectivePosition(11,9));
    validMoves.add(new PerspectivePosition(3,10));
    validMoves.add(new PerspectivePosition(11,10));
    validMoves.add(new PerspectivePosition(4,11));
    validMoves.add(new PerspectivePosition(5,11));
    validMoves.add(new PerspectivePosition(6,11));
    validMoves.add(new PerspectivePosition(7,11));
    validMoves.add(new PerspectivePosition(8,11));
    validMoves.add(new PerspectivePosition(9,11));
    validMoves.add(new PerspectivePosition(10,11));
    validMoves.add(new PerspectivePosition(4,2));
    validMoves.add(new PerspectivePosition(5,2));
    validMoves.add(new PerspectivePosition(6,2));
    validMoves.add(new PerspectivePosition(7,2));
    validMoves.add(new PerspectivePosition(8,2));
    validMoves.add(new PerspectivePosition(9,2));
    validMoves.add(new PerspectivePosition(10,2));
    validMoves.add(new PerspectivePosition(2,4));
    validMoves.add(new PerspectivePosition(12,4));
    validMoves.add(new PerspectivePosition(2,5));
    validMoves.add(new PerspectivePosition(12,5));
    validMoves.add(new PerspectivePosition(2,6));
    validMoves.add(new PerspectivePosition(12,6));
    validMoves.add(new PerspectivePosition(2,7));
    validMoves.add(new PerspectivePosition(12,7));
    validMoves.add(new PerspectivePosition(2,8));
    validMoves.add(new PerspectivePosition(12,8));
    validMoves.add(new PerspectivePosition(2,9));
    validMoves.add(new PerspectivePosition(12,9));
    validMoves.add(new PerspectivePosition(2,10));
    validMoves.add(new PerspectivePosition(12,10));
    validMoves.add(new PerspectivePosition(4,12));
    validMoves.add(new PerspectivePosition(5,12));
    validMoves.add(new PerspectivePosition(6,12));
    validMoves.add(new PerspectivePosition(7,12));
    validMoves.add(new PerspectivePosition(8,12));
    validMoves.add(new PerspectivePosition(9,12));
    validMoves.add(new PerspectivePosition(10,12));
    validMoves.add(new PerspectivePosition(4,1));
    validMoves.add(new PerspectivePosition(5,1));
    validMoves.add(new PerspectivePosition(6,1));
    validMoves.add(new PerspectivePosition(7,1));
    validMoves.add(new PerspectivePosition(8,1));
    validMoves.add(new PerspectivePosition(9,1));
    validMoves.add(new PerspectivePosition(10,1));
    validMoves.add(new PerspectivePosition(1,4));
    validMoves.add(new PerspectivePosition(13,4));
    validMoves.add(new PerspectivePosition(1,5));
    validMoves.add(new PerspectivePosition(13,5));
    validMoves.add(new PerspectivePosition(1,6));
    validMoves.add(new PerspectivePosition(13,6));
    validMoves.add(new PerspectivePosition(1,7));
    validMoves.add(new PerspectivePosition(13,7));
    validMoves.add(new PerspectivePosition(1,8));
    validMoves.add(new PerspectivePosition(13,8));
    validMoves.add(new PerspectivePosition(1,9));
    validMoves.add(new PerspectivePosition(13,9));
    validMoves.add(new PerspectivePosition(1,10));
    validMoves.add(new PerspectivePosition(13,10));
    validMoves.add(new PerspectivePosition(4,13));
    validMoves.add(new PerspectivePosition(5,13));
    validMoves.add(new PerspectivePosition(6,13));
    validMoves.add(new PerspectivePosition(7,13));
    validMoves.add(new PerspectivePosition(8,13));
    validMoves.add(new PerspectivePosition(9,13));
    validMoves.add(new PerspectivePosition(10,13));
    validMoves.add(new PerspectivePosition(4,0));
    validMoves.add(new PerspectivePosition(5,0));
    validMoves.add(new PerspectivePosition(6,0));
    validMoves.add(new PerspectivePosition(7,0));
    validMoves.add(new PerspectivePosition(8,0));
    validMoves.add(new PerspectivePosition(9,0));
    validMoves.add(new PerspectivePosition(10,0));
    validMoves.add(new PerspectivePosition(0,4));
    validMoves.add(new PerspectivePosition(14,4));
    validMoves.add(new PerspectivePosition(0,5));
    validMoves.add(new PerspectivePosition(14,5));
    validMoves.add(new PerspectivePosition(0,6));
    validMoves.add(new PerspectivePosition(14,6));
    validMoves.add(new PerspectivePosition(0,7));
    validMoves.add(new PerspectivePosition(14,7));
    validMoves.add(new PerspectivePosition(0,8));
    validMoves.add(new PerspectivePosition(14,8));
    validMoves.add(new PerspectivePosition(0,9));
    validMoves.add(new PerspectivePosition(14,9));
    validMoves.add(new PerspectivePosition(0,10));
    validMoves.add(new PerspectivePosition(14,10));
    validMoves.add(new PerspectivePosition(4,14));
    validMoves.add(new PerspectivePosition(5,14));
    validMoves.add(new PerspectivePosition(6,14));
    validMoves.add(new PerspectivePosition(7,14));
    validMoves.add(new PerspectivePosition(8,14));
    validMoves.add(new PerspectivePosition(9,14));
    validMoves.add(new PerspectivePosition(10,14));
    /*
    System.out.println("Valid moves count "+validMoves.size());
    try { System.in.read(); } catch (Exception e) {}
    */
  }
  
  /* Return how good a move is */
  private int miniMax(final int depth, final int maxDepth,
    Position position, final int sign, int alpha, int beta) {
    int r, c, d1, d2;
    	
    Position dummy = new Position(-1, -1);
    Position move;
    int oldSign; 
    int value;
    int state;
    
    if (depth == maxDepth) {
      return myBoard.boardValue();
    }

    /* Go through all valid positions */
    int i = 0;
    while (i < bestMoves.size()){
      move = (PerspectivePosition)(bestMoves.get(i));
    	
      oldSign = myBoard.get(move.x, move.y);

      /* Remember old values from Board */
      r = myBoard.rowsValue[move.y]; 
      c = myBoard.colsValue[move.x]; 
      d1 = myBoard.diag1Value[move.x]; 
      d2 = myBoard.diag2Value[move.x]; 

      myBoard.set(move.x, move.y, sign);
      myBoard.updatePrice(move);
      bestMoves.remove(move);
      
      if (sign == io.FIELD_MY) {
      	/* My move */
	state = myBoard.checkState(move.x, move.y, bestMoves.size());
      	if (state == IO.RESULT_WON) {
          value = MAXWIN;
	} else if (state == IO.RESULT_DRAW) {
          value = 0;
	} else {
          value = miniMax(depth + 1, maxDepth, dummy,
                       io.FIELD_OPPONENT, alpha, beta);
      	}
      	
      	if (value > alpha) {
      	  position.x = move.x; position.y = move.y; 
      	  alpha = value;
      	}
          
        if (alpha >= beta) {
          myBoard.set(move.x, move.y, oldSign);
	  /* Return all old values */
	  myBoard.rowsValue[move.y] = r;
          myBoard.colsValue[move.x] = c;
	  myBoard.diag1Value[move.x] = d1;
	  myBoard.diag2Value[move.x] = d2;

          bestMoves.add(i, move.clone());      
          return alpha;
        }
      } else {
      	/* Opponent move */
	state = myBoard.checkState(move.x, move.y, bestMoves.size());
      	if (state == IO.RESULT_WON) {
          value = MINWIN;
	} else if (state == IO.RESULT_DRAW) {
          value = 0;
      	} else {
          value = miniMax(depth + 1, maxDepth, dummy,
                io.FIELD_MY, alpha, beta);
        }
      
        if (value < beta) {
	  position.x = move.x; position.y = move.y; 
      	  beta = value;
      	}
      	
        if (alpha >= beta) {
          myBoard.set(move.x, move.y, oldSign);
	  /* Return all values */
	  myBoard.rowsValue[move.y] = r;
          myBoard.colsValue[move.x] = c;
	  myBoard.diag1Value[move.x] = d1;
	  myBoard.diag2Value[move.x] = d2;
          bestMoves.add(i, move.clone());      
          return beta;
        }
      }
      
      myBoard.set(move.x, move.y, oldSign);
      /* Return all old values */
      myBoard.rowsValue[move.y] = r;
      myBoard.colsValue[move.x] = c;
      myBoard.diag1Value[move.x] = d1;
      myBoard.diag2Value[move.x] = d2;
      bestMoves.add(i, move.clone());
            
      i++;
    }
    
    return (sign == io.FIELD_MY) ? alpha : beta;
  }
    
  /* Set my sign on position p */
  public void setMySign(Position p) {
    myBoard.set(p.x, p.y, io.FIELD_MY);
    validMoves.remove(new PerspectivePosition(p.x, p.y));
    myMoves.add(p.clone());
    myBounds.update(p);

    myBoard.updatePrice(p);

    System.out.println("Dajem znak na polje: "+p);
    io.move(p.x, p.y);
  }

  /* Does the position p have any free neighbours? */
  private int countFreeNeighbours(Position p) {
    int count = 0;
    int sign; 
    for (int y=Math.max(p.y-1, 0); y<=Math.min(p.y+1, io.getFieldHeight()-1); ++y) {
      for (int x=Math.max(p.x-1, 0); x<=Math.min(p.x+1, io.getFieldWidth()-1); ++x) {
	if ((p.x == x) && (p.y == y)) {
          continue;
	}
	sign = myBoard.get(x, y);
	switch (sign) {
          case IO.FIELD_EMPTY:
            count++;
          break;
	  case IO.FIELD_MY:
            count+=2;
	  break;
	}
      }
    }
    return count;
  }

  /* Add all neighbours (perspective) to the set */
  private void addNeighbours(ArrayList a, Position p) {
    PerspectivePosition newP;
    for (int y=Math.max(p.y-1, 0); y<=Math.min(p.y+1, io.getFieldHeight()-1); ++y) {
      for (int x=Math.max(p.x-1, 0); x<=Math.min(p.x+1, io.getFieldWidth()-1); ++x) {
	/* Better safe than sorry */
	if ((x == p.x) && (y == p.y)) {
          continue;
	}
	if (myBoard.get(x, y) != IO.FIELD_EMPTY) {
          continue;
        }
	newP = new PerspectivePosition(x, y);
	newP.isPerspective = true;
	a.add(newP);
      }
    }
  }

  /* Construct an array of valid positions */
  private ArrayList getBestPositions() {
    int i, j;
    Position p;
    PerspectivePosition newP;
    ArrayList temp = new ArrayList();

    /*
    ArrayList dummy = new ArrayList();
    */

    /* Add all neighbours of last positions as perspective */
    for (i=opponentMoves.size()-1, j=1 ; (i >= 0) && (j <= PERSPECTIVE_LAST); --i, ++j) {
      p = (Position)(opponentMoves.get(i));
      addNeighbours(temp, p);
    }
    for (i=myMoves.size()-1, j=1 ; (i >= 0) && (j <= PERSPECTIVE_LAST); --i, ++j) {
      p = (Position)(myMoves.get(i));
      addNeighbours(temp, p);
    }

    removeDuplicates(temp);

    for (i=0; i<validMoves.size(); ++i) {
      p = (PerspectivePosition)validMoves.get(i);
      /* Position p is not in the position where the game is happening */
      if (!myBounds.inside(p)) {
        continue;
      }
      /* Better safe than sorry */
      if (myBoard.get(p.x, p.y) != io.FIELD_EMPTY) {
        continue;
      }
      newP = (PerspectivePosition)p.clone();
      newP.freeNeighbours = countFreeNeighbours(p);
      temp.add(newP); 
      /* dummy.add(newP); */
    }

    /* Sort temp */
    Collections.sort(temp);
    removeDuplicates(temp);

    /* If there are not many elements increase depth */

    if (temp.size() < BIGGER_DEPTH) {
       MINIMAX_DEPTH = Math.min(MAX_MINIMAX_DEPTH, NORMAL_MINIMAX_DEPTH+2);
    } else {
       MINIMAX_DEPTH = Math.min(MAX_MINIMAX_DEPTH, NORMAL_MINIMAX_DEPTH);
    }

    /* System.out.println("dummy = "+dummy.size()+" temp = "+temp.size()); */
    return temp; 
  }

  private void removeDuplicates(ArrayList a) {
    PerspectivePosition p;
    int index;
    for (int i=0; i<a.size()-1; ++i) {
      p = (PerspectivePosition)a.get(i);
      while ((index = a.lastIndexOf(p)) > i) {
        a.remove(index);
      }
    }
  }

  /* Result of the game */
  private void draw() {
    System.out.println("Igra se je koncala z remijem!");
    System.exit(0);
  }

  private void lost() {
    System.out.println("Izgubil sem!");
    System.exit(-1);
  }

  private void won() {
    System.out.println("Zmagal sem!");
    System.exit(1);
  }

  public void opponent(final int x, final int y) {
    int i, myX, myY;
    int value;
    Position p = new Position(-1, -1);

    if (x == io.MOVE_ILLEGAL) {
      System.out.println("Nasprotnik je naredil prepovedano potezo!");
      return;
    }
    
    if (x == io.MOVE_VOID) {
      /* I'm the starting - some random move in the middle of the field */ 
      p.x = (io.getFieldWidth()-1)/2; p.y = (io.getFieldHeight()-1)/2;  
      p.x += (int)((Math.random()*(2*FIRST_SQUARE))-FIRST_SQUARE);
      p.y += (int)((Math.random()*(2*FIRST_SQUARE))-FIRST_SQUARE);
      System.out.println("Jaz zacenjam z "+p);
      myBounds = new Bounds(p);
      setMySign(p); 
    } else {
      p.x = x; p.y = y;
      System.out.println("Nasprotnik je naredil potezo "+x+" "+y);
      if (io.getMoveNum() == 1) {
        /* Opponents first move */
	myBounds = new Bounds(p);
      } else {
	myBounds.update(p);
      }

      myBoard.set(x, y, io.FIELD_OPPONENT);
      if (myBoard.checkState(x, y, 1) == IO.RESULT_WON) {
        lost();
      }
      validMoves.remove(new PerspectivePosition(x, y));
      opponentMoves.add(new Position(x, y)); 

      if (validMoves.size() <= 0) {
        draw();
      } 
      
      myBoard.updatePrice(p);

      bestMoves = getBestPositions();

      p.x = IO.MOVE_ILLEGAL; p.y = IO.MOVE_ILLEGAL;
      /* MINWIN-1 = -INFINITY    MAXWIN+1 = +INFINITY */
      value = miniMax(0, MINIMAX_DEPTH, p, io.FIELD_MY, MINWIN-1, MAXWIN+1);
      System.out.println("Minimax returned: "+value+" Position: "+p);
      if (p.x == IO.MOVE_ILLEGAL) {
        System.out.println("Minimax returned with value: "+value+" and didn't change position!");
	value = 0;
        io.move(p.x, p.y);  
      } else {
        setMySign(p); 
      }

      if (value == MAXWIN) {
	/* We might not have won yet */
	if (myBoard.checkState(p.x, p.y, 1) == IO.RESULT_WON) { 
	  won();
	}
      }

      if (io.getMoveNum() >= io.getTotalMoves()) {
        draw();
      }
    }
  }

  private class Bounds {
    public int left; 
    public int right;
    public int up;
    public int down;

    public Bounds(Position p) {
      this.left = p.x; 
      this.right = p.x;
      this.up = p.y;
      this.down = p.y;
    }

    public int getLeft() {
      return (left-AROUND_BOUNDS < 0) ? (0) : (left-AROUND_BOUNDS);
    }

    public int getRight() {
      return (right+AROUND_BOUNDS > io.getFieldWidth()-1) ? (io.getFieldWidth()-1) : (right+AROUND_BOUNDS);
    }

    public int getUp() {
      return (up-AROUND_BOUNDS < 0) ? (0) : (up-AROUND_BOUNDS);
    }

    public int getDown() {
      return (down+AROUND_BOUNDS > io.getFieldHeight()-1) ? (io.getFieldHeight()-1) : (down+AROUND_BOUNDS);
    }

    public void update(Position p) {
      if (p.x < this.left) {
        this.left = p.x;
      }

      if (p.x > this.right) {
        this.right = p.x;
      }

      if (p.y < this.up) {
        this.up = p.y;
      }

      if (p.y > this.down) {
        this.down = p.y;
      }
    }

    public boolean inside(Position p) {
      /*
      System.out.println(this.getLeft()+" "+this.getRight()+" "+this.getUp()+" "+this.getDown());
      */
      if ((p.x >= this.getLeft()) && (p.x <= this.getRight()) && 
	  (p.y >= this.getUp()) && (p.y <= this.getDown())) {
        return true;
      }
      return false;
    }
  }
}
