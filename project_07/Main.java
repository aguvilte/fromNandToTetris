import java.io.*;

public class Main {

  public static void main(String[] args) throws IOException {
    String inputPath = args[0];
    String outputPath = inputPath.replace(".vm", ".asm");

    try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
         PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {

      Parser parser = new Parser(reader);
      CodeWriter codeWriter = new CodeWriter(writer);

      while (parser.hasMoreCommands()) {
        parser.advance();
        switch (parser.commandType()) {
          case "C_ARITHMETIC":
            codeWriter.writeArithmetic(parser.arg1());
            break;
          case "C_PUSH":
          case "C_POP":
            codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
            break;
        }
      }

      codeWriter.writeEndLoop();
      codeWriter.close();
    }
  }

}
