program C;   (* Compiler *)
(*

{
TO DO LIST
- for loop  - OK
- range checking
- expression types
- array constant values {1, 2, 3, 4}
}


NumLit StrLit Name + - * / % ( ) = < > <> >= <= NOT OR AND [ ] ; := !
begin end if then loop exit while for write read program int double bool char

VarSpec = [ TypeName [ '[' Expression ']' ] ] VarName [ '[' Expression ']' ]

Condition = Expression [ CompOp Expression ]
Expression = Term { AddOp Term }
Term = Factor { MulOp Factor }
Factor = NumLit | StrLit | VarSpec | [ NOT ] '(' Condition ')'
CompOp = '=' | < | > | <> | >= | <=
AddOp = + | - | OR
MulOp = * | / | % | AND

Statement = |
  begin { Statement ';' } end |
  VarSpec := Expression |
  if Condition then Statement [ else Statement ] |
  loop Statement |
  exit |
  while Condition do Statement |
  for (Statement;Condition;LoopStatement) do  |
  break |
  write Condition |
  read VarSpec

Program = program ProgramName ';' Statement '!'

Example:
program Test;
begin
  char[10] str := 'test';
  int[5] ai := {1, 2, 3, 4, 5};
  a := 1;
  while (a <= 10) do begin
    write a;
    a := a + 1;
  end;
end!

PUSH 0
PUSH 1
MOVREF
L0:
PUSH 0
DEREF
PUSH 10
LEQ
NOT
JC L1
PUSH 0
DEREF
WRITE
PUSH 0
PUSH 0
DEREF
PUSH 1
ADD
MOVREF
JMP L0
L1:
END
*)

uses
  fdelay, Crt, Dos;

const
  chEOF = #26;
  Null = -1;

  MxNum = 1000;
  MxNNestedLoops = 10;
  MxNLoopExits = 10;
  MxCodeSize = 1000;
  MxStackSize = 1000;
  MxMemSize = 1000;
  MxStrLen = 255;
  MxNmLen = 32;
  MxNmTSize = 100;

  SrcFNm = 'P.PL2';
  EVCFNm = 'OUT.EVC';

type
  TSym = (symNull, symErr, symEOF, symKeyword,
   symNumLit, symStrLit, symName,
   symPlus, symMinus, symMul, symDiv, symMod,
   symEq, symLss, symGtr, symNEq, symGEq, symLEq,
   symNot, symOr, symAnd,
   symLParen, symRParen, symLBrack, symRBrack,
   symSemi, symAssign, symExcl);

  TKeyword = (kwFIRST,   (* kwFIRST and kwLAST are sentinels *)
   kwBegin, kwEnd, kwIf, kwThen, kwElse, kwLoop, kwExit,
   kwWhile, kwDo, kwWrite, kwRead, kwProgram,
   kwInt, kwDouble, kwBool, kwChar,kwFor,
   kwLAST);

  TSymSet = set of TSym;

  TAddr = integer;

  TType = (tyNull, tyInt, tyDouble, tyBool, tyChar);

  TNm = string[MxNmLen];

  TNmTEntry = record
    nteNm : TNm;
    nteAddr : TAddr;
    nteType : TType;
    nteRange : integer;
  end;
  TNmT = array [0..MxNmTSize-1] of TNmTEntry;

  TLoopExitStackEntry = array [0..MxNLoopExits-1] of TAddr;
  TLoopExitStack = array [0..MxNNestedLoops-1] of TLoopExitStackEntry;

  TOpCode = (iNull,
   iAdd, iSub, iMul, iDiv, iMod,
   iAnd, iOr, iNot,
   iEq, iLss, iGtr, iLEq, iGEq, iNEq,
   iPush, iPop, iMovRef, iDeRef,
   iJmp, iJC,
   iWrite, iRead,
   iEnd);

  TCodeItemKind = (cikInteger, cikAddr);
  TCodeItem = record
    ciOpCode : TOpCode;
    case TCodeItemKind of
      cikInteger : (ciIParam : integer);
      cikAddr : (ciAParam : TAddr);
  end;
  TCode = array [0..MxCodeSize-1] of TCodeItem;

  TStack = array [0..MxStackSize-1] of byte;

  TMem = array [0..MxMemSize-1] of byte;

