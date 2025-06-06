import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
    public static void main(String[] args) throws Exception {
        String pythonCode = "def hello():\n    print('Hello, world!')";
        CharStream input = CharStreams.fromString(pythonCode);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        ParseTree tree = parser.file_input();
        System.out.println(tree.toStringTree(parser));
    }
}
