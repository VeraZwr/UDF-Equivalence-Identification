package org.tub.DIMA.BDSPRO;


import com.microsoft.z3.*;
import org.tub.DIMA.BDSPRO.UDFParsers.UDFParser;

/**
 * Can compare two UDFs using the compare function.
 * Some Parser extending UDFParser is injected at construction.
 */
public class Z3UdfEquivalenceTester{
    private final UDFParser parser;

    public Z3UdfEquivalenceTester(UDFParser parser) {
        this.parser = parser;
    }


    /**
     * Compares two UDFs for equivalence. When a UDF contains invalid input or an unsupported feature we report UDFs to be different.
     */
    public UDFDifferenceReport compare(UDF udfA, UDF udfB) {
        if (udfA == null || udfB == null) {
            throw new IllegalArgumentException("UDFs must not be null");
        }

        Context context = new Context();

        Expr udfAExpr = parser.parseToZ3Expr(context,udfA);
        Expr udfBExpr = parser.parseToZ3Expr(context,udfB);



        Solver solver = context.mkSolver();
        solver.add(context.mkNot(context.mkEq(udfAExpr,udfBExpr)));

        String z3Formula = solver.toString();

        Status check = solver.check();

        switch (check){
            case UNSATISFIABLE -> {
                return UDFDifferenceReport.equivalent(udfA,udfB,"Z3 found no difference:\n"+z3Formula);
            }
            case UNKNOWN -> {
                return UDFDifferenceReport.different(udfA,udfB,"Z3 is not sure. ERROR?");
            }
            case SATISFIABLE -> {
                Model model = solver.getModel();

                Expr evalAExpr = model.eval(udfAExpr, true);
                Expr evalBExpr = model.eval(udfBExpr, true);
                String params = "(";
                for (int constIdx = 0; constIdx < model.getNumConsts(); constIdx++) {
                    params += model.getConstInterp(model.getConstDecls()[constIdx]);
                    params += ", ";
                }
                params += ")";

                String resultString = "udfA"+params+"="+evalAExpr+";\nudfB"+params+"="+evalBExpr+";\n";

                return UDFDifferenceReport.different(udfA,udfB,"Z3 found some difference:\n"+resultString.indent(4)+" \n\n" + z3Formula);
            }
            default -> throw new IllegalStateException("Unexpected value: " + check);
        }
    }




}