const
  SymNms : array [TSym] of string =
   ('symNull', 'symErr', 'symEOF', 'symKeyword',
    'symNumLit', 'symStrLit', 'symName',
    'symPlus', 'symMinus', 'symMul', 'symDiv', 'symMod',
    'symEq', 'symLss', 'symGtr', 'symNEq', 'symGEq', 'symLEq',
    'symNot', 'symOr', 'symAnd',
    'symLParen', 'symRParen', 'symLBrack', 'symRBrack',
    'symSemi', 'symAssign', 'symExcl');

  SymInsts : array [TSym] of TOpCode =
   (iNull, iNull, iNull, iNull,
    iNull, iNull, iNull,
    iAdd, iSub, iMul, iDiv, iMod,
    iEq, iLss, iGtr, iNEq, iGEq, iLEq,
    iNot, iOr, iAnd,
    iNull, iNull, iNull, iNull,
    iNull, iNull, iNull);

  OpCodeNms : array [TOpCode] of string = ('iNull',
   'iAdd', 'iSub', 'iMul', 'iDiv', 'iMod',
   'iAnd', 'iOr', 'iNot',
   'iEq', 'iLss', 'iGtr', 'iLEq', 'iGEq', 'iNEq',
   'iPush', 'iPop', 'iMovRef', 'iDeRef',
   'iJmp', 'iJC',
   'iWrite', 'iRead',
    'iEnd');

  KeywordNms : array [TKeyword] of string = ('FIRST',
   'begin', 'end', 'if', 'then', 'else', 'loop', 'exit', 'while', 'do',
   'write', 'read', 'program', 'int', 'double', 'bool', 'char', 'for','LAST');

  KeywordTypes : array [TKeyword] of TType = (
   tyNull, tyNull, tyNull, tyNull, tyNull, tyNull, tyNull, tyNull,
   tyNull, tyNull, tyNull, tyNull, tyNull,
   tyInt, tyDouble, tyBool, tyChar, tyNull, tyNull);

  TypeSizes : array [TType] of integer = (
   0, sizeof (integer), sizeof (double), sizeof (boolean), sizeof (char));

  AddOps : TSymSet = [symPlus, symMinus, symOr];
  MulOps : TSymSet = [symMul, symDiv, symMod, symAnd];
  CompOps : TSymSet = [symEq, symNEq, symGtr, symLss, symGEq, symLEq];

var
  E : string;
  EP, PrevEP : integer;
  Sym : TSym;
  KW : TKeyword;
  Num : integer;
  Str, Name : string;
  Ch : char;
  SrcF : Text;
  EVCF : Text;
  LineN : integer;

  Code : TCode;
  IP : integer;

  LEStack : TLoopExitStack;
  LESP : integer;

  Stack : TStack;
  SP : integer;

  Mem : TMem;

  NmT : TNmT;
  NmTP, FirstFreeAddr : integer;

procedure Error (ErrMsg : string);
begin
  writeln (LineN, ': ----- Error ----- ', ErrMsg);
  Halt;
end;

function UpperCase(s: string):string;
var temp: string;
    i: integer;
begin
  temp:='';
  for i:=1 to length(s) do temp:=temp+UpCase(s[i]);
  UpperCase:=temp;
end;

procedure Assert (Status : boolean; ErrMsg : string);
begin
  if not Status then Error (ErrMsg);
end;

function SearchNmT (Nm : TNm; Size : integer; Ty : TType) : integer;
var
  P : integer;
