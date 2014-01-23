package sem2;

abstract class Visitor {
  abstract void visit(TSymbol current);

  public static void topDownVisit(TSymbol root, String symbolName, Visitor v) {
    if (root == null || root.stringSymbol == null) {
      return;
    }

    if (root.stringSymbol.equals(symbolName)) {
      v.visit(root);
    }
  
    if (root.children == null) {
      return;
    }

    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        topDownVisit(root.children[i], symbolName, v);
      }
    }  
  }

  public static void topDownVisit(TSymbol root, int sym, Visitor v) {
    if (root.sym == sym) {
      v.visit(root);
      /* Terminal symbol doesn't have children per definition */
      return;
    }

    if (root.children == null) {
      return;
    }
  
    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        topDownVisit(root.children[i], sym, v);
      }
    }  
  }

  public static void topDownVisit(TSymbol root, String[] symbolNames, Visitor[] v) {
    int i;

    if (root == null || root.stringSymbol == null) {
      return;
    }

    for (i=0; i<symbolNames.length; ++i) {
      if (root.stringSymbol.equals(symbolNames[i])) {
        if (i < v.length) {
          v[i].visit(root);
        }
      }
    }
  
    if (root.children == null) {
      return;
    }

    for (i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        topDownVisit(root.children[i], symbolNames, v);
      }
    }  
  }


  public static void bottomUpVisit(TSymbol root, String symbolName, Visitor v) {
    if (root == null || root.stringSymbol == null) {
      return;
    }
 
    if (root.children == null) {
      return;
    }

    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        bottomUpVisit(root.children[i], symbolName, v);
      }
    }  
    
    if (root.stringSymbol.equals(symbolName)) {
      v.visit(root);
    }
  }

  public static void bottomUpVisit(TSymbol root, int sym, Visitor v) {
    if (root.children == null) {
      return;
    }
  
    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        bottomUpVisit(root.children[i], sym, v);
      }
    } 

    if (root.sym == sym) {
      v.visit(root);
    }
  }

  public static void bottomUpVisit(TSymbol root, String[] symbolNames, Visitor[] v) {
    int i;

    if (root == null || root.stringSymbol == null) {
      return;
    }

 
    if (root.children == null) {
      return;
    }

    for (i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        bottomUpVisit(root.children[i], symbolNames, v);
      }
    }  

    for (i=0; i<symbolNames.length; ++i) {
      if (root.stringSymbol.equals(symbolNames[i])) {
        if (i < v.length) {
          v[i].visit(root);
        }
      }
    }
  }

  public static TSymbol findFirst(TSymbol root, String symbolName) {
    TSymbol result = (TSymbol)null;

    if (root == null || root.stringSymbol == null) {
      return null;
    }

    if (root.stringSymbol.equals(symbolName)) {
      return root;
    }
  
    if (root.children == null) {
      return null;
    }

    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        result = findFirst(root.children[i], symbolName);
        if (result != null) {
          return result;
        }
      }
    }  

    return result;
  }

  public static TSymbol findFirst(TSymbol root, int sym) {
    TSymbol result = (TSymbol)null;

    if (root == null) {
      return null;
    }

    if (root.sym == sym) {
      return root;
    }
  
    if (root.children == null) {
      return null;
    }

    for (int i=0; i<root.children.length; ++i) {
      if (root.children[i] != null) {
        result = findFirst(root.children[i], sym);
        if (result != null) {
          return result;
        }
      }
    }  

    return result;
  }
}