abstract class Player {
  /* Every player must implement an opponent() method */
  abstract void opponent(int x, int y);

  /* The player calls move(int x, int y); */

  protected String ID() {
    return "OK";
  }
}
