import java.io.PrintWriter;

public class CompilationEngine {

  private final JackTokenizer tokenizer;
  private final PrintWriter writer;
  private int indent = 0;

  public CompilationEngine(JackTokenizer tokenizer, PrintWriter writer) {
    this.tokenizer = tokenizer;
    this.writer = writer;
    tokenizer.advance();
  }

  private void eat() {
    String type = tokenizer.tokenType();

    String tag = switch (type) {
      case "KEYWORD" -> "keyword";
      case "SYMBOL" -> "symbol";
      case "INT_CONST" -> "integerConstant";
      case "STRING_CONST" -> "stringConstant";
      case "IDENTIFIER" -> "identifier";
      default -> null;
    };

    String var = switch (type) {
      case "KEYWORD" -> tokenizer.keyWord().toLowerCase();
      case "SYMBOL" -> String.valueOf(tokenizer.symbol());
      case "INT_CONST" -> String.valueOf(tokenizer.intVal());
      case "STRING_CONST" -> tokenizer.stringVal();
      case "IDENTIFIER" -> tokenizer.identifier();
      default -> null;
    };

    String escaped = var.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;")
                       .replace("\"", "&quot;");
    writeLine("<" + tag + "> " + escaped + " </" + tag + ">");

    if (tokenizer.hasMoreTokens()) {
      tokenizer.advance();
    }
  }

  private void open(String tag) {
    writeLine("<" + tag + ">");
    indent++;
  }

  private void close(String tag) {
    indent--;
    writeLine("</" + tag + ">");
  }

  private void writeLine(String line) {
    writer.println("  ".repeat(indent) + line);
  }

  private boolean isSymbol(char c) {
    return tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == c;
  }

  private boolean isKeyword(String kw) {
    return tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyWord().equals(kw.toUpperCase());
  }

  public void compileClass() {
    open("class");
    eat();
    eat();
    eat();
    while (isKeyword("static") || isKeyword("field")) {
      compileClassVarDec();
    }
    while (isKeyword("constructor") || isKeyword("function") || isKeyword("method")) {
      compileSubroutine();
    }
    eat();
    close("class");
  }

  public void compileClassVarDec() {
    open("classVarDec");
    eat();
    do {
      eat();
      eat();
    } while (isSymbol(','));
    eat();
    close("classVarDec");
  }

  public void compileSubroutine() {
    open("subroutineDec");
    eat();
    eat();
    eat();
    eat();
    compileParameterList();
    eat();
    compileSubroutineBody();
    close("subroutineDec");
  }

  public void compileParameterList() {
    open("parameterList");
    if (tokenizer.tokenType().equals("KEYWORD") || tokenizer.tokenType().equals("IDENTIFIER")) {
      eat();
      eat();
      while (isSymbol(',')) {
        eat();
        eat();
        eat();
      }
    }
    close("parameterList");
  }

  public void compileSubroutineBody() {
    open("subroutineBody");
    eat();
    while (isKeyword("var")) {
      compileVarDec();
    }
    compileStatements();
    eat();
    close("subroutineBody");
  }

  public void compileVarDec() {
    open("varDec");
    eat();
    do {
      eat();
      eat();
    } while (isSymbol(','));
    eat();
    close("varDec");
  }

  public void compileStatements() {
    open("statements");
    while (tokenizer.tokenType().equals("KEYWORD")) {
      switch (tokenizer.keyWord()) {
        case "LET" -> compileLet();
        case "IF" -> compileIf();
        case "WHILE" -> compileWhile();
        case "DO" -> compileDo();
        case "RETURN" -> compileReturn();
        default -> {
          close("statements");
          return;
        }
      }
    }
    close("statements");
  }

  public void compileLet() {
    open("letStatement");
    eat();
    eat();
    if (isSymbol('[')) {
      eat();
      compileExpression();
      eat();
    }
    eat();
    compileExpression();
    eat();
    close("letStatement");
  }

  public void compileIf() {
    open("ifStatement");
    eat();
    eat();
    compileExpression();
    eat();
    eat();
    compileStatements();
    eat();
    if (isKeyword("else")) {
      eat();
      eat();
      compileStatements();
      eat();
    }
    close("ifStatement");
  }

  public void compileWhile() {
    open("whileStatement");
    eat();
    eat();
    compileExpression();
    eat();
    eat();
    compileStatements();
    eat();
    close("whileStatement");
  }

  public void compileDo() {
    open("doStatement");
    eat();
    eat();
    if (isSymbol('.')) {
      eat();
      eat();
    }
    eat();
    compileExpressionList();
    eat();
    eat();
    close("doStatement");
  }

  public void compileReturn() {
    open("returnStatement");
    eat();
    if (!isSymbol(';')) {
      compileExpression();
    }
    eat();
    close("returnStatement");
  }

  public void compileExpression() {
    open("expression");
    compileTerm();
    while (tokenizer.tokenType().equals("SYMBOL") && "+-*/&|<>=".indexOf(tokenizer.symbol()) >= 0) {
      eat();
      compileTerm();
    }
    close("expression");
  }

  public void compileTerm() {
    open("term");
    String type = tokenizer.tokenType();
    if (type.equals("INT_CONST") || type.equals("STRING_CONST") || type.equals("KEYWORD")) {
      eat();
    } else if (isSymbol('(')) {
      eat();
      compileExpression();
      eat();
    } else if (isSymbol('-') || isSymbol('~')) {
      eat();
      compileTerm();
    } else {
      eat();
      if (isSymbol('[')) {
        eat();
        compileExpression();
        eat();
      } else if (isSymbol('(')) {
        eat();
        compileExpressionList();
        eat();
      } else if (isSymbol('.')) {
        eat();
        eat();
        eat();
        compileExpressionList();
        eat();
      }
    }
    close("term");
  }

  public int compileExpressionList() {
    open("expressionList");
    int count = 0;
    if (!isSymbol(')')) {
      compileExpression();
      count++;
      while (isSymbol(',')) {
        eat();
        compileExpression();
        count++;
      }
    }
    close("expressionList");
    return count;
  }
}
