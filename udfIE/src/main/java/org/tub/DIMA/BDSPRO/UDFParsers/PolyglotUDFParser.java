package org.tub.DIMA.BDSPRO.UDFParsers;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.Python.PythonUDFParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper UDF Parser that delegates to the language specific Parsers
 */
public class PolyglotUDFParser extends UDFParser {
    private final JavaUDFParser javaUDFCompiler;
    private final PythonUDFParser pythonUDFCompiler;

    public PolyglotUDFParser(JavaUDFParser javaUDFCompiler, PythonUDFParser pythonUDFCompiler) {
        this.javaUDFCompiler = javaUDFCompiler;
        this.pythonUDFCompiler = pythonUDFCompiler;
    }

    @Override
    public Expr parseToZ3Expr(Context context, UDF udf) {
        return switch (udf.getLanguage()) {
            case PYTHON -> pythonUDFCompiler.parseToZ3Expr(context, udf);
            case JAVA -> javaUDFCompiler.parseToZ3Expr(context, udf);
        };
    };
}
