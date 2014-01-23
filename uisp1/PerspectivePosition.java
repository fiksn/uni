public class PerspectivePosition extends Position implements Comparable {
  public boolean isPerspective;
  public int freeNeighbours;

  protected Object clone() {
    PerspectivePosition a = new PerspectivePosition();
    a.x = this.x; a.y = this.y; a.isPerspective = this.isPerspective;
    return (Object)a;
  }

  public PerspectivePosition() {
    super();
    this.isPerspective = false;
    this.freeNeighbours = 0;
  }

  public PerspectivePosition(int x, int y) {
    super(x, y);
    this.isPerspective = false;
    this.freeNeighbours = 0;
  }

  /* Perspective positions are always better, else the number of empty fields around counts */
  public int compareTo(Object a) {
    final int ME_SMALLER = -1;
    final int EQUAL = 0;
    final int ME_BIGGER = 1;

    PerspectivePosition other = (PerspectivePosition)a;

    /* Elements can be the same only when they have same x, y */
    if ((other.x == this.x) && (other.y == this.y)) {
      return EQUAL;
    }

    int myValue = (this.isPerspective) ? (50) : (0);
    int otherValue = (other.isPerspective) ? (50) : (0);

    myValue += (this.freeNeighbours);
    otherValue += (this.freeNeighbours);

    /* Better elements are before */
    if (otherValue > myValue) {
      /* Ordering will be (..., other, me, ...) */
      return ME_BIGGER;
    } else {
      return ME_SMALLER;
    }
  }
}

