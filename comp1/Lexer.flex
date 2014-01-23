package sem2;
import java_cup.runtime.Symbol;

%%

%cup
%class Lexer
%line
%char
%column

/* I could aswell use that 
* %caseless
* %ignorecase
*/

A = [aA]
B = [bB]
C = [cC]
D = [dD]
E = [eE]
F = [fF]
G = [gG]
H = [hH]
I = [iI]
J = [jJ]
K = [kK]
L = [lL]
M = [mM]
N = [nN]
O = [oO]
P = [pP]
Q = [qQ]
R = [rR]
S = [sS]
T = [tT]
U = [uU]
V = [vV]
W = [wW]
X = [xX]
Y = [yY]
Z = [zZ]

whitespace = [ \t\r\n\f]
open_comment = "(*"
close_comment = "*)"
any_char = ({whitespace}|.)*
comment = \{{any_char}\}|{open_comment}{any_char}{close_comment}
sign = [+|-]
digit = [0-9]
letter = [A-Za-z_]
identifier = {letter}({letter}|{digit})*
uint = {digit}+
unumber = {uint}(\.{digit}+)?({E}{sign}?{uint})?

%{
  public String filename = "";

  private Symbol symbol(int type) {
    return new Symbol(type, new TSymbol(new TPosition(yyline+1, yycolumn+1, yychar+1), 
                               new TPosition(yyline+1, yycolumn+1+yylength(), yychar+1+yylength()),
                               yytext(), type, ""));
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, new TSymbol(new TPosition(yyline+1, yycolumn+1, yychar+1), 
                               new TPosition(yyline+1, yycolumn+1+yylength(), yychar+1+yylength()),
                               yytext(), type, "", value));
  }
%}

%%

/* Save the stuff without quotes as the symbol value */
{A}{N}{D} { return symbol(sym.AND); }
{A}{R}{R}{A}{Y} { return symbol(sym.ARRAY); }
{B}{E}{G}{I}{N} { return symbol(sym.BEGIN); }
{C}{A}{S}{E} { return symbol(sym.CASE); }
{C}{O}{N}{S}{T} { return symbol(sym.CONST); }
{D}{I}{V} { return symbol(sym.DIV); }
{D}{O} { return symbol(sym.DO); }
{D}{O}{W}{N}{T}{O} { return symbol(sym.DOWNTO); }
{E}{L}{S}{E} { return symbol(sym.ELSE); }
{E}{N}{D} { return symbol(sym.END); }
{F}{I}{L}{E} { return symbol(sym.FILE); }
{F}{O}{R} { return symbol(sym.FOR); }
{F}{U}{N}{C}{T}{I}{O}{N} { return symbol(sym.FUNCTION); }
{G}{O}{T}{O} { return symbol(sym.GOTO); }
{I}{F} { return symbol(sym.IF); }
{I}{N} { return symbol(sym.IN); }
{L}{A}{B}{E}{L} { return symbol(sym.LABEL); }
{M}{O}{D} { return symbol(sym.MOD); }
{N}{I}{L} return symbol(sym.NIL); }
{N}{O}{T} { return symbol(sym.NOT); }
{O}{F} { return symbol(sym.OF); }
{O}{R} { return symbol(sym.OR); }
{P}{A}{C}{K}{E}{D} { return symbol(sym.PACKED); }
{P}{R}{O}{C}{E}{D}{U}{R}{E} { return symbol(sym.PROCEDURE); }
{P}{R}{O}{G}{R}{A}{M}  { return symbol(sym.PROGRAM); }
{R}{E}{C}{O}{R}{D} { return symbol(sym.RECORD); }
{R}{E}{P}{E}{A}{T} { return symbol(sym.REPEAT); }
{S}{E}{T} { return symbol(sym.SET); }
{T}{H}{E}{N} { return symbol(sym.THEN); }
{T}{O} { return symbol(sym.TO); }
{T}{Y}{P}{E} { return symbol(sym.TYPE); }
{U}{N}{T}{I}{L} { return symbol(sym.UNTIL); }
{V}{A}{R} { return symbol(sym.VAR); }
{W}{H}{I}{L}{E} { return symbol(sym.WHILE); }
{W}{I}{T}{H} { return symbol(sym.WITH); }

/*
According to Sergio those are identifiers! Hmm...
{T}{R}{U}{E} { return symbol(sym.TRUE); }
{F}{A}{L}{S}{E} { return symbol(sym.FALSE); }
*/

\'[^\']*\' { return symbol(sym.STRING, new String(yytext().substring(1, yylength()-2))); }
{identifier} { return symbol(sym.IDENTIFIER, new String(yytext()));}
{uint} { return symbol(sym.UINT, new Integer(yytext()));}
{unumber} { return symbol(sym.UNUMBER, new Double(yytext()));}
":=" { return symbol(sym.ASSIGNMENT);}
":" { return symbol(sym.COLON);}
"," { return symbol(sym.COMMA);}
"." { return symbol(sym.DOT);}
".." { return symbol(sym.DOTDOT);}
"=" { return symbol(sym.EQUAL);}
">=" { return symbol(sym.GE);}
">" { return symbol(sym.GT);}
"\[" { return symbol(sym.LBRAC);}
"<=" { return symbol(sym.LE);}
"("  { return symbol(sym.LPAREN);}
"<" { return symbol(sym.LT);}
"-" { return symbol(sym.MINUS);}
"<>" { return symbol(sym.NOTEQUAL);}
"+" { return symbol(sym.PLUS);}
"\]" { return symbol(sym.RBRAC);}
")" { return symbol(sym.RPAREN);}
";" { return symbol(sym.SEMICOLON);}
"/" { return symbol(sym.SLASH);}
"*" { return symbol(sym.STAR);}
"->"|"^" { return symbol(sym.UPARROW);}
{whitespace} { /* ignore white space. */ }
{comment} { /* ignore comments. */ }
. { 
    String s;
    System.err.println("Lexer error - invalid character: "+yytext()+" - line: "+(yyline+1)+" column: "+(yycolumn+1)); 
    s = Report.getLine(filename, yyline+1);
    System.err.println(s);
    System.err.println(Report.markerString(s, yycolumn+1, '^'));
    System.exit(20);
    
  }
