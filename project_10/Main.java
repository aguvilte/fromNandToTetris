import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: JackAnalyzer <file.jack | directory>");
      System.exit(1);
    }

    Path input = Paths.get(args[0]);
    List<Path> jackFiles;

    if (Files.isDirectory(input)) {
      jackFiles = Files.list(input)
          .filter(p -> p.toString().endsWith(".jack"))
          .collect(Collectors.toList());
    } else {
      jackFiles = List.of(input);
    }

    for (Path jackFile : jackFiles) {
      String tokenOutPath = jackFile.toString().replace(".jack", "T.xml");
      try (BufferedReader reader = new BufferedReader(new FileReader(jackFile.toFile()));
          PrintWriter writer = new PrintWriter(new FileWriter(tokenOutPath))) {
        tokenizeToXml(reader, writer);
      }
      System.out.println("Wrote " + tokenOutPath);

      String parseOutPath = jackFile.toString().replace(".jack", ".xml");
      try (BufferedReader reader = new BufferedReader(new FileReader(jackFile.toFile()));
          PrintWriter writer = new PrintWriter(new FileWriter(parseOutPath))) {
        JackTokenizer tokenizer = new JackTokenizer(reader);
        CompilationEngine engine = new CompilationEngine(tokenizer, writer);
        engine.compileClass();
      }
      System.out.println("Wrote " + parseOutPath);
    }
  }

  private static void tokenizeToXml(BufferedReader reader, PrintWriter writer) throws IOException {
    JackTokenizer tokenizer = new JackTokenizer(reader);
    writer.println("<tokens>");
    while (tokenizer.hasMoreTokens()) {
      tokenizer.advance();
      String type = tokenizer.tokenType();
      String tag = xmlTag(type);
      String value = xmlValue(tokenizer, type);
      writer.println("<" + tag + "> " + value + " </" + tag + ">");
    }
    writer.println("</tokens>");
  }

  private static String xmlTag(String tokenType) {
    return switch (tokenType) {
      case "KEYWORD" -> "keyword";
      case "SYMBOL" -> "symbol";
      case "INT_CONST" -> "integerConstant";
      case "STRING_CONST" -> "stringConstant";
      case "IDENTIFIER" -> "identifier";
      default -> throw new IllegalStateException("Unknown token type: " + tokenType);
    };
  }

  private static String xmlValue(JackTokenizer t, String type) {
    String var = switch (type) {
      case "KEYWORD" -> t.keyWord().toLowerCase();
      case "SYMBOL" -> String.valueOf(t.symbol());
      case "INT_CONST" -> String.valueOf(t.intVal());
      case "STRING_CONST" -> t.stringVal();
      case "IDENTIFIER" -> t.identifier();
      default -> throw new IllegalStateException("Unknown token type: " + type);
    };
    return var.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
