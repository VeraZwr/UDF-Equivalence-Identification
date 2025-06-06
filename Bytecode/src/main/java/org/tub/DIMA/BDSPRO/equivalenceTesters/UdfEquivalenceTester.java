package org.tub.DIMA.BDSPRO.equivalenceTesters;

import javassist.CtClass;
import org.tub.DIMA.BDSPRO.CompilableUDF;

public interface UdfEquivalenceTester {
    UDFDifferenceReport compare(CompilableUDF classA, CompilableUDF classB);
}
