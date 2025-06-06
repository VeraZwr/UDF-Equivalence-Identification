package org.tub.DIMA.BDSPRO;

import org.tub.DIMA.BDSPRO.UDFParsers.Java.JavaUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.PolyglotUDFParser;
import org.tub.DIMA.BDSPRO.UDFParsers.Python.PythonUDFParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class Main {


    /**
     * Runs a simple equality check on the two passed UDF files.
     *
     * OR
     *
     * Runs equivalence on all UDF Testcases combinations.
     *
     * @param args
     */
    public static void main(String[] args) {

        // Initialize Compiling Toolchains
        JavaUDFParser javaParser = new JavaUDFParser();
        PythonUDFParser pythonParser = new PythonUDFParser();
        PolyglotUDFParser polyglotUDFParser = new PolyglotUDFParser(javaParser, pythonParser);

        // Initialize Equivalence Tester
        Z3UdfEquivalenceTester tester = new Z3UdfEquivalenceTester(polyglotUDFParser);


        if (args.length == 2) {

            UDF udfA = resolveUDF(args[0]);
            UDF udfB = resolveUDF(args[1]);

            UDFDifferenceReport report = tester.compare(udfA, udfB);
            System.out.println("Comparing "+udfA.getIdentifier()+" and "+udfB.getIdentifier()+":\n"+report.toString().indent(4));
        } else {
            // Get the testcases
            UDFCollection udfCollection = new UDFCollection();
            UDFsPairFilter udFsFilter = new UDFsPairFilter();
            Collection<UDFsPair> udfPairs = udfCollection.getUDFPairs(udFsFilter);

            System.out.println("Got "+udfPairs.size()+" combinations:");

            for (UDFsPair udfPair :udfPairs ) {
                UDFDifferenceReport report = tester.compare(udfPair.udfA, udfPair.udfB);
                System.out.println("Comparing "+udfPair.udfA.getIdentifier()+" and "+udfPair.udfB.getIdentifier()+":\n"+report.toString().indent(4));
            }
        }






    }

    private static UDF resolveUDF(String filename) {
        Path path = Paths.get(filename);
        URL url = null;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return new UDF(url);
    }
}