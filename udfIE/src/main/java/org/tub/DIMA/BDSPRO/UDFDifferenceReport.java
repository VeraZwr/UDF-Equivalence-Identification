package org.tub.DIMA.BDSPRO;


/**
 * Utility class to pretty print the result of an equivalence test.
 */
public class UDFDifferenceReport {
    public final boolean equivalent;
    public final String description;

    public final UDF udfA;
    public final UDF udfB;

    private UDFDifferenceReport(boolean equivalent, String description, UDF udfA, UDF udfB) {
        this.equivalent = equivalent;
        this.description = description;
        this.udfA = udfA;
        this.udfB = udfB;
    }

    public static UDFDifferenceReport equivalent(UDF udfA, UDF udfB){
        return new UDFDifferenceReport(true, "",udfA,udfB);
    }

    public static UDFDifferenceReport equivalent(UDF udfA, UDF udfB,String message ){
        return new UDFDifferenceReport(true, message,udfA,udfB);
    }

    public static UDFDifferenceReport different(UDF udfA, UDF udfB, String description){
        return new UDFDifferenceReport(false, description,udfA,udfB);
    }

    public void report(){
        System.out.println(this);
    }
    @Override
    public String toString() {
        if (equivalent){
            return "The UDFs "+udfA.getIdentifier()+" and "+udfB.getIdentifier()+" are equivalent.\nUDFs:\n"+udfA.toString().trim().indent(4)+"\n"+udfB.toString().trim().indent(4);
        } else {
            return "Difference between "+udfA.getIdentifier()+" and "+udfB.getIdentifier()+" found: \n"+description.trim().indent(4)+"UDFs:\n"+udfA.toString().trim().indent(4)+"\n"+udfB.toString().trim().indent(4);
        }
    }
}
