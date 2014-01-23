package sem2;
import java.util.*;
import java.io.*;

class UpperLevelVisitor extends Visitor {
  public void visit(TSymbol sym) {
    TSemantic.currentNode = TSemantic.currentNode.parent;
  }
}

/*** PROCEDURE ***/
class ProcedureVisitor extends Visitor {
  public void visit(TSymbol proc) {
    TSymbol identifier = proc.children[1];
    TSemantic.currentNode.add(identifier.value.toString().toUpperCase(), TSymbolTableNode.SYMTABLE_PROCEDURE);

    TSemantic.currentNode = TSemantic.currentNode.newLevel();
    /* Store -> to symbol table */
    proc.value = TSemantic.currentNode;

    if (proc.children.length > 2) {
      /* There are some parameters */
      Visitor.topDownVisit(proc, new String[] { "value_parameter_specification", "variable_parameter_specification"},
                           new Visitor[] { new ProcValueParameter(), new ProcVarParameter() });
    }
  }
}

class ProcValueParameter extends Visitor {
  public static TSymbol type;
  public void visit(TSymbol current) {
    TSymbol left = current.children[0];
    type = current.children[2];
    Visitor.topDownVisit(left, sym.IDENTIFIER, new OneProcValueParameter());        
  }
}

class OneProcValueParameter extends Visitor {
  public void visit(TSymbol identifier) {
    TSemantic.currentNode.add(identifier.value.toString(), TSymbolTableNode.SYMTABLE_VALUEPARAMETER, ProcValueParameter.type.stringValue);
  }
}

class ProcVarParameter extends Visitor {
  public static TSymbol type;
  public void visit(TSymbol current) {
    TSymbol left = current.children[0];
    type = current.children[3];
    Visitor.topDownVisit(left, sym.IDENTIFIER, new OneProcVarParameter());        
  }
}

class OneProcVarParameter extends Visitor {
  public void visit(TSymbol identifier) {
    TSemantic.currentNode.add(identifier.value.toString(), TSymbolTableNode.SYMTABLE_VARPARAMETER, ProcVarParameter.type.stringValue);
  }
}


/*** FUNCTION ***/
class FunctionVisitor extends Visitor {
  public void visit(TSymbol fun) {
    TSymbol identifier = fun.children[1];
    TSymbol type;

    if (fun.children.length == 5)
    {
      type = fun.children[4];
    } else {
      type = fun.children[3];
    }
    
    TSemantic.currentNode.add(identifier.value.toString(), TSymbolTableNode.SYMTABLE_PROCEDURE, type.stringValue);
    TSemantic.currentNode = TSemantic.currentNode.newLevel();
    /* Store -> to symbol table */
    fun.value = TSemantic.currentNode;

    if (fun.children.length > 3) {
      /* There are some parameters */
      Visitor.topDownVisit(fun, new String[] { "value_parameter_specification", "variable_parameter_specification" },
                         new Visitor[] { new FuncValueParameter(), new FuncVarParameter() });
    }
  }
}

class FuncValueParameter extends Visitor {
  public static TSymbol type;
  public void visit(TSymbol current) {
    TSymbol left = current.children[0];
    type = current.children[2];
    Visitor.topDownVisit(left, sym.IDENTIFIER, new OneFuncValueParameter());        
  }
}

class OneFuncValueParameter extends Visitor {
  public void visit(TSymbol identifier) {
    TSemantic.currentNode.add(identifier.value.toString(), TSymbolTableNode.SYMTABLE_VALUEPARAMETER, FuncValueParameter.type.stringValue);
  }
}

class FuncVarParameter extends Visitor {
  public static TSymbol type;
  public void visit(TSymbol current) {
    TSymbol left = current.children[0];
    type = current.children[2];
    Visitor.topDownVisit(left, sym.IDENTIFIER, new OneFuncVarParameter());        
  }
}

class OneFuncVarParameter extends Visitor {
  public void visit(TSymbol identifier) {
    TSemantic.currentNode.add(identifier.value.toString(), TSymbolTableNode.SYMTABLE_VARPARAMETER, FuncVarParameter.type.stringValue);
  }
}



/*** LABELS ***/
class LabelVisitor extends Visitor {
  public void visit(TSymbol label) {
    Visitor.topDownVisit(label, sym.UINT, new OneLabelVisitor());        
  }
}

class OneLabelVisitor extends Visitor {
  public void visit(TSymbol oneLabel) {
    TSemantic.currentNode.add(oneLabel.value.toString(), TSymbolTableNode.SYMTABLE_LABEL);
  }
}


/*** CONST ***/
class ConstVisitor extends Visitor {
  public void visit(TSymbol constant) {
    Visitor.topDownVisit(constant, "constant_definition", new ConstDefVisitor());  
  }
}

class ConstDefVisitor extends Visitor {
  public void visit(TSymbol constantDefinition) {
    TSymbol left = Visitor.findFirst(constantDefinition, sym.IDENTIFIER);
    TSymbol right = Visitor.findFirst(constantDefinition, "constant");
    TSemantic.currentNode.add(left.stringValue, TSymbolTableNode.SYMTABLE_CONST);
 }
}


/*** TYPE ***/
class TypeVisitor extends Visitor {
  public void visit(TSymbol type) {
    Visitor.topDownVisit(type, "type_definition", new TypeDefVisitor());  
  }
}

