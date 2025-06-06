package org.tub.DIMA.BDSPRO.UDFParsers.Java;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.antlr.v4.grammars.Java20Lexer;
import org.antlr.v4.grammars.Java20Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFParsers.UDFParser;

import java.io.IOException;


public class JavaUDFParser extends UDFParser {

    public JavaUDFParser() {

    }

    /**
     * A UDFParser for Java UDFs.
     *
     * Makes a pass with {@link JavaWhileLoopUnrollerPreprocessor} to unroll while loops before performing the remapping to z3 expressions using a pass with {@link JavaToZ3FormulaASTWalker}
     */
    @Override
    public Expr parseToZ3Expr(Context context, UDF udf) {
        CharStream charStream;
        try {
            charStream = CharStreams.fromStream(udf.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Java20Lexer lexer = new Java20Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java20Parser parser = new Java20Parser(tokens);
        Java20Parser.Start_Context parseTree = parser.start_();

        JavaToZ3FormulaASTWalker remapper = new JavaToZ3FormulaASTWalker(parser,udf,context);
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        parseTreeWalker.walk(new JavaWhileLoopUnrollerPreprocessor(),parseTree);
        parseTreeWalker.walk(remapper, parseTree);

        return remapper.classContext.getMethodExpr("main");
    }
}
