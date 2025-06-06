package org.tub.DIMA.BDSPRO.UDFParsers.Python;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.antlr.v4.grammars.Java20Lexer;
import org.antlr.v4.grammars.Java20Parser;
import org.antlr.v4.grammars.Python3Lexer;
import org.antlr.v4.grammars.Python3Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaToZ3FormulaASTWalker;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaWhileLoopUnrollerPreprocessor;
import org.tub.DIMA.BDSPRO.UDFParsers.UDFParser;
import org.tub.DIMA.BDSPRO.UDFLanguages;
import org.tub.DIMA.BDSPRO.UDF;

import java.io.IOException;

/**
 * A UDFParser for Python UDFs.
 */
public class PythonUDFParser extends UDFParser {

    public PythonUDFParser() {
    }

    @Override
    public Expr parseToZ3Expr(Context context, UDF udf) {
        CharStream charStream;
        try {
            charStream = CharStreams.fromStream(udf.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Python3Lexer lexer = new Python3Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        Python3Parser.File_inputContext parseTree = parser.file_input();

        PythonToZ3FormulaASTWalker remapper = new PythonToZ3FormulaASTWalker(parser,udf,context);
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        //parseTreeWalker.walk(new JavaPreprocessor(),parseTree);
        parseTreeWalker.walk(remapper, parseTree);

        return remapper.classContext.getMethodExpr("main");
    }
}