begin
  for P := 0 to NmTP - 1 do if Nm = NmT[P].nteNm then begin
    Assert (Ty = tyNull, 'type already specified');
    SearchNmT := NmT[P].nteAddr; exit;
  end;
  Assert (NmTP < MxNmTSize, 'NmT Range Overflow');
  Assert (Ty <> tyNull, 'type not specified');
  NmT[NmTP].nteNm := Nm;
  NmT[NmTP].nteAddr := FirstFreeAddr;
  NmT[NmTP].nteType := Ty;
  NmT[NmTP].nteRange := (Size div TypeSizes[Ty]) - 1;
  Inc (NmTP);
  SearchNmT := FirstFreeAddr;
  Inc (FirstFreeAddr, Size);
end;

procedure Encode (OpCode : TOpCode);
begin
  Assert (IP < MxCodeSize, 'Code Table Overflow');
  with Code[IP] do begin ciOpCode := OpCode; end;
  Inc (IP);
end;

procedure IEncode (OpCode : TOpCode; Param : integer);
begin
  Assert (IP < MxCodeSize, 'Code Table Overflow');
  with Code[IP] do begin ciOpCode := OpCode; ciIParam := Param; end;
  Inc (IP);
end;

procedure AEncode (OpCode : TOpCode; Param : TAddr);
begin
  Assert (IP < MxCodeSize, 'Code Table Overflow');
  with Code[IP] do begin ciOpCode := OpCode; ciAParam := Param; end;
  Inc (IP);
end;

procedure FillInCodeParam (CodeIP : integer; Param : TAddr);
begin
  Code[CodeIP].ciAParam := Param;
end;

procedure GetCh (var Ch : char);
begin
  if Eof (SrcF) then begin
    Ch := chEOF;
  end else
    Read (SrcF, Ch);
end;   (* GetCh *)

procedure GetSym (var Sym : TSym);
var
  NL, SL : integer;
