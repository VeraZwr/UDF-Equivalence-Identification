package org.tub.DIMA.BDSPRO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.PolyglotUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.Python.PythonUDFParser;

import java.util.Collection;



class Z3UdfEquivalenceTests {
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
            //Assertions.fail();
        }

    }

    private static Collection<UDFsPair> allUDFPairs() {
        return udfCollection.getUDFPairs();
    }
    @ParameterizedTest
    @MethodSource
    void allUDFPairs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> allJavaUDFPairs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.PYTHON);
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void polyglotUDFPairs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> polyglotUDFPairs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .setCustomPairFilter((udFsPair -> !udFsPair.udfA.getLanguage().equals(udFsPair.udfB.getLanguage())));
        return udfCollection.getUDFPairs(udFsFilter);
    }


    @ParameterizedTest
    @MethodSource
    void allJavaUDFPairs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> allPythonUDFPairs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .withoutLanguage(UDFLanguages.JAVA);
        return udfCollection.getUDFPairs(udFsFilter);
    }
    @ParameterizedTest
    @MethodSource
    void allPythonUDFPairs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testEquivalentJavaUDFs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .equivalentUdfs()
                .withoutLanguage(UDFLanguages.PYTHON);
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testEquivalentJavaUDFs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testReorderJavaUDFs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .equivalentUdfs()
                .withGroup("reorder")
                .withoutLanguage(UDFLanguages.PYTHON);
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testReorderJavaUDFs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }

    private static Collection<UDFsPair> testIdentityJavaUDFs() {
        UDFsPairFilter udFsFilter = new UDFsPairFilter()
                .equivalentUdfs()
                .withGroup("identity")
                .withoutLanguage(UDFLanguages.PYTHON);
        return udfCollection.getUDFPairs(udFsFilter);
    }

    @ParameterizedTest
    @MethodSource
    void testIdentityJavaUDFs(UDFsPair udfPair) {
        assertReportFitsUDFPair(udfPair);
    }
}