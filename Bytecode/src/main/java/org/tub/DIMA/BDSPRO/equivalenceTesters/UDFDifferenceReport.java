package org.tub.DIMA.BDSPRO.equivalenceTesters;

import org.tub.DIMA.BDSPRO.CompilableUDF;

public class UDFDifferenceReport {
    public final boolean equivalent;
    public final String description;

    public final CompilableUDF udfA;
    public final CompilableUDF udfB;

    private UDFDifferenceReport(boolean equivalent, String description, CompilableUDF udfA, CompilableUDF udfB) {
        this.equivalent = equivalent;
        this.description = description;
        this.udfA = udfA;
        this.udfB = udfB;
    }

    public static UDFDifferenceReport equivalent(CompilableUDF udfA, CompilableUDF udfB){
        return new UDFDifferenceReport(true, "",udfA,udfB);
    }

    public static UDFDifferenceReport different(CompilableUDF udfA, CompilableUDF udfB,String description){
        return new UDFDifferenceReport(false, description,udfA,udfB);
    }

    public void assertEquivalent(){
        if (!equivalent){
            throw new AssertionError(description);
        }
    }

    public void report(){
        System.out.println(this);
    }
    @Override
    public String toString() {
        if (equivalent){
            return "The UDFs "+udfA.udf.getIdentifier()+" and "+udfB.udf.getIdentifier()+" are equivalent.";
        } else {
            return "Difference between "+udfA.udf.getIdentifier()+" and "+udfB.udf.getIdentifier()+" found: \n"+description.trim().indent(4)+"UDFs:\n"+udfA.toString().trim().indent(4)+"\n"+udfB.toString().trim().indent(4);
        }
    }
}