begin
  PrevEP := EP;
  if Ch = #13 then Inc (LineN);
  while Ch in [' ', #13, #10] do GetCh (Ch);
  if Ch in ['+', '-', '*', '/', '%', '(', ')', '=', '!', '[', ']', ';']
   then begin
    case Ch of
      '+' : Sym := symPlus;
      '-' : Sym := symMinus;
      '*' : Sym := symMul;
      '/' : Sym := symDiv;
      '%' : Sym := symMod;
      '(' : Sym := symLParen;
      ')' : Sym := symRParen;
      '=' : Sym := symEq;
      '!' : Sym := symExcl;
      '[' : Sym := symLBrack;
      ']' : Sym := symRBrack;
      ';' : Sym := symSemi;
    end;
    GetCh (Ch);
  end else if Ch = chEOF then begin
    Sym := symEOF;
  end else if UpCase (Ch) in ['A'..'Z', '_'] then begin
    Sym := symName;
    NL := 0;
    while UpCase (Ch) in ['A'..'Z', '0'..'9', '_'] do begin
      Assert (NL < MxNmLen, 'name length overflow');
      inc (NL); Name[NL] := Ch; GetCh (Ch);
    end;
    Name[0] := Chr (NL);

    if UpperCase(Name) = 'NOT' then begin
      Sym := symNot;
    end else if UpperCase(Name) = 'OR' then begin
      Sym := symOr;
    end else if UpperCase(Name) = 'AND' then begin
      Sym := symAnd;
    end else begin
      for KW := Succ (kwFIRST) to Pred (kwLAST) do
        if Name = KeywordNms[KW] then begin Sym := symKeyword; break; end;
    end;

  end else if Ch in ['0'..'9'] then begin
    Sym := symNumLit;
    Num := 0;
    repeat
      Num := Num * 10 + Ord (Ch) - Ord ('0');
      if (Num > MxNum) then Sym := symErr;
      GetCh (Ch);
    until (Num > MxNum) or not (Ch in ['0'..'9']);
  end else if Ch = '"' then begin
    Sym := symStrLit;
    SL := 0;
    GetCh (Ch);
    while Ch <> '"' do begin
      Assert (SL < MxStrLen, 'string literal length overflow');
      inc (SL); Str[SL] := Ch; GetCh (Ch);
    end;
    Str[0] := Chr (SL);
    GetCh (Ch);
  end else if Ch = ':' then begin
    Sym := symNull;
    GetCh (Ch);
    if Ch = '=' then begin
      Sym := symAssign;
      GetCh (Ch);
    end;
  end else if Ch = '>' then begin
    Sym := symGtr;
    GetCh (Ch);
    if Ch = '=' then begin
      Sym := symGEq;
      GetCh (Ch);
    end;
  end else if Ch = '<' then begin
    Sym := symLss;
    GetCh (Ch);
    if Ch = '=' then begin
      Sym := symLEq;
      GetCh (Ch);
    end else if Ch = '>' then begin
      Sym := symNEq;
      GetCh (Ch);
    end;
  end else begin
    Sym := symErr;
  end;
end;   (* GetSym *)

function IsKeyword (ExpKW : TKeyword) : boolean;
begin
  IsKeyword := (Sym = symKeyword) and (KW = ExpKW);
end;   (* IsKeyword *)

procedure CompileVarSpec (ExpSym : TSymSet); forward;

procedure CompileCondition (ExpSym : TSymSet); forward;

procedure CompileFactor (ExpSym : TSymSet);
var
  FixSymNot : boolean;
begin
  Assert (Sym in [symNumLit, symStrLit, symName, symNot, symLParen],
   'factor expected');
  if Sym = symNumLit then begin
    IEncode (iPush, Num);
    GetSym (Sym);
  end else if Sym = symStrLit then begin
    Error ('string not supported');
    GetSym (Sym);
  end else if Sym = symName then begin
    CompileVarSpec (ExpSym);
    Encode (iDeref);
  end else begin
    FixSymNot := false;
    if Sym = symNot then begin
      GetSym (Sym);
      FixSymNot := true;
    end;
    if Sym = symLParen then begin
      GetSym (Sym);
      CompileCondition ([symRParen]);
      GetSym (Sym);
    end;
    if FixSymNot then Encode (iNot);
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;

procedure CompileTerm (ExpSym : TSymSet);
var
  LVal : integer;
  Op : TSym;
begin
  CompileFactor (MulOps + ExpSym);
  Op := Sym;
  while Op in MulOps do begin
    GetSym (Sym);
    CompileFactor (MulOps + ExpSym);
    Encode (SymInsts[Op]);
    Op := Sym;
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;

procedure CompileExpression (ExpSym : TSymSet);
var
  LVal : integer;
  Op : TSym;
begin
  CompileTerm (AddOps + ExpSym);
  Op := Sym;
  while Op in AddOps do begin
    GetSym (Sym);
    CompileTerm (AddOps + ExpSym);
    Encode (SymInsts[Op]);
    Op := Sym;
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileExpression *)

procedure CompileCondition (ExpSym : TSymSet);
var
  Op : TSym;
begin
  CompileExpression (CompOps + ExpSym);
  Op := Sym;
  if Op in CompOps then begin
    GetSym (Sym);
    CompileExpression (ExpSym);
    Encode (SymInsts[Op]);
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileCondition *)

procedure CompileVarSpec (ExpSym : TSymSet);
var
  Addr : integer;
  Val : integer;
  Ty : TType;
  Size : integer;
begin
  Ty := tyNull;
  if Sym = symKeyword then begin
    Ty := KeywordTypes[KW];
    Assert (Ty <> tyNull, 'type specifier expected');
    Size := TypeSizes[Ty];
    GetSym (Sym);
    if Sym = symLBrack then begin
      GetSym (Sym);
      Assert (Sym = symNumLit, 'array length specifier expected');
      Assert (Num > 0, 'zero sized array');
      Assert (longint (Size) * longint (Num) <= longint (maxint),
       'array size overflow');
      Size := Size * Num;
      GetSym (Sym); Assert (Sym = symRBrack, '] expected'); GetSym (Sym);
    end;
  end;
  Assert (Sym = symName, 'name expected');
  Addr := SearchNmT (Name, Size, Ty);
  AEncode (iPush, Addr);
  GetSym (Sym);
  if (Sym = symLBrack) then begin
    GetSym (Sym); CompileExpression ([symRBrack]); GetSym (Sym);
    Encode (iAdd);
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileVarSpec *)

