package org.tub.DIMA.BDSPRO.UDFParsers.AST;

import com.microsoft.z3.Expr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.*;

/**
 * This is a Node that can be attached to a {@link org.antlr.v4.runtime.tree.ParseTree} in order to store The resulting expressions of Functions.
 * Useful to register the main UDF method and store its returned Expression.
 * This Class is interacted with through its static methods.
 */
public class ClassContext extends TerminalNodeImpl {
    final AstException.AstExceptionBuilder problemBuilder;
    HashMap<String, Expr> methodExpressions = new HashMap<>();

    public ClassContext(Token symbol, AstException.AstExceptionBuilder problemBuilder) {
        super(symbol);
        this.problemBuilder = problemBuilder;
    }
    /**
     * Attaches a new ClassContext to the AST at {@code ctx}.
     *
     * @param ctx The AST node that represents the class
     * @param problemBuilder for error formatting
     * @return the created Variable Scope
     */
    public static ClassContext attachClass(ParserRuleContext ctx,AstException.AstExceptionBuilder problemBuilder){
        Token start = ctx.getStart();
        ClassContext classContext = new ClassContext(start,problemBuilder);
        ctx.addAnyChild(classContext);
        return classContext;
    }
    /**
     * Finds an attached ClassContext from {@code ctx}.
     *
     * @param ctx the location where to query for a ClassContext
     * @param problemBuilder for error formatting
     * @return the created Variable Scope
     */
    public static ClassContext resolveClass(ParserRuleContext ctx, AstException.AstExceptionBuilder problemBuilder){
        for (RuleContext parent = ctx;parent != null; parent = parent.parent){
            for (int child = 0; child < parent.getChildCount(); child++) {
                ParseTree potentialVariableScope = parent.getChild(child);
                if (potentialVariableScope instanceof ClassContext){
                    return (ClassContext) potentialVariableScope;
                }
            }
        }
        throw problemBuilder.inContext(ctx,"was called with a context OUTSIDE of a class body!");
    }

    /**
     * Obtain the Expression of the function.
     *
     * @param methodName the name of the function whose Expression should be returned
     * @return the Expression of the function
     */
    public Expr getMethodExpr(String methodName){
        if (!methodExpressions.containsKey(methodName)){
            throw problemBuilder.build("The Class did not define \""+methodName+"\" method. \nThese methods were defined: "+String.join(", ",methodExpressions.keySet()));
        }

        return  methodExpressions.get(methodName);
    }

    /**
     * Store the Exression as the expression that describes the result of the method.
     * @param methodName the name of the method
     * @param z3Expr an Expression describing the result of the method.
     */
    public void declareMethod(String methodName, Expr z3Expr) {
        methodExpressions.put(methodName,z3Expr);
    }
}
