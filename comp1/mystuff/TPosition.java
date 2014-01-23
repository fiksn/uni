package sem2;

public class TPosition {
  public int lineNumber;
  public int lineOffset;
  public int beginOffset;

  TPosition(int lineNumber, int lineOffset, int beginOffset) {
    this.lineNumber = lineNumber;
    this.lineOffset = lineOffset;
    this.beginOffset = beginOffset;
  }
}