procedure CompileStatement (ExpSym : TSymSet); forward;

procedure CompileAssignment (ExpSym : TSymSet);
begin
  CompileVarSpec ([symAssign]); GetSym (Sym);
  CompileExpression (ExpSym);
  Encode (iMovRef);
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileAssignment *)

procedure CompileCompoundStatement (ExpSym : TSymSet);
begin
  Assert (IsKeyword (kwBegin), 'begin expected'); GetSym (Sym);
  while not IsKeyword (kwEnd) do begin
    CompileStatement ([symSemi]); GetSym (Sym);
  end;
  Assert (IsKeyword (kwEnd), 'end expected'); GetSym (Sym);
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileCompoundStatement *)

procedure CompileIfStatement (ExpSym : TSymSet);
var
  ElseFIAddr, EndFIAddr : integer;
begin
  Assert (IsKeyword (kwIf), 'if expected'); GetSym (Sym);
  CompileCondition ([symKeyword]);
  Encode (iNot);
  ElseFIAddr := IP;
  AEncode (iJC, 0);
  Assert (IsKeyword (kwThen), 'then expected'); GetSym (Sym);
  CompileStatement ([symKeyword] + ExpSym);
  FillInCodeParam (ElseFIAddr, IP);
  if IsKeyword (kwElse) then begin
    EndFIAddr := IP;
    AEncode (iJmp, 0);
    FillInCodeParam (ElseFIAddr, IP);
    GetSym (Sym); CompileStatement (ExpSym);
    FillInCodeParam (EndFIAddr, IP);
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileIfStatement *)

procedure CompileForStatement (ExpSym : TSymSet);
var
  BeginAddr, EndFIAddr, I : integer;
begin
  Assert (LESP < MxNNestedLoops, 'LEStack overflow');
  for I := 0 to MxNLoopExits-1 do LEStack[LESP][I] := Null; Inc (LESP);

  Assert (IsKeyword (kwFor), 'for expected'); GetSym (Sym);
  Assert (Sym=symLParen, '( expected'); CompileAssignment(ExpSym);
  CompileCondition (ExpSym);  BeginAddr:=IP;
  Encode (iNot);
  EndFIAddr:=IP;
  AEncode (iJC, 0);
  CompileAssignment (ExpSym);
  Assert (Sym=symRParen, '( expected');
  CompileStatement (ExpSym); AEncode (iJmp, BeginAddr);
  FillInCodeParam (EndFIAddr, IP);

  Dec (LESP);
  for I := 0 to MxNLoopExits-1 do
  if LEStack[LESP][I] <> Null then FillInCodeParam (LEStack[LESP][I], IP);

end;   (* CompileForStatement *)



procedure CompileLoopStatement (ExpSym : TSymSet);
var
  BeginAddr, I : integer;
begin
  Assert (LESP < MxNNestedLoops, 'LEStack overflow');
  for I := 0 to MxNLoopExits-1 do LEStack[LESP][I] := Null; Inc (LESP);
  Assert (IsKeyword (kwLoop), 'loop expected'); GetSym (Sym);
  BeginAddr := IP;
  CompileStatement (ExpSym);
  AEncode (iJmp, BeginAddr);
  Assert (Sym in ExpSym, 'symbol not expected');
  Dec (LESP);
  for I := 0 to MxNLoopExits-1 do
    if LEStack[LESP][I] <> Null then FillInCodeParam (LEStack[LESP][I], IP);
end;   (* CompileLoopStatement *)

procedure CompileExitStatement (ExpSym : TSymSet);
var
  Val, I : integer;
