package org.tub.DIMA.BDSPRO.UDFParsers.AST;

import com.microsoft.z3.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Utilities that can be shared across the AST to Z3 Expression remapping logic for multiple Languages  
 */
public class AstUtils {

    /**
     * Get the z3 Expr describing a value.
     *
     * @param exceptionBuilder for error formatting
     * @param ctx the AST node
     * @return the Expression representing the value of {@code ctx}
     */
    public static Supplier<Expr> getZ3Expr(AstException.AstExceptionBuilder exceptionBuilder, ParserRuleContext ctx) {
        ParseTree last = ctx.children.getLast();

        if (last == null ){
            throw exceptionBuilder.inContext(ctx,"There is no child registerred for this Antlr AST (not even a Z3 expression)");

        } else if (!(last instanceof Z3ExpressionTerminalNode)) {
            throw exceptionBuilder.inContext(ctx,"There is no Z3 Expression registerred for this Antlr AST");
        }

        Z3ExpressionTerminalNode child = (Z3ExpressionTerminalNode) last;

        return child.getUnresolvedExpr();
    }


    /**
     * Propagates an expression.
     *
     * @param exceptionBuilder for error formatting 
     * @param to where to propagate to
     * @param from where to propagate from
     */
    public static void propagateZ3Expression(AstException.AstExceptionBuilder exceptionBuilder, ParserRuleContext to, ParserRuleContext from) {
        ParseTree last = from.children.getLast();

        if (last == null ){
            throw exceptionBuilder.inContext(from,"There is no child registerred for this Antlr AST (not even a Z3 expression)");

        } else if (!(last instanceof Z3ExpressionTerminalNode)) {
            throw exceptionBuilder.inContext(from,"There is no Z3 Expression registerred for this Antlr AST");
        }

        Z3ExpressionTerminalNode child = (Z3ExpressionTerminalNode) last;


        setZ3Expression(to, child.getUnresolvedExpr());
    }


    /**
     * Sets an Expression to represent the value of the given ctx
     */
    public static void setZ3Expression(ParserRuleContext ctx, Expr expr) {
        Token start = ctx.getStart();
        Z3ExpressionTerminalNode sz3ExpressionTerminalNode = new Z3ExpressionTerminalNode(start, expr);
        ctx.addAnyChild(sz3ExpressionTerminalNode);
    }

    /**
     * Sets a lazy Expression to represent the value of the given ctx. This expression can be resolved later on demand.
     */
    public static void setZ3Expression(ParserRuleContext ctx, Supplier<Expr> expr) {
        Token start = ctx.getStart();
        Z3ExpressionTerminalNode sz3ExpressionTerminalNode = new Z3ExpressionTerminalNode(start, expr);
        ctx.addAnyChild(sz3ExpressionTerminalNode);
    }

    /**
     * Get the z3 Expr describing a value as a BitVectorExpression.
     *
     * @param exceptionBuilder for error formatting
       @param context the z3 Context needed to construct z3Expressions
     * @param ctx the AST node
     * @return the Expression representing the value of {@code ctx}
     */
    public static Supplier<BitVecExpr> getZ3BVExpr(Context context, AstException.AstExceptionBuilder exceptionBuilder, ParserRuleContext ctx) {
        Supplier<Expr> unresolvedZ3Expr = getZ3Expr(exceptionBuilder, ctx);
        return ()->{
            Expr z3Expr = unresolvedZ3Expr.get();

            if(z3Expr instanceof BitVecExpr) {
                return (BitVecExpr) z3Expr;
            } else if (z3Expr instanceof IntExpr) {
                return context.mkInt2BV(32,z3Expr);
            } else if (z3Expr instanceof BoolExpr) {
                return context.mkInt2BV(1,z3Expr);
            } else {
                throw exceptionBuilder.inContext(ctx,"Expression was of unknown Type and could not be transformed to a BitVecExpr");
            }
        };
    }

    /**
     * Get the z3 Expr describing a value as a IntExpr.
     *
     * @param exceptionBuilder for error formatting
     @param context the z3 Context needed to construct z3Expressions
      * @param ctx the AST node
     * @return the Expression representing the value of {@code ctx}
     */
    public static Supplier<IntExpr> getZ3IntExpr(Context context, AstException.AstExceptionBuilder exceptionBuilder, ParserRuleContext ctx) {
        Supplier<Expr> unresolvedZ3Expr = getZ3Expr(exceptionBuilder, ctx);
        return ()->{
            Expr z3Expr = unresolvedZ3Expr.get();
            if(z3Expr instanceof IntExpr) {
                return (IntExpr) z3Expr;
            } else if (z3Expr instanceof BitVecExpr) {
                return context.mkBV2Int(z3Expr,true);
            } else if (z3Expr instanceof BoolExpr) {
                return (IntExpr) context.mkITE(z3Expr,context.mkInt(1),context.mkInt(0));
            } else {
                throw exceptionBuilder.inContext(ctx,"Expression was of unknown Type and could not be transformed to a IntExpr");
            }
        };
    }

    /**
     * A Helper Method to handle setting the right values for Variables when a block is exited.
     * You can think of this method as inserting an instruction after a block that sets the variable to the
     * value BEFORE the block executed or the value it was assigned to INSIDE the block depending on the condition.
     *
     *
     * @param context
     * @param exceptionBuilder for error formatting
     * @param ctx the AST node just outside of the block
     * @param condition the condition when the block is executed
     * @param changedVariables the variables that are set inside the block.
     */
    public static void exitBlockHelper(Context context, AstException.AstExceptionBuilder exceptionBuilder,ParserRuleContext ctx,Expr<BoolSort> condition, VariablesScope changedVariables){
        for (Map.Entry<String, Expr> changedVariable : changedVariables.variables.entrySet()) {
            String name = changedVariable.getKey();
            Expr newValue = changedVariable.getValue();


            // This try catch handles the case where a previously uninitialized variable is set in a block.
            // We assume that we can leave out this brach because it effectively overrides the "default" state of the var, which was uninitialized.
            // So this value can only leak out if the code has a branch that does not initialize the value.
            // This would mean the code is invalid and another tool should've caught this :)

            Expr resultingValue;
            try {
                Expr previousValue = VariablesScope.getVariable(ctx, name, exceptionBuilder);
                resultingValue = context.mkITE(condition,newValue,previousValue);
            }catch (AstException e){
                resultingValue = newValue;
            }
            //System.out.println(name+" = "+resultingValue);
            VariablesScope.setVariable(ctx,name,resultingValue,exceptionBuilder);
        }
    }

}
