package org.tub.DIMA.BDSPRO;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.PolyglotUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.Python.PythonUDFParser;

import java.util.Collection;


class ParserTests {
    static final UDFCollection udfCollection = new UDFCollection();
    private PolyglotUDFParser parser;
    @BeforeEach
    void setUp() {
        // Initialize Compiling Toolchains
        JavaUDFParser javaParser = new JavaUDFParser();
        PythonUDFParser pythonParser = new PythonUDFParser();
        parser = new PolyglotUDFParser(javaParser, pythonParser);
    }

    void assertParserCanParse(UDF udf) {
        System.out.println(udf);
        Context context = new Context();
        Expr expr = parser.parseToZ3Expr(context, udf);
        System.out.println("Resulting Expr: "+expr.toString());
    }

    private static Collection<UDF> allUDFs() {
        return udfCollection.getUDFs();
    }
    @ParameterizedTest
    @MethodSource
    void allUDFs(UDF udfPair) {
        assertParserCanParse(udfPair);
    }

    private static Collection<UDF> allJavaUDFs() {
        UDFsFilter filter = new UDFsFilter().withoutLanguage(UDFLanguages.PYTHON);
        return udfCollection.getUDFs(filter);
    }

    @ParameterizedTest
    @MethodSource
    void allJavaUDFs(UDF udfPair) {
        assertParserCanParse(udfPair);
    }
}