begin
  Assert (LESP > 0, 'Exit needs a loop');
  Assert (IsKeyword (kwExit), 'exit expected'); GetSym (Sym);
  I := 0;
  while (I < MxNLoopExits) and (LEStack[LESP-1][I] <> Null) do Inc (I);
  Assert (I < MxNLoopExits, 'LEStackEntry overflow');
  LEStack[LESP-1][I] := IP;
  AEncode (iJmp, 0);
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileExitStatement *)

procedure CompileWhileStatement (ExpSym : TSymSet);
var
  BeginAddr, EndFIAddr : integer;
begin
  Assert (IsKeyword (kwWhile), 'while expected'); GetSym (Sym);
  BeginAddr := IP;
  CompileCondition ([symKeyword]);
  Encode (iNot);
  EndFIAddr := IP;
  AEncode (iJC, 0);
  Assert (IsKeyword (kwDo), 'do expected'); GetSym (Sym);
  CompileStatement (ExpSym);
  AEncode (iJmp, BeginAddr);
  FillInCodeParam (EndFIAddr, IP);
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileWhileStatement *)

procedure CompileWriteStatement (ExpSym : TSymSet);
begin
  Assert (IsKeyword (kwWrite), 'write expected'); GetSym (Sym);
  CompileCondition (ExpSym);
  Encode (iWrite);
end;   (* CompileWriteStatement *)

procedure CompileReadStatement (ExpSym : TSymSet);
begin
  Assert (IsKeyword (kwRead), 'read expected'); GetSym (Sym);
  CompileVarSpec (ExpSym);
  Encode (iRead);
end;   (* CompileReadStatement *)

procedure CompileStatement (ExpSym : TSymSet);
begin
  if Sym = symName then
    CompileAssignment (ExpSym)
  else if Sym = symKeyword then case KW of
    kwInt, kwDouble, kwBool, kwChar : CompileAssignment (ExpSym);
    kwBegin : CompileCompoundStatement (ExpSym);
    kwIf : CompileIfStatement (ExpSym);
    kwLoop : CompileLoopStatement (ExpSym);
    kwExit : CompileExitStatement (ExpSym);
    kwWhile : CompileWhileStatement (ExpSym);
    kwWrite : CompileWriteStatement (ExpSym);
    kwRead : CompileReadStatement (ExpSym);
    kwFor : CompileForStatement (ExpSym);
  end;
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileStatement *)

procedure CompileProgram (ExpSym : TSymSet);
begin
  Assert (IsKeyword (kwProgram), 'program expected');
  GetSym (Sym);
  Assert (Sym = symName, 'program name expected');
  GetSym (Sym);
  Assert (Sym = symSemi, '; expected');
  GetSym (Sym);
  CompileStatement ([symExcl]);
  Encode (iEnd);
  GetSym (Sym);
  Assert (Sym in ExpSym, 'symbol not expected');
end;   (* CompileProgram *)

procedure CheckStack (NPops, NPushes : integer);
begin
  Assert (NPops <= SP, 'stack underflow');
  Assert (SP + NPushes < MxStackSize, 'stack underflow');
end;   (* CheckStack *)

procedure CheckInteger (Int : longint);
begin
  Assert ((Int <= MaxInt) and (Int >= -MaxInt - 1), 'integer overflow');
end;   (* CheckInteger *)

procedure CheckMem (Addr : integer);
begin
  Assert ((Addr >= 0) and (Addr < MxMemSize), 'memory overflow');
end;   (* CheckMem *)

procedure InterpretProgram;
var
  NewIP : integer;
  Str : string;
  ValCode : integer;
