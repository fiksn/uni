package sem2;
import java.io.*;

public class TSymbol {
  public TPosition start;
  public TPosition end;
  public String stringValue;
  public String stringSymbol;
  public int sym;
  public Object value;
  public TSymbol[] children;
  public String type;

  TSymbol(TPosition start, TPosition end, String stringValue, int sym, String stringSymbol) {
    this(start, end, stringValue, sym, stringSymbol, null);
  }

  TSymbol(TPosition start, TPosition end, String stringValue, int sym, String stringSymbol, Object value) {
    this.children = null;
    this.start = start;
    this.end = end;
    this.stringValue = stringValue;
    this.sym = sym;
    this.stringSymbol = stringSymbol;
    this.value = value;
  }

  /* Is this a terminal symbol? */
  public boolean isTerminal() {
    return (children == null);
  }

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
    TSymbol next = null;
    final String once = "  ";
    String name = (isTerminal()) ? ("terminal") : ("nonterminal");

    stream.println(offset+"<"+name+">");
  
    if (isTerminal()) {
      /* Terminals */
      stream.println(offset+once+"<sym>");
      stream.println(offset+once+once+sym);
      stream.println(offset+once+"</sym>");
      stream.println(offset+once+"<value>");
      stream.println(offset+once+once+escape(stringValue));
      stream.println(offset+once+"</value>");
    } else {
      /* Non-terminal */
      stream.println(offset+once+"<production>");
      stream.println(offset+once+once+(String)value);
      stream.println(offset+once+"</production>");
      stream.println(offset+once+"<value>");
      stream.println(offset+once+once+escape(stringValue));
      stream.println(offset+once+"</value>");

      /* Non-terminal has some productions */
      for (i=0; i<children.length; ++i) {
        next = children[i];
	if (next == null) {
  	  continue;
	} else {
	  next.output(stream, offset+once);
	}
      }
    }

    stream.println(offset+"</"+name+">");
  }
}