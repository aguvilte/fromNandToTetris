public class Code {

  public static boolean[] dest(String mnemonic) {
    boolean[] dest = new boolean[]{false, false, false};
    if (mnemonic == null) {
      return dest;
    }
    if (mnemonic.contains("M")) {
      dest[2] = true;
    }
    if (mnemonic.contains("D")) {
      dest[1] = true;
    }
    if (mnemonic.contains("A")) {
      dest[0] = true;
    }
    return dest;
  }

  public static boolean[] comp(String mnemonic) {
    boolean a = mnemonic.contains("M");
    return switch (mnemonic) {
      case "0" -> new boolean[]{a, true, false, true, false, true, false};
      case "1" -> new boolean[]{a, true, true, true, true, true, true};
      case "-1" -> new boolean[]{a, true, true, true, false, true, false};
      case "D" -> new boolean[]{a, false, false, true, true, false, false};
      case "A", "M" -> new boolean[]{a, true, true, false, false, false, false};
      case "!D" -> new boolean[]{a, false, false, true, true, false, true};
      case "!A", "!M" -> new boolean[]{a, true, true, false, false, false, true};
      case "-D" -> new boolean[]{a, false, false, true, true, true, true};
      case "-A", "-M" -> new boolean[]{a, true, true, false, false, true, true};
      case "D+1" -> new boolean[]{a, false, true, true, true, true, true};
      case "A+1", "M+1" -> new boolean[]{a, true, true, false, true, true, true};
      case "D-1" -> new boolean[]{a, false, false, true, true, true, false};
      case "A-1", "M-1" -> new boolean[]{a, true, true, false, false, true, false};
      case "D+A", "D+M" -> new boolean[]{a, false, false, false, false, true, false};
      case "D-A", "D-M" -> new boolean[]{a, false, true, false, false, true, true};
      case "A-D", "M-D" -> new boolean[]{a, false, false, false, true, true, true};
      case "D&A", "D&M" -> new boolean[]{a, false, false, false, false, false, false};
      case "D|A", "D|M" -> new boolean[]{a, false, true, false, true, false, true};
      default -> throw new IllegalStateException("Unexpected value: " + mnemonic);
    };
  }

  public static boolean[] jump(String mnemonic) {
    if (mnemonic == null) {
      return new boolean[]{false, false, false};
    }
    return switch (mnemonic) {
      case "JGT" -> new boolean[]{false, false, true};
      case "JEQ" -> new boolean[]{false, true, false};
      case "JGE" -> new boolean[]{false, true, true};
      case "JLT" -> new boolean[]{true, false, false};
      case "JNE" -> new boolean[]{true, false, true};
      case "JLE" -> new boolean[]{true, true, false};
      case "JMP" -> new boolean[]{true, true, true};
      default -> new boolean[]{false, false, false};
    };
  }

}
