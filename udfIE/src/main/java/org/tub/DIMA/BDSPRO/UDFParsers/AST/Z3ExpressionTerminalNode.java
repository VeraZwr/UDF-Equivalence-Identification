package org.tub.DIMA.BDSPRO.UDFParsers.AST;

import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.function.Supplier;


/**
 * This is a Node that can be attached to a {@link org.antlr.v4.runtime.tree.ParseTree} in order to store a {@link Expr} directly inside the AST.
 * It also supports storing a {@code Supplier<Expr<S>>} to store an expression lazyly and only compute them when needed,
 * which can reduce errors as "faulty" lazy evaluations might not be necessary and therefore dont cause errors.
 *
 * This Class is not interacted with directly but rather through the methods of {@link AstUtils}.
 */
class Z3ExpressionTerminalNode<S extends Sort> extends TerminalNodeImpl{
    private boolean resolved;
    private  Expr<S> expr;
    private  Supplier<Expr<S>> unresolved_expression;


    public Z3ExpressionTerminalNode(Token symbol, Expr<S> expr) {
        super(symbol);
        this.expr = expr;
        resolved = true;
    }

    public Z3ExpressionTerminalNode(Token symbol, Supplier<Expr<S>> unresolved_expression) {
        super(symbol);
        this.unresolved_expression = unresolved_expression;
        resolved = false;

    }

    public Supplier<Expr<S>> getUnresolvedExpr(){
        if (resolved) {
            return ()->expr;
        } else {
            return unresolved_expression;
        }
    }

    public Expr<S> resolve(){
        if (!resolved) {
            expr = unresolved_expression.get();
            unresolved_expression = null;
            resolved = true;
        }
        return expr;
    }
}