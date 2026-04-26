import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

  private final List<String> lines;
  private int current;
  private String command;

  public Parser(BufferedReader inputFile) throws IOException {
    lines = new ArrayList<>();
    String line;
    while ((line = inputFile.readLine()) != null) {
      if (line.contains("//")) {
        line = line.split("//")[0];
      }
      line = line.trim();
      if (!line.isEmpty()) {
        lines.add(line);
      }
    }
    current = -1;
  }

  public boolean hasMoreCommands() {
    return current < lines.size() - 1;
  }

  public void advance() {
    command = lines.get(++current);
  }

  public String commandType() {
    if (command.startsWith("add") || command.startsWith("sub")
        || command.startsWith("neg") || command.startsWith("eq")
        || command.startsWith("gt") || command.startsWith("lt")
        || command.startsWith("and") || command.startsWith("or")
        || command.startsWith("not")) {
      return "C_ARITHMETIC";
    } else if (command.startsWith("push")) {
      return "C_PUSH";
    } else if (command.startsWith("pop")) {
      return "C_POP";
    } else if (command.startsWith("label")) {
      return "C_LABEL";
    } else if (command.startsWith("goto")) {
      return "C_GOTO";
    } else if (command.startsWith("if-goto")) {
      return "C_IF";
    } else if (command.startsWith("function")) {
      return "C_FUNCTION";
    } else if (command.startsWith("call")) {
      return "C_CALL";
    } else if (command.startsWith("return")) {
      return "C_RETURN";
    } else {
      return null;
    }
  }

  public String arg1() {
    if ("C_ARITHMETIC".equals(commandType())) {
      return command;
    }
    return command.split(" ")[1];
  }

  public int arg2() {
    return Integer.parseInt(command.split(" ")[2]);
  }

}
