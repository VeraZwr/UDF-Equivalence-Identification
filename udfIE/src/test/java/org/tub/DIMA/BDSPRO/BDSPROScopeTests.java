package org.tub.DIMA.BDSPRO;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.PolyglotUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.Python.PythonUDFParser;

import java.util.Collection;


class BDSPROScopeTests {
    static final UDFCollection udfCollection = new UDFCollection();
    private Z3UdfEquivalenceTester tester;
    @BeforeEach
    void setUp() {
        // Initialize Compiling Toolchains
        JavaUDFParser javaParser = new JavaUDFParser();
        PythonUDFParser pythonParser = new PythonUDFParser();
        PolyglotUDFParser polyglotUDFParser = new PolyglotUDFParser(javaParser, pythonParser);

        // Initialize Equivalence Tester
        tester = new Z3UdfEquivalenceTester(polyglotUDFParser);

    }

    void assertReportFitsUDFPair(UDFsPair udfPair) {
        UDFDifferenceReport report = tester.compare(udfPair.udfA, udfPair.udfB);
        System.out.println("Comparing "+udfPair.udfA.getIdentifier()+" and "+udfPair.udfB.getIdentifier()+":\n"+report.toString().indent(4));



        if (udfPair.equivalent && !report.equivalent){
            // False Negative. This is okay
            Assumptions.abort("False Negative Equivalence between "+udfPair.toString()+":\n"+report.toString());
        } else if (!udfPair.equivalent && report.equivalent){
            // False Positive. This is NOT okay
            throw new AssertionError("False Positive Equivalence:"+udfPair.toString()+report.toString());
        }

    }

    private static Collection<UDFsPair> testBasicOperators() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withGroup("operators.sum")
                .withGroup("operators.difference")
                .withGroup("operators.product")
                .withGroup("operators.quotient")
                .withGroup("operators.modulo")
                .withGroup("operators.bitwiseand")
                .withGroup("operators.bitwiseor")
                .withGroup("operators.bitwisexor");
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testBasicOperators(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testBasicControlFlows() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
            .withGroup("controlflows.ifstatement")
            .withGroup("controlflows.switchstatement");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testBasicControlFlows(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testFiniteLoops() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withGroup("controlflows.finitefor")
                .withGroup("controlflows.finitewhile");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testFiniteLoops(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    // JAVA

    private static Collection<UDFsPair> testBasicOperatorsInJava() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.PYTHON)
                .withGroup("operators.sum")
                .withGroup("operators.difference")
                .withGroup("operators.product")
                .withGroup("operators.quotient")
                .withGroup("operators.modulo")
                .withGroup("operators.bitwiseand")
                .withGroup("operators.bitwiseor")
                .withGroup("operators.bitwisexor");
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testBasicOperatorsInJava(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testBasicControlFlowsInJava() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.PYTHON)
                .withGroup("controlflows.ifstatement")
                .withGroup("controlflows.switchstatement");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testBasicControlFlowsInJava(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testFiniteLoopsInJava() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.PYTHON)
                .withGroup("controlflows.finitefor")
                .withGroup("controlflows.finitewhile");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testFiniteLoopsInJava(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    // PYTHON

    private static Collection<UDFsPair> testBasicOperatorsInPython() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.JAVA)
                .withGroup("operators.sum")
                .withGroup("operators.difference")
                .withGroup("operators.product")
                .withGroup("operators.quotient")
                .withGroup("operators.modulo")
                .withGroup("operators.bitwiseand")
                .withGroup("operators.bitwiseor")
                .withGroup("operators.bitwisexor");
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testBasicOperatorsInPython(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testBasicControlFlowsInPython() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.JAVA)
                .withGroup("controlflows.ifstatement")
                .withGroup("controlflows.switchstatement");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testBasicControlFlowsInPython(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testFiniteLoopsInPython() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.JAVA)
                .withGroup("controlflows.finitefor")
                .withGroup("controlflows.finitewhile");
        return udfCollection.getUDFPairs(udFsFilter);
    }



    @ParameterizedTest
    @MethodSource
    void testFiniteLoopsInPython(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }


}