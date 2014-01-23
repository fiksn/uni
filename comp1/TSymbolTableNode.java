package sem2;
import java.util.*;
import java.io.*;

class TSymbolTableElement {
  public String name;
  public int kind;
  /* functions and variables have some return type */
  public String type;

  public Object extra;

  public TSymbolTableElement(String name, int kind, String type) {
     this.name = name;
     this.kind = kind;
     this.type = type;
  }
}

public class TSymbolTableNode  {
  public static final int SYMTABLE_TYPE = 1;
  public static final int SYMTABLE_TYPEALIAS = 2;
  public static final int SYMTABLE_VARIABLE = 3;
  public static final int SYMTABLE_FUNCTION = 4;
  public static final int SYMTABLE_PROCEDURE = 5;
  public static final int SYMTABLE_VALUEPARAMETER = 6;
  public static final int SYMTABLE_VARPARAMETER = 7;
  public static final int SYMTABLE_LABEL = 8;
  public static final int SYMTABLE_CONST = 9;

  public ArrayList symbols;

  public TSymbolTableNode parent;
  public ArrayList children;

  public TSymbolTableNode() {
    symbols = new ArrayList();
    children = new ArrayList();
    parent = null;
  }

  public void add(String name, int kind) {
    add(name, kind, null);
  }

  public void add(String name, int kind, String type) {
    String bigName = name.toUpperCase();
    TSymbolTableElement symbol;
    for (int i=0; i<symbols.size(); ++i) {
      symbol = (TSymbolTableElement)symbols.get(i);
      if (symbol.name.equals(bigName)) {
        System.err.println("Identifier '"+name+"' already exists.");
        return;
      }
    }
    symbols.add(new TSymbolTableElement(bigName, kind, type));
  }

  public TSymbolTableElement get(String name) {
    return getInternal(name.toUpperCase(), this);
  }

  public TSymbolTableElement get(String name, int kind) {
    return getInternal(name.toUpperCase(), kind, this);
  }

  private TSymbolTableElement getInternal(String name, TSymbolTableNode current) {
    TSymbolTableElement symbol;
    for (int i=0; i<current.symbols.size(); ++i) {
      symbol = (TSymbolTableElement)current.symbols.get(i);
      if (symbol.name.equals(name)) {
        return symbol;
      } 
    }
    
    if (current.parent == null) {
      return null;
    }

    return getInternal(name, current.parent);
  }
  
  private TSymbolTableElement getInternal(String name, int kind, TSymbolTableNode current) {
    TSymbolTableElement symbol;
    for (int i=0; i<current.symbols.size(); ++i) {
      symbol = (TSymbolTableElement)current.symbols.get(i);      
      if (symbol.kind == kind && symbol.name.equals(name)) {
        return symbol;
      } 
    }

    if (current.parent == null) {
      return null;
    }

    return getInternal(name, kind, current.parent);
  }

  public String resolveType(String type) {
    final int MAX_RECURSION = 5;
    String bigType = type.toUpperCase();
    TSymbolTableElement temp;
    int i;

    i = 0;
    while (i < MAX_RECURSION) {
      temp = getInternal(bigType, TSymbolTableNode.SYMTABLE_TYPE, this);
      if (temp != null) {
        return temp.name;
      }
      temp = getInternal(bigType, TSymbolTableNode.SYMTABLE_TYPEALIAS, this);
      if (temp != null) {
        bigType = temp.type;
      }
      i++;
    }

    return null;
  }

  public TSymbolTableNode newLevel() {
    TSymbolTableNode temp = new TSymbolTableNode();
    temp.parent = this;
    this.children.add(temp);
    return temp;
  }

  /* Output */
  public static String escape(String text) {
    StringBuffer temp = new StringBuffer(text.length()*2);
    for (int i=0; i<text.length(); ++i) {
      switch (text.charAt(i)) {
        case '<':
          temp.append("&lt;");  
          break;
        case '>':
          temp.append("&gt;");  
          break;
        case '"':
          temp.append("&quot;");  
          break;
        case '\'':
          temp.append("&#039;");  
          break;
        case '\\':
          temp.append("&#092;");  
          break;
        case '&':
          temp.append("&amp;");  
          break;
        default:
          temp.append(text.charAt(i));
      }
    }
    return temp.toString();
  }
  

  /* Output the tree starting from this node to stream */
  public void output(PrintStream stream, String offset) {
    int i;
    final String once = "  ";
    TSymbolTableNode next;
    TSymbolTableElement symbol;
    stream.println(offset+"<symbolTableNode>");
    stream.println(offset+once+"<symbols>");
    for (i=0; i<symbols.size(); ++i) {    
      symbol = (TSymbolTableElement)symbols.get(i);
      stream.println(offset+once+once+"<symbol>");  
      stream.println(offset+once+once+once+"<kind>");
      stream.println(offset+once+once+once+once+symbol.kind);
      stream.println(offset+once+once+once+"</kind>");
      stream.println(offset+once+once+once+"<name>");
      stream.println(offset+once+once+once+once+symbol.name);
      stream.println(offset+once+once+once+"</name>");
      stream.println(offset+once+once+once+"<type>");
      stream.println(offset+once+once+once+once+symbol.type);
      stream.println(offset+once+once+once+"</type>");
      stream.println(offset+once+once+"</symbol>");  
    }
    stream.println(offset+once+"</symbols>");
    for (i=0; i<children.size(); ++i) {
      next = (TSymbolTableNode)children.get(i);
      next.output(stream, offset+once);
    }

    stream.println(offset+"</symbolTableNode>");
  }
}