class TypeDefVisitor extends Visitor {
  public void visit(TSymbol typeDefinition) {
    TSymbol left = Visitor.findFirst(typeDefinition, sym.IDENTIFIER);
    TSymbol right = Visitor.findFirst(typeDefinition, "type_denoter");
    String type;

    if (right.children[0].sym == sym.IDENTIFIER) {
      /* An alias for something else */
      type = TSemantic.currentNode.resolveType(right.children[0].value.toString());
      if (type == null) {
        System.err.println("Unknown type: "+right.children[0].value);
        String s = Report.getLine(TSemantic.fileName, right.children[0].start.lineNumber);
        System.err.println(s);
        System.err.println(Report.markerString(s, right.children[0].start.lineOffset, '^'));
        return;
      }
      TSemantic.currentNode.add(left.value.toString(), TSymbolTableNode.SYMTABLE_TYPEALIAS, type);
    } else {
      TSemantic.currentNode.add(left.value.toString(), TSymbolTableNode.SYMTABLE_TYPE, right.stringValue.toUpperCase());
    }
  }
}

/*** VAR ***/
class VariableVisitor extends Visitor {
  public void visit(TSymbol variable) {
    Visitor.topDownVisit(variable, "variable_declaration", new VariableDefVisitor());  
  }
}

class VariableDefVisitor extends Visitor {
  public String type; 
  public String nameExtension;
  public TSymbol predefinedIdList;
  public TSymbol left;

  public VariableDefVisitor() {
    type = null;
    nameExtension = "";
    predefinedIdList = null;
  }

  public void visit(TSymbol variableDefinition) {

    if (predefinedIdList == null) {
      left = Visitor.findFirst(variableDefinition, "identifier_list");
    } else {
      left = predefinedIdList;
    }
    TSymbol right = Visitor.findFirst(variableDefinition, "type_denoter");
     

    if (right.children[0].sym == sym.IDENTIFIER) {
      /* Type must be already known */
      type = TSemantic.currentNode.resolveType(right.children[0].value.toString());
      if (type == null) {
        System.err.println("Unknown type: "+right.children[0].value);
        String s = Report.getLine(TSemantic.fileName, right.children[0].start.lineNumber);
        System.err.println(s);
        System.err.println(Report.markerString(s, right.children[0].start.lineOffset, '^'));
        return;
      }
    } 

    Visitor.topDownVisit(left, sym.IDENTIFIER, new OneVariableDefVisitor(this));

    if ((right = findFirst(right.children[0], "structured_type")) != null) {
      Visitor.topDownVisit(right, new String[] { "array_type"}, new Visitor[] { 
                               new ArrayVarVisitor(this)});
    }
 }
}

class ArrayVarVisitor extends Visitor {
  public VariableDefVisitor parent;
  
  public ArrayVarVisitor(VariableDefVisitor parent) { 
    this.parent = parent;
  }

  public void visit(TSymbol array) {
    TSymbol lowerType = array.children[5];
    VariableDefVisitor recursiveVisitor = new VariableDefVisitor();
    recursiveVisitor.nameExtension = parent.nameExtension + "[]";
    recursiveVisitor.predefinedIdList = parent.left;
    System.out.println("Recursive blah");
    recursiveVisitor.visit(array);
  }
}

class RecordVarVisitor extends Visitor {
  public void visit(TSymbol record) {

  }
}


class OneVariableDefVisitor extends Visitor {
  public VariableDefVisitor parent;
  
  public OneVariableDefVisitor(VariableDefVisitor parent) { 
    this.parent = parent;
  }

  public void visit(TSymbol oneVariable) {
    TSemantic.currentNode.add((oneVariable.value.toString()+parent.nameExtension).toUpperCase(), TSymbolTableNode.SYMTABLE_VARIABLE,
                              parent.type); 
  }
}


/***************************************************************************/

class LabelChecker extends Visitor {
  public void visit(TSymbol label) {
    
  }
}

public class TSemantic {


  public static TSymbolTableNode root;
  public static TSymbolTableNode currentNode;

  public static String fileName;
   
  TSemantic() {
    root = new TSymbolTableNode();
        
    /* Built-in types */
    root.add("integer", TSymbolTableNode.SYMTABLE_TYPE);    
    root.add("real", TSymbolTableNode.SYMTABLE_TYPE);
    root.add("boolean", TSymbolTableNode.SYMTABLE_TYPE);
    root.add("char", TSymbolTableNode.SYMTABLE_TYPE);
   
    currentNode = root.newLevel();

  }

  public void check(TSymbol tree) {
    /* Skip program heading */
    TSymbol top = Visitor.findFirst(tree, "block");
    top.value = currentNode;

    /* Build symbol table */
    Visitor.topDownVisit(top, new String[] {"statement_part", 
                                    "label_declaration_part", "constant_definition_part", "type_definition_part",
                                    "variable_declaration_part",
                                    "procedure_heading", "function_heading" }, 
              new Visitor[] {new UpperLevelVisitor(), 
                             new LabelVisitor(), new ConstVisitor(), new TypeVisitor(), 
                             new VariableVisitor(),
                             new ProcedureVisitor(), new FunctionVisitor() });

    /* Check all types */
/*
    Visitor.bottomUpVisit(top, new String[] { "unsigned_constant", "factor", "term", "simple_expression", "expression", "boolean_expression" },
                                  new Visitor[] { new LabelChecker(); } )

*/
    

  }

  public void output(String xmlFilename) {
    try {
      PrintStream outStream = new PrintStream(new FileOutputStream(xmlFilename));
      root.output(outStream, "");
      outStream.flush();
    }
    catch (Exception ex) {
        System.err.println("Couldn't write to output file because of: "+ex.toString());
    }
  }
}