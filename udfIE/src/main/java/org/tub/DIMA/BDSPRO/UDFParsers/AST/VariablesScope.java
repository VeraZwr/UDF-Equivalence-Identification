package org.tub.DIMA.BDSPRO.UDFParsers.AST;

import com.microsoft.z3.Expr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.*;
import java.util.function.Predicate;

/**
 * This is a Node that can be attached to a {@link org.antlr.v4.runtime.tree.ParseTree} in order to store a Scope of Variables directly in the AST.
 * Multiple Variable Scope instances attached to the path to the root of the AST can be used to retrieve values for variables.
 * This Class is interacted with through its static methods.
 */
public class VariablesScope extends TerminalNodeImpl {
    public HashMap<String, Expr> variables = new HashMap<>();

    public VariablesScope(Token symbol) {
        super(symbol);
    }

    /**
     * Attaches a new Variable Scope with initial variable values to the AST at {@code ctx}.
     *
     * @param ctx The AST node that represents the scope where the variables live
     * @param initialValues the initial values for the variables
     * @return the created Variable Scope
     */
    public static VariablesScope addVariableScope(ParserRuleContext ctx,  HashMap<String,Expr> initialValues){
        Token start = ctx.getStart();
        VariablesScope variablesScope = new VariablesScope(start);
        variablesScope.variables = initialValues;
        ctx.addAnyChild(variablesScope);
        return variablesScope;
    }

    /**
     * Attaches a new empty Variable Scope to the AST at {@code ctx}.
     *
     * @param ctx The AST node that represents the scope where the variables live
     */
    public static void addVariableScope(ParserRuleContext ctx){
        Token start = ctx.getStart();
        VariablesScope variablesScope = new VariablesScope(start);

        // Reset the variable scope again if its already there:
        for (int childIdx = 0; childIdx < ctx.getChildCount(); childIdx++) {
            ParseTree potentialVariableScope = ctx.children.get(childIdx);
            if (potentialVariableScope instanceof VariablesScope){


                ctx.children.set(childIdx,variablesScope);
                return;
            }
        }
        ctx.addAnyChild(variablesScope);
    }


    /**
     * Resolves the value of a variable.
     * @param ctx the point at which the variable is resolved.
     * @param identifier the variable name
     * @param exceptionBuilder needed to format errors
     * @return the value of the variable
     */
    public static Expr getVariable(ParserRuleContext ctx,String identifier,AstException.AstExceptionBuilder exceptionBuilder){
        VariablesScope variablesScope = resolveScope(ctx, (test ->test.variables.containsKey(identifier) ),"Variable "+identifier+" is not declared!", exceptionBuilder);
        return variablesScope.variables.get(identifier);
    }
    /**
     * Sets the value of a variable
     * @param ctx the point at which the variable is set.
     * @param identifier the variable name
     * @param expr the variable value
     * @param exceptionBuilder needed to format errors
     */
    public static void setVariable(ParserRuleContext ctx,String identifier,Expr expr,AstException.AstExceptionBuilder exceptionBuilder){
        VariablesScope variablesScope = resolveFirstScope(ctx, exceptionBuilder);
        variablesScope.variables.put(identifier,expr);
    }

    /**
     * Initializes a variable with a value.
     * @param ctx the point at which the variable is initialized.
     * @param variableName the variable name
     * @param initialValue the variable value
     * @param exceptionBuilder needed to format errors
     */
    public static void initializeVariable(ParserRuleContext ctx, String variableName, Expr initialValue, AstException.AstExceptionBuilder exceptionBuilder) {
        VariablesScope variablesScope = resolveFirstScope(ctx, exceptionBuilder);

        if (variablesScope.variables.containsKey(variableName)) {
            exceptionBuilder.inContext(ctx, "Variable "+variableName+" cannot be initialized twice!");
        }
        variablesScope.variables.put(variableName,initialValue);
    }

    /**
     * Sets or initializes the value of a variable.
     * @param ctx the point at which the variable is initialized.
     * @param variableName the variable name
     * @param initialValue the variable value
     * @param exceptionBuilder needed to format errors
     */
    public static void setOrinitializeVariable(ParserRuleContext ctx, String variableName, Expr initialValue, AstException.AstExceptionBuilder exceptionBuilder) {
        try {
            setVariable(ctx,variableName,initialValue,exceptionBuilder);
        }catch (AstException e) {
            initializeVariable(ctx,variableName,initialValue,exceptionBuilder);
        }
    }


    /**
     * Resolves the topmost Variable Scope
     * @param ctx where to resolve the scope from
     * @param exceptionBuilder needed to format errors
     * @return the topmost Variable Scope
     */
    public static VariablesScope resolveFirstScope(ParserRuleContext ctx, AstException.AstExceptionBuilder exceptionBuilder){
        return resolveScope(ctx,(x)->true,"No Variable scope found!",exceptionBuilder);
    }


    /**
     * Private helper method to generically find the right Variable Scope on of the parents.
     * @param ctx where to start the search for a Variable Scope
     * @param pred A predicate that must be true for the Variable Scope to be returned.
     * @param errMessage Customizable Error message when nothing is found.
     * @param exceptionBuilder needed to build the error to throw.
     * @return the desired Variable Scope
     */
    private static VariablesScope resolveScope(ParserRuleContext ctx, Predicate<VariablesScope> pred, String errMessage,AstException.AstExceptionBuilder exceptionBuilder){
        Set<String> allVars = new HashSet<>();
        int numScopes = 0;
        for (RuleContext parent = ctx;parent != null; parent = parent.parent){
            for (int child = 0; child < parent.getChildCount(); child++) {
                ParseTree potentialVariableScope = parent.getChild(child);
                if (potentialVariableScope instanceof VariablesScope){
                    VariablesScope variableScope = (VariablesScope) potentialVariableScope;

                    numScopes++;
                    allVars.addAll(variableScope.variables.keySet());

                    if(pred.test(variableScope)){
                        return variableScope;
                    }
                }
            }
        }
        throw exceptionBuilder.inContext(ctx,"Could not find an appropriate VariableScope: "+errMessage+"! \nThe "+numScopes+" scopes contain these variables:" + String.join(", ",allVars));
    }


}
