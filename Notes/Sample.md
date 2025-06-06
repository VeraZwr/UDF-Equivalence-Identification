

```java
package operators.product;

public class Mult01 {
    public static int main(int a, int b){
        int prd = a * b;
        return prd;
    }  
}
```

AST: 
```lisp
(start_ (compilationUnit (ordinaryCompilationUnit (packageDeclaration package operators . product ;) (topLevelClassOrInterfaceDeclaration (classDeclaration (normalClassDeclaration (classModifier public) class (typeIdentifier Mult01) (classBody { (classBodyDeclaration (classMemberDeclaration (methodDeclaration (methodModifier public) (methodModifier static) (methodHeader (result (unannType (unannPrimitiveType (numericType (integralType int))))) (methodDeclarator main ( (formalParameterList (formalParameter (unannType (unannPrimitiveType (numericType (integralType int)))) (variableDeclaratorId a)) , (formalParameter (unannType (unannPrimitiveType (numericType (integralType int)))) (variableDeclaratorId b))) ))) (methodBody (block { (blockStatements (blockStatement (localVariableDeclarationStatement (localVariableDeclaration (localVariableType (unannType (unannPrimitiveType (numericType (integralType int))))) (variableDeclaratorList (variableDeclarator (variableDeclaratorId prd) = (variableInitializer (expression (assignmentExpression (conditionalExpression (conditionalOrExpression (conditionalAndExpression (inclusiveOrExpression (exclusiveOrExpression (andExpression (equalityExpression (relationalExpression (shiftExpression (additiveExpression (multiplicativeExpression (multiplicativeExpression (unaryExpression (unaryExpressionNotPlusMinus (postfixExpression (expressionName a))))) * (unaryExpression (unaryExpressionNotPlusMinus (postfixExpression (expressionName b))))))))))))))))))))) ;)) (blockStatement (statement (statementWithoutTrailingSubstatement (returnStatement return (expression (assignmentExpression (conditionalExpression (conditionalOrExpression (conditionalAndExpression (inclusiveOrExpression (exclusiveOrExpression (andExpression (equalityExpression (relationalExpression (shiftExpression (additiveExpression (multiplicativeExpression (unaryExpression (unaryExpressionNotPlusMinus (postfixExpression (expressionName prd))))))))))))))))) ;))))) }))))) })))))) <EOF>)
```

Resulting Z3 Expr for "main" function:
Note: params are named in a numbered format p1, p2, etc.
```
(* p2 p1)
```


Short Example:

```java
int prd = a * b;
```

```lisp
(localVariableDeclaration 
    (localVariableType (unannType (unannPrimitiveType (numericType (integralType int))))) 
    (variableDeclaratorList (variableDeclarator 
        (variableDeclaratorId prd)
            = 
        (variableInitializer (expression (assignmentExpression (conditionalExpression (conditionalOrExpression (conditionalAndExpression (inclusiveOrExpression (exclusiveOrExpression (andExpression (equalityExpression (relationalExpression (shiftExpression (additiveExpression (multiplicativeExpression (multiplicativeExpression 
            (unaryExpression (unaryExpressionNotPlusMinus (postfixExpression (expressionName a))))) 
                * 
            (unaryExpression (unaryExpressionNotPlusMinus (postfixExpression (expressionName b)))))))))))))))))))))
```
Relevant ANTLR rules:

```antlrv4
multiplicativeExpression
    : unaryExpression
    | multiplicativeExpression '*' unaryExpression
    | multiplicativeExpression '/' unaryExpression
    | multiplicativeExpression '%' unaryExpression
    ;
```

Relevant Method in Remapper:

```java
@Override
    public void exitMultiplicativeExpression(Java20Parser.MultiplicativeExpressionContext ctx) {
        if (ctx.MOD() != null) {
            // Handle Modulo
            //[...]
        } else if (ctx.DIV() != null) {
            // Handle Division
            //[...]
        } else if (ctx.MUL() != null) {
            Expr<IntSort> firstOperand = getZ3Expr(ctx.multiplicativeExpression());
            Expr<IntSort> secondOperand = getZ3Expr(ctx.unaryExpression());

            ArithExpr<IntSort> arithExpr = context.mkMul(firstOperand, secondOperand);

            setZ3Expression(ctx, arithExpr);
        } else {
            propagateZ3Expression(ctx, ctx.unaryExpression());
        }
    }
```




