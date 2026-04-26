import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) throws IOException {
    File input = new File(args[0]);

    List<File> vmFiles;
    String outputPath;
    boolean isDirectory = input.isDirectory();

    if (isDirectory) {
      File[] files = input.listFiles((dir, name) -> name.endsWith(".vm"));
      vmFiles = Arrays.stream(files).sorted().collect(Collectors.toList());
      outputPath = input.getPath() + "/" + input.getName() + ".asm";
    } else {
      vmFiles = List.of(input);
      outputPath = args[0].replace(".vm", ".asm");
    }

    try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
      CodeWriter codeWriter = new CodeWriter(writer);

      if (isDirectory) {
        codeWriter.writeInit();
      }

      for (File vmFile : vmFiles) {
        String fileName = vmFile.getName().replace(".vm", "");
        codeWriter.setFileName(fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(vmFile))) {
          Parser parser = new Parser(reader);
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
              case "C_LABEL":
                codeWriter.writeLabel(parser.arg1());
                break;
              case "C_GOTO":
                codeWriter.writeGoto(parser.arg1());
                break;
              case "C_IF":
                codeWriter.writeIf(parser.arg1());
                break;
              case "C_FUNCTION":
                codeWriter.writeFunction(parser.arg1(), parser.arg2());
                break;
              case "C_CALL":
                codeWriter.writeCall(parser.arg1(), parser.arg2());
                break;
              case "C_RETURN":
                codeWriter.writeReturn();
                break;
            }
          }
        }
      }

      codeWriter.writeEndLoop();
      codeWriter.close();
    }
  }

}
