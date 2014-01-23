public class Position {
  public int x;
  public int y;
  
  public Position(int x, int y) {
    this.x = x; this.y = y;
  }
  public Position() {
    this.x = 0; this.y = 0;
  }

  public boolean equals(Object o) {
    Position p = (Position)o;
    return ((this.x == p.x) && (this.y == p.y));
  }

  protected Object clone() {
    Position a = new Position();
    a.x = this.x; a.y = this.y;
    return (Object)a;
  }

  public String toString() {
    return "("+this.x+", "+this.y+")";
  }
}

