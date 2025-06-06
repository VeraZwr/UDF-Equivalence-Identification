Transforming the AST to Z3 Formulas is done in a bottom-up fashion.
We utilize the `Java20ParserBaseListener` class for Java AST transformation (For other languages, other BaseListeners would be used) which gives us methods for 'walking' the AST.
For each Grammar Rule there exists an `enterRule` and `exitRule` Method.
We utilize the exit methods to replace a Node in the Tree with its corresponding Z3 formula. 
When non-leaf nodes are processed like this, they can assume that the lower nodes have already been remapped to Z3 formula, allowing us to either propagate it or assemble it into the correct formula for this node.

The two main methods that help us do this in the Listener are therefore:
`private <S extends Sort> Expr<S> getZ3Expr(ParserRuleContext ctx)` and ` private <S extends Sort> void setZ3Expression(ParserRuleContext ctx,Expr<S> expr)` which respectively get and set the associated Z3 Expression to the Node in the tree.
Additionally there is `private <S extends Sort> void propagateZ3Expression(ParserRuleContext to, ParserRuleContext from)` which internally calls `setZ3Expression(to, getZ3Expr(from));`

# How to:
Given a Grammar rule:
```antlrv4
additiveExpression
    : multiplicativeExpression
    | additiveExpression '+' multiplicativeExpression
    | additiveExpression '-' multiplicativeExpression
    ;
```
We create if-else statements that go into each case **IN REVERSE** by checking **NON-AMBIGUOUS RULES**.
This is done in reverse as the framework gives first priority when matching to the first element, meaning that it can use the same Rules later without introducing ambiguity.
That is to say, if we were to do it in order, the first call to `ctx.multiplicativeExpression()` could mean the multiplicativeExpression in ANY of the rules.
Thus, we arrive at this if-else chain:
```java
if (ctx.SUB()!=null) {
    //Handling additiveExpression '-' multiplicativeExpression
} else if (ctx.ADD()!=null) {
    //Handling additiveExpression '+' multiplicativeExpression
    //[...]
} else {
    //Handling multiplicativeExpression
}
```

in the case of the multiplicativeExpression, we propagate it. But in the other cases, we get the Z3Formula from the children to create a new Expression, which we save as our expression. This results in this method:
```java
@Override
public void exitAdditiveExpression(Java20Parser.AdditiveExpressionContext ctx) {
    if (ctx.SUB()!=null) {
        Expr<ArithSort> firstOperand = getZ3Expr(ctx.additiveExpression());
        Expr<ArithSort> secondOperand = getZ3Expr(ctx.multiplicativeExpression());
        ArithExpr<ArithSort> arithExpr = context.mkSub(firstOperand, secondOperand);

        setZ3Expression(ctx,arithExpr);
    } else if (ctx.ADD()!=null) {
        Expr<ArithSort> firstOperand = getZ3Expr(ctx.additiveExpression());
        Expr<ArithSort> secondOperand = getZ3Expr(ctx.multiplicativeExpression());

        ArithExpr<ArithSort> arithExpr = context.mkAdd(firstOperand, secondOperand);

        setZ3Expression(ctx,arithExpr);
    } else {
        propagateZ3Expression(ctx,ctx.multiplicativeExpression());
    }
}
```

## How Variables are kept Track of
In code, variables are scoped to their current block.
We utilize an abstraction that allows similar behavior. 
We attach `VariablesScope` to AST nodes. 
The static methods on `VariablesScope` then walk the AST when figuring out what value to assign to a variable.
When entering blocks (or other language constructs that could open up a new Scope) we attach a new Scope to the corresponding AST node like this: `VariablesScope.addVariableScope(ctx.block());`.
Then expressions for variables can be read or written using:  `Expr previousValue = VariablesScope.getVariable(ctx, varName, exceptionBuilder);` and `VariablesScope.setVariable(ctx,varName,resultingValue,exceptionBuilder);`




## How Methods are kept track of
Similar to how we attach `VariableScopes` to the AST when there could be a new Scope for variables we attach a `ClassContext` to the entire class.
Methods and their resulting expressions are registerred against the `ClassContext` and allow later retrieval.


## Notes
- You can use IntelliJ to provide you a stub implementation of one of the methods `Right Click -> Generate -> Overrides` or pressing `Ctrl+O` directly.
- try to keep the ordering of the exit methods the same as in the grammar.
- error handling is done using the `exceptionBuilder` field. This has a few methods to create ASTExceptions with nice error messages that point to the current location:
```java
@Override
public void enterImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
    throw exceptionBuilder.inContext(ctx,"Import declaration not supported in UDFs");
}
```
- some z3 expressions require Ints and some BitVecs. These can be transformed using `context.mkInt2BV(32,intExpr)` and `context.mkBV2Int(bvExpr,true)`

