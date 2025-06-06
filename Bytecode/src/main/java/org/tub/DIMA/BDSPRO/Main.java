package org.tub.DIMA.BDSPRO;

import javassist.ClassPool;
import org.tub.DIMA.BDSPRO.UDFCompilers.Java.JavaUDFCompiler;
import org.tub.DIMA.BDSPRO.UDFCompilers.PolyglotUDFCompiler;
import org.tub.DIMA.BDSPRO.UDFCompilers.Python.PythonUDFCompiler;
import org.tub.DIMA.BDSPRO.equivalenceTesters.UDFDifferenceReport;
import org.tub.DIMA.BDSPRO.equivalenceTesters.UdfEquivalenceTester;
import org.tub.DIMA.BDSPRO.equivalenceTesters.Z3UdfEquivalenceTester;

import java.util.Collection;

public class Main {


    public static void main(String[] args) {
        ClassPool cp = ClassPool.getDefault();

        // Initialize Compiling Toolchains
        JavaUDFCompiler javaUDFCompiler = new JavaUDFCompiler(cp);
        PythonUDFCompiler pythonUDFCompiler = new PythonUDFCompiler(cp);
        PolyglotUDFCompiler polyglotUDFCompiler = new PolyglotUDFCompiler(javaUDFCompiler, pythonUDFCompiler);

        // Initialize Equivalence Tester
        UdfEquivalenceTester tester = new Z3UdfEquivalenceTester();

        // Get the testcases
        UDFCollection udfCollection = new UDFCollection();
        UDFsFilter udFsFilter = new UDFsPairFilter()
                .equivalentUdfs()
                .withGroup("identity")
                .withoutLanguage(UDFLanguages.PYTHON);
        Collection<UDFsPair> udfPairs = udfCollection.getUDFPairs();

        System.out.println("Got "+udfPairs.size()+" combinations:");
        for (UDFsPair udfPair : udfPairs) {
            System.out.println(udfPair.toString().indent(4));
        }

        for (UDFsPair udfPair :udfPairs ) {
            CompilableUDF udfA = new CompilableUDF(polyglotUDFCompiler,udfPair.udfA);
            CompilableUDF udfB = new CompilableUDF(polyglotUDFCompiler,udfPair.udfB);

            UDFDifferenceReport report = tester.compare(udfA, udfB);
            report.report();
        }
    }
}