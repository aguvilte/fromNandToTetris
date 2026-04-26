import java.io.PrintWriter;
import java.util.Map;

public class CodeWriter {

  private final PrintWriter out;
  private int counter = 0;
  private String currentFile = "Foo";

  private final Map<String, String> segmentTable = new java.util.HashMap<>(Map.of(
      "local", "LCL",
      "argument", "ARG",
      "this", "THIS",
      "that", "THAT",
      "constant", "SP",
      "static", "",
      "temp", "R5"
  ));

  public CodeWriter(PrintWriter out) {
    this.out = out;
  }

  public void setFileName(String name) {
    this.currentFile = name;
  }

  public void writeInit() {
    emit("@256");
    emit("D=A");
    emit("@SP");
    emit("M=D");
    writeCall("Sys.init", 0);
  }

  public void writeArithmetic(String command) {
    switch (command) {
      case "not":
        emit("@SP");
        emit("A=M-1");
        emit("M=!M");
        break;
      case "neg":
        emit("@SP");
        emit("A=M-1");
        emit("M=-M");
        break;
      case "add":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("M=D+M");
        break;
      case "sub":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("M=M-D");
        break;
      case "and":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("M=M&D");
        break;
      case "or":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("M=M|D");
        break;
      case "eq":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("D=M-D");
        emit("@EQ_TRUE_" + counter);
        emit("D;JEQ");
        emit("@SP");
        emit("A=M-1");
        emit("M=0");
        emit("@EQ_END_" + counter);
        emit("0;JMP");
        emitLabel("(EQ_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emitLabel("(EQ_END_" + counter + ")");
        counter++;
        break;
      case "lt":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("D=M-D");
        emit("@LT_TRUE_" + counter);
        emit("D;JLT");
        emit("@SP");
        emit("A=M-1");
        emit("M=0");
        emit("@LT_END_" + counter);
        emit("0;JMP");
        emitLabel("(LT_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emitLabel("(LT_END_" + counter + ")");
        counter++;
        break;
      case "gt":
        emit("@SP");
        emit("M=M-1");
        emit("A=M");
        emit("D=M");
        emit("A=A-1");
        emit("D=M-D");
        emit("@GT_TRUE_" + counter);
        emit("D;JGT");
        emit("@SP");
        emit("A=M-1");
        emit("M=0");
        emit("@GT_END_" + counter);
        emit("0;JMP");
        emitLabel("(GT_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emitLabel("(GT_END_" + counter + ")");
        counter++;
        break;
    }
  }

  public void writePushPop(String command, String segment, int index) {
    if ("C_PUSH".equals(command)) {
      loadD(segment, index);
      pushDToStack();
    } else if ("C_POP".equals(command)) {
      switch (segment) {
        case "pointer":
          popDToAddr(index == 0 ? "THIS" : "THAT");
          break;
        case "temp":
          popDToAddr(String.valueOf(5 + index));
          break;
        case "static":
          popDToAddr(currentFile + "." + index);
          break;
        default:
          storeAddrInR13(segment, index);
          popDFromStack();
          emit("@R13");
          emit("A=M");
          emit("M=D");
      }
    }
  }

  public void writeLabel(String label) {
    emitLabel("(" + label + ")");
  }

  public void writeGoto(String label) {
    emit("@" + label);
    emit("0;JMP");
  }

  public void writeIf(String label) {
    popDFromStack();
    emit("@" + label);
    emit("D;JNE");
  }

  public void writeCall(String functionName, int nArgs) {
    int callIndex = counter++;
    emit("@" + functionName + "$ret." + callIndex);
    emit("D=A");
    pushDToStack();

    emit("@LCL");
    emit("D=M");
    pushDToStack();

    emit("@ARG");
    emit("D=M");
    pushDToStack();

    emit("@THIS");
    emit("D=M");
    pushDToStack();

    emit("@THAT");
    emit("D=M");
    pushDToStack();

    emit("@SP");
    emit("D=M");
    emit("@5");
    emit("D=D-A");
    emit("@" + nArgs);
    emit("D=D-A");
    emit("@ARG");
    emit("M=D");

    emit("@SP");
    emit("D=M");
    emit("@LCL");
    emit("M=D");

    emit("@" + functionName);
    emit("0;JMP");

    writeLabel(functionName + "$ret." + callIndex);
  }

  public void writeFunction(String functionName, int nVars) {
    emitLabel("(" + functionName + ")");
    for (int i = 0; i < nVars; i++) {
      writePushPop("C_PUSH", "constant", 0);
    }
  }

  public void writeReturn() {
    emit("@LCL");
    emit("D=M");
    emit("@R14");
    emit("M=D");

    emit("@5");
    emit("D=A");
    emit("@R14");
    emit("D=M-D");
    emit("A=D");
    emit("D=M");
    emit("@R15");
    emit("M=D");

    writePushPop("C_POP", "argument", 0);

    emit("@ARG");
    emit("D=M+1");
    emit("@SP");
    emit("M=D");

    emit("@1");
    emit("D=A");
    emit("@R14");
    emit("D=M-D");
    emit("A=D");
    emit("D=M");
    emit("@THAT");
    emit("M=D");

    emit("@2");
    emit("D=A");
    emit("@R14");
    emit("D=M-D");
    emit("A=D");
    emit("D=M");
    emit("@THIS");
    emit("M=D");

    emit("@3");
    emit("D=A");
    emit("@R14");
    emit("D=M-D");
    emit("A=D");
    emit("D=M");
    emit("@ARG");
    emit("M=D");

    emit("@4");
    emit("D=A");
    emit("@R14");
    emit("D=M-D");
    emit("A=D");
    emit("D=M");
    emit("@LCL");
    emit("M=D");

    emit("@R15");
    emit("A=M");
    emit("0;JMP");
  }

  private void loadD(String segment, int index) {
    switch (segment) {
      case "constant":
        emit("@" + index);
        emit("D=A");
        break;
      case "temp":
        emit("@" + index);
        emit("D=A");
        emit("@5");
        emit("A=D+A");
        emit("D=M");
        break;
      case "static":
        emit("@" + currentFile + "." + index);
        emit("D=M");
        break;
      case "pointer":
        emit(index == 0 ? "@THIS" : "@THAT");
        emit("D=M");
        break;
      default:
        emit("@" + index);
        emit("D=A");
        emit("@" + segmentTable.get(segment));
        emit("A=D+M");
        emit("D=M");
    }
  }

  private void pushDToStack() {
    emit("@SP");
    emit("A=M");
    emit("M=D");
    emit("@SP");
    emit("M=M+1");
  }

  private void storeAddrInR13(String segment, int index) {
    emit("@" + index);
    emit("D=A");
    emit("@" + segmentTable.get(segment));
    emit("D=D+M");
    emit("@R13");
    emit("M=D");
  }

  private void popDToAddr(String addr) {
    popDFromStack();
    emit("@" + addr);
    emit("M=D");
  }

  private void popDFromStack() {
    emit("@SP");
    emit("M=M-1");
    emit("A=M");
    emit("D=M");
  }

  private void emit(String instruction) {
    out.println("  " + instruction);
  }

  private void emitLabel(String instruction) {
    out.println(instruction);
  }

  public void writeEndLoop() {
    emitLabel("(END)");
    emit("@END");
    emit("0;JMP");
  }

  public void close() {
    out.close();
  }

}
