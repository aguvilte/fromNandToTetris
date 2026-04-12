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
    current++;
    command = lines.get(current);
  }

  public String commandType() {
    if (command.startsWith("@")) {
      return "A_COMMAND";
    } else if (command.startsWith("(")) {
      return "L_COMMAND";
    } else {
      return "C_COMMAND";
    }
  }

  public String symbol() {
    if (command.startsWith("@")) {
      return command.substring(1);
    } else {
      return command.substring(1, command.length() - 1);
    }
  }

  public String dest() {
    return command.contains("=") ? command.split("=")[0] : null;
  }

  public String comp() {
    String comp = command;
    if (command.contains("=")) {
      comp = command.split("=")[1];
    }
    if (comp.contains(";")) {
      comp = comp.split(";")[0];
    }
    return comp;
  }

  public String jump() {
    return command.contains(";") ? command.split(";")[1] : null;
  }

}