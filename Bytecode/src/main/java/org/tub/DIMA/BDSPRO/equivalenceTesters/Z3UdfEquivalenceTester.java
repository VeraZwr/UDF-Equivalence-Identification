package org.tub.DIMA.BDSPRO.equivalenceTesters;

import javassist.CtClass;
import javassist.NotFoundException;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.InstructionPrinter;
import org.tub.DIMA.BDSPRO.CompilableUDF;

public class Z3UdfEquivalenceTester implements UdfEquivalenceTester{

    @Override
    public UDFDifferenceReport compare(CompilableUDF udfA, CompilableUDF udfB) {
        if (udfA == null || udfB == null) {
            throw new IllegalArgumentException("UDFs must not be null");
        }

        try {
            CtClass udfClassA = udfA.compile();
            CtClass udfClassB = udfB.compile();



            if (udfClassA == null && udfClassB == null) {
                return UDFDifferenceReport.different(udfA,udfB,"BOTH UDFs were null. Compilation Failure!");
            } else if (udfClassA == null) {
                return UDFDifferenceReport.different(udfA,udfB,"First UDF compiled to null. Compilation Failure!");
            } else if (udfClassB == null) {
                return UDFDifferenceReport.different(udfA,udfB,"Second UDF compiled to null. Compilation Failure!");
            }


            // TODO: use the javassist.bytecode.analysis namespace Tooling to transform into Z3 Formula :)
            CodeIterator bytecode = udfClassA.getDeclaredMethod("main").getMethodInfo().getCodeAttribute().iterator();
            String classABytecode = InstructionPrinter.instructionString(bytecode, 0, null);
            String classBBytecode = InstructionPrinter.instructionString(udfClassB.getDeclaredMethod("main").getMethodInfo().getCodeAttribute().iterator(), 0, null);
            if (classABytecode.equals(classBBytecode)) {
                return UDFDifferenceReport.equivalent(udfA,udfB);
            }
            // Iterate over the bytecode
            return UDFDifferenceReport.different(udfA,udfB,"IDK, prolly different :)");
        }catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }



}
