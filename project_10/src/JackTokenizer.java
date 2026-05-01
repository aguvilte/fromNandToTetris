import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JackTokenizer {

  private final List<String> tokens = new ArrayList<>();
  private int current = -1;
  private String token;
  private static final Set<String> keywordSet = new HashSet<>(List.of(
      "class", "constructor", "function", "method", "field", "static", "var", "int",
      "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else",
      "while", "return"
  ));

  private static final Set<Character> symbolSet = new HashSet<>(List.of(
      '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'
  ));

  public JackTokenizer(BufferedReader inputFile) throws IOException {
    StringBuilder src = new StringBuilder();
    String line;
    while ((line = inputFile.readLine()) != null) {
      src.append(line).append('\n');
    }
    tokenize(stripComments(src.toString()));
  }

  private String stripComments(String src) {
    StringBuilder out = new StringBuilder();
    int i = 0;
    while (i < src.length()) {
      char c = src.charAt(i);
      if (c == '"') {
        out.append(c); i++;
        while (i < src.length() && src.charAt(i) != '"') {
          out.append(src.charAt(i++));
        }
        if (i < src.length()) { out.append(src.charAt(i)); i++; }
      } else if (c == '/' && i + 1 < src.length() && src.charAt(i + 1) == '/') {
        while (i < src.length() && src.charAt(i) != '\n') i++;
      } else if (c == '/' && i + 1 < src.length() && src.charAt(i + 1) == '*') {
        i += 2;
        while (i + 1 < src.length() && !(src.charAt(i) == '*' && src.charAt(i + 1) == '/')) i++;
        if (i + 1 < src.length()) i += 2;
      } else {
        out.append(c); i++;
      }
    }
    return out.toString();
  }

  private void tokenize(String src) {
    int i = 0;
    while (i < src.length()) {
      char c = src.charAt(i);
      if (Character.isWhitespace(c)) {
        i++;
      } else if (c == '"') {
        StringBuilder sb = new StringBuilder("\"");
        i++;
        while (i < src.length() && src.charAt(i) != '"') {
          sb.append(src.charAt(i++));
        }
        sb.append('"');
        tokens.add(sb.toString());
        i++;
      } else if (Character.isDigit(c)) {
        StringBuilder sb = new StringBuilder();
        while (i < src.length() && Character.isDigit(src.charAt(i))) {
          sb.append(src.charAt(i++));
        }
        tokens.add(sb.toString());
      } else if (Character.isLetter(c) || c == '_') {
        StringBuilder sb = new StringBuilder();
        while (i < src.length() && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '_')) {
          sb.append(src.charAt(i++));
        }
        tokens.add(sb.toString());
      } else if (symbolSet.contains(c)) {
        tokens.add(String.valueOf(c));
        i++;
      } else {
        i++;
      }
    }
  }

  public boolean hasMoreTokens() {
    return current < tokens.size() - 1;
  }

  public void advance() {
    token = tokens.get(++current);
  }

  public String tokenType() {
    if (keywordSet.contains(token)) {
      return "KEYWORD";
    } else if (symbolSet.contains(token.charAt(0)) && token.length() == 1) {
      return "SYMBOL";
    }
    try {
      Integer.valueOf(token);
      return "INT_CONST";
    } catch (NumberFormatException e) {
      if (token.startsWith("\"")) {
        return "STRING_CONST";
      }
      return "IDENTIFIER";
    }
  }

  public String keyWord() {
    return token.toUpperCase();
  }

  public char symbol() {
    return token.charAt(0);
  }

  public String identifier() {
    return token;
  }

  public int intVal() {
    return Integer.parseInt(token);
  }

  public String stringVal() {
    return token.substring(1, token.length() - 1);
  }
}
