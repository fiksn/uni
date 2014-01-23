package sem2;

public class TestVisitor extends Visitor {
  public void visit(TSymbol sym) {
    System.out.println(sym.stringValue);
  }
}