import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.err.println("Usage: Assembler <file.asm>");
      return;
    }

    String inputPath = args[0];
    String outputPath = inputPath.replace(".asm", ".hack");

    SymbolTable symbolTable = new SymbolTable();

    Parser parser = new Parser(new BufferedReader(new FileReader(inputPath)));
    int romAddress = 0;
    while (parser.hasMoreCommands()) {
      parser.advance();
      String type = parser.commandType();
      if ("L_COMMAND".equals(type)) {
        symbolTable.addEntry(parser.symbol(), romAddress);
      } else {
        romAddress++;
      }
    }

    parser = new Parser(new BufferedReader(new FileReader(inputPath)));
    int ramAddress = 16;

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
      while (parser.hasMoreCommands()) {
        parser.advance();
        String type = parser.commandType();
        String binaryInstruction;

        if ("A_COMMAND".equals(type)) {
          String symbol = parser.symbol();
          int value;
          if (symbol.matches("\\d+")) {
            value = Integer.parseInt(symbol);
          } else if (symbolTable.contains(symbol)) {
            value = symbolTable.GetAddress(symbol);
          } else {
            symbolTable.addEntry(symbol, ramAddress);
            value = ramAddress++;
          }
          binaryInstruction = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');
        } else if ("C_COMMAND".equals(type)) {
          boolean[] comp = Code.comp(parser.comp());
          boolean[] dest = Code.dest(parser.dest());
          boolean[] jump = Code.jump(parser.jump());
          StringBuilder sb = new StringBuilder("111");
          for (boolean b : comp) sb.append(b ? '1' : '0');
          for (boolean b : dest) sb.append(b ? '1' : '0');
          for (boolean b : jump) sb.append(b ? '1' : '0');
          binaryInstruction = sb.toString();
        } else {
          continue;
        }

        writer.write(binaryInstruction);
        writer.newLine();
      }
    }
  }

}