begin
  IP := 0; SP := 0;
  repeat
    NewIP := IP + 1;
    case Code[IP].ciOpCode of
      iAdd : begin
        CheckStack (2, 0);
        Dec (SP);
        CheckInteger (longint (Stack[SP-1]) + longint (Stack[SP]));
        Stack[SP-1] := Stack[SP-1] + Stack[SP];
      end;

      iSub : begin
        CheckStack (2, 0);
        Dec (SP);
        CheckInteger (longint (Stack[SP-1]) - longint (Stack[SP]));
        Stack[SP-1] := Stack[SP-1] - Stack[SP];
      end;

      iMul : begin
        CheckStack (2, 0);
        Dec (SP);
        CheckInteger (longint (Stack[SP-1]) * longint (Stack[SP]));
        Stack[SP-1] := Stack[SP-1] * Stack[SP];
      end;

      iDiv : begin
        CheckStack (2, 0);
        Dec (SP);
        Assert (Stack[SP] <> 0, 'division by zero');
        CheckInteger (longint (Stack[SP-1]) div longint (Stack[SP]));
        Stack[SP-1] := Stack[SP-1] div Stack[SP];
      end;

      iMod : begin
        CheckStack (2, 0);
        Dec (SP);
        Assert (Stack[SP] <> 0, 'division by zero');
        CheckInteger (longint (Stack[SP-1]) mod longint (Stack[SP]));
        Stack[SP-1] := Stack[SP-1] mod Stack[SP];
      end;

      iAnd : begin
        CheckStack (2, 0);
        Dec (SP);
        if (Stack[SP-1] <> 0) and (Stack[SP] <> 0) then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iOr : begin
        CheckStack (2, 0);
        Dec (SP);
        if (Stack[SP-1] <> 0) or (Stack[SP] <> 0) then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iNot : begin
        CheckStack (1, 0);
        if Stack[SP-1] = 0 then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iEq : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] = Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iLss : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] < Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iGtr : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] > Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iLEq : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] <= Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iGEq : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] >= Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iNEq : begin
        CheckStack (2, 0);
        Dec (SP);
        if Stack[SP-1] <> Stack[SP] then
          Stack[SP-1] := 1
        else
          Stack[SP-1] := 0;
      end;

      iPush : begin
        CheckStack (0, 1);
        Stack[SP] := Code[IP].ciIParam;
        Inc (SP);
      end;

      iPop : begin
        CheckStack (1, 0);
        Dec (SP);
      end;

      iMovRef : begin
        CheckStack (2, 0);
        Dec (SP, 2);
        CheckMem (Stack[SP]);
        Mem[Stack[SP]] := Stack[SP+1];
      end;

      iDeRef : begin
        CheckStack (1, 0);
        CheckMem (Stack[SP-1]);
        Stack[SP-1] := Mem[Stack[SP-1]];
      end;

      iJmp : begin
        NewIP := Code[IP].ciAParam;
      end;

      iJC : begin
        CheckStack (1, 0);
        Dec (SP);
        if Stack[SP] <> 0 then NewIP := Code[IP].ciAParam;
      end;

      iWrite : begin
        CheckStack (1, 0);
        Dec (SP);
        writeln (Stack[SP]);
      end;

      iRead : begin
        CheckStack (1, 0);
        Dec (SP);
        CheckMem (Stack[SP]);
        readln (Str);
        Val (Str, Mem[Stack[SP]], ValCode);
      end;

      iEnd : break;

      iNull : Error ('OpCode not expected');
    else
      Error ('OpCode not recognized');
    end;
    IP := NewIP;
  until false;
  writeln; writeln ('Program execution terminated.');
end;

procedure OutputProgram;
begin
  IP := 0;
  repeat
    writeln (IP, ': ', OpCodeNms[Code[IP].ciOpCode], ' ', Code[IP].ciIParam);
    Inc (IP);
  until Code[IP-1].ciOpCode = iEnd;
end;

begin
  ClrScr;
  LineN := 1;
  Sym := symNull;
  NmTP := 0; FirstFreeAddr := 0;
  Assign (SrcF, SrcFNm); Reset (SrcF);
  GetCh (Ch); GetSym (Sym);

  IP := 0; LESP := 0;
  CompileProgram ([symEOF]);

  Close (SrcF);
  Writeln ('Done.');

  OutputProgram;

  InterpretProgram;

  ReadKey;
end.
