import java.io.PrintWriter;
import java.util.Map;

public class CodeWriter {

  private final PrintWriter out;
  private int counter = 0;

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
        emit("(EQ_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emit("(EQ_END_" + counter + ")");
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
        emit("(LT_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emit("(LT_END_" + counter + ")");
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
        emit("(GT_TRUE_" + counter + ")");
        emit("@SP");
        emit("A=M-1");
        emit("M=-1");
        emit("(GT_END_" + counter + ")");
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
          popDToAddr("Foo." + index);
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
        emit("@Foo." + index);
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
    emit("D=M+D");
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
    out.println(instruction);
  }

  public void writeEndLoop() {
    emit("(END)");
    emit("@END");
    emit("0;JMP");
  }

  public void close() {
    out.close();
  }


}
