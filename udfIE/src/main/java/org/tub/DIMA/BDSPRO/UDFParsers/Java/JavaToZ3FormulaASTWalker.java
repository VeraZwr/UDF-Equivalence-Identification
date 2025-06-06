package org.tub.DIMA.BDSPRO.UDFParsers.Java;

import com.microsoft.z3.*;
import org.antlr.v4.grammars.Java20Parser;
import org.antlr.v4.grammars.Java20ParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.AstException;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.AstUtils;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.ClassContext;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.VariablesScope;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;


/**
 * Remaps a ParseTree of a JavaUDF to a Z3 Expression
 */
public class JavaToZ3FormulaASTWalker extends Java20ParserBaseListener {
    private final AstException.AstExceptionBuilder exceptionBuilder;
    private final Context context;


    public ClassContext classContext;

    private Supplier<Expr> getZ3Expr(ParserRuleContext ctx) {
        return AstUtils.getZ3Expr(exceptionBuilder,ctx);
    }

    private void propagateZ3Expression(ParserRuleContext to, ParserRuleContext from) {
        AstUtils.propagateZ3Expression(exceptionBuilder,to,from);
    }

    private void setZ3Expression(ParserRuleContext ctx, Expr expr) {
        AstUtils.setZ3Expression(ctx,expr);
    }

    private void setZ3Expression(ParserRuleContext ctx, Supplier<Expr> exprSupplier) {
        AstUtils.setZ3Expression(ctx,exprSupplier);
    }

    private Supplier<BitVecExpr> getZ3BVExpr(ParserRuleContext ctx) {
       return AstUtils.getZ3BVExpr(context,exceptionBuilder,ctx);
    }

    private Supplier<IntExpr> getZ3IntExpr(ParserRuleContext ctx) {
        return AstUtils.getZ3IntExpr(context,exceptionBuilder,ctx);
    }

    private void exitBlockHelper(ParserRuleContext ctx,Expr<BoolSort> condition, VariablesScope changedVariables){
        AstUtils.exitBlockHelper(context,exceptionBuilder,ctx,condition,changedVariables);
    }


    public JavaToZ3FormulaASTWalker(Java20Parser parser, UDF udf
            , Context context) {
        this.context = context;
        exceptionBuilder = new AstException.AstExceptionBuilder(udf,parser);
    }

    @Override
    public void enterImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Import declaration not supported in UDFs");
    }

    @Override
    public void enterUnannReferenceType(Java20Parser.UnannReferenceTypeContext ctx) {
        throw exceptionBuilder.inContext(ctx,"UDFs must use only primitive types");
    }

    @Override
    public void enterMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        throw exceptionBuilder.inContext(ctx,"UDFs should not use method calls.");
    }

    @Override
    public void enterAnnotation(Java20Parser.AnnotationContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Annotations are not supported in UDFs");
    }

    @Override
    public void enterLambdaExpression(Java20Parser.LambdaExpressionContext ctx) {
        throw exceptionBuilder.inContext(ctx,"UDFs should not use lambdas.");
    }

    @Override
    public void enterVariableArityParameter(Java20Parser.VariableArityParameterContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Variable Arity Parameters are not Supported!");
    }

    @Override
    public void enterForStatement(Java20Parser.ForStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"For Loops are not supported!");
    }

    @Override
    public void enterForStatementNoShortIf(Java20Parser.ForStatementNoShortIfContext ctx) {
        throw exceptionBuilder.inContext(ctx,"For Loops are not supported!");
    }

    @Override
    public void enterWhileStatement(Java20Parser.WhileStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"While Loops are not supported!");
    }

    @Override
    public void enterWhileStatementNoShortIf(Java20Parser.WhileStatementNoShortIfContext ctx) {
        throw exceptionBuilder.inContext(ctx,"While Loops are not supported!");
    }

    @Override
    public void enterLabeledStatement(Java20Parser.LabeledStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Labelled Statements are not supported!");
    }

    @Override
    public void enterLabeledStatementNoShortIf(Java20Parser.LabeledStatementNoShortIfContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Labelled Statements are not supported!");
    }

    @Override
    public void enterThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Throw Statements are not supported!");
    }

    @Override
    public void enterAssertStatement(Java20Parser.AssertStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Assert Statements are not supported!");
    }

    @Override
    public void enterDoStatement(Java20Parser.DoStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Do Statements are not supported!");
    }

    @Override
    public void enterBreakStatement(Java20Parser.BreakStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Break Statements are not supported!");
    }

    @Override
    public void enterContinueStatement(Java20Parser.ContinueStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Continue Statements are not supported!");
    }

    @Override
    public void enterSynchronizedStatement(Java20Parser.SynchronizedStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Synchronized Statements are not supported!");
    }

    @Override
    public void enterTryStatement(Java20Parser.TryStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Try Statements are not supported!");
    }

    @Override
    public void enterYieldStatement(Java20Parser.YieldStatementContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Yield Statements are not supported!");
    }

    @Override
    public void enterNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        if (classContext != null) {
            throw exceptionBuilder.inContext(ctx,"There was already a class registerred for this UDF");
        }

        classContext = ClassContext.attachClass(ctx,exceptionBuilder);
    }

    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        Java20Parser.MethodDeclaratorContext methodDeclarator = ctx.methodHeader().methodDeclarator();

        String methodName = methodDeclarator.Identifier().getText();

        if (methodDeclarator.receiverParameter()!=null){
            throw exceptionBuilder.inContext(ctx,"Receiver Parameter not supported.");
        }

        Java20Parser.FormalParameterListContext parameterList = methodDeclarator.formalParameterList();
        if (parameterList!=null){

            HashMap<String, Expr> params = new HashMap<>();
            int parameter = 0;
            for (Java20Parser.FormalParameterContext param : parameterList.formalParameter()) {
                try {
                    param.unannType().unannPrimitiveType().numericType().integralType().INT().getText();
                }catch (NullPointerException e){
                    throw exceptionBuilder.inContext(ctx,"For now, only int is supported!");
                }

                String paramName = param.variableDeclaratorId().Identifier().getText();

                parameter++;

                Expr paramExpr = context.mkIntConst("p"+(parameter));
                params.put(paramName,paramExpr);
            }
            VariablesScope.addVariableScope(ctx,params);
        }
    }

    @Override
    public void exitMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {

        Supplier<Expr> z3Expr = getZ3Expr(ctx.methodBody());
        ClassContext classContext = ClassContext.resolveClass(ctx, exceptionBuilder);
        String methodName = ctx.methodHeader().methodDeclarator().Identifier().getText();

        classContext.declareMethod(methodName,z3Expr.get());

    }


    @Override
    public void enterIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        VariablesScope.addVariableScope(ctx.statement());
    }

    @Override
    public void enterIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        VariablesScope.addVariableScope(ctx.statement());
        VariablesScope.addVariableScope(ctx.statementNoShortIf());
    }

    @Override
    public void enterIfThenElseStatementNoShortIf(Java20Parser.IfThenElseStatementNoShortIfContext ctx) {
        VariablesScope.addVariableScope(ctx.statementNoShortIf(0));
        VariablesScope.addVariableScope(ctx.statementNoShortIf(1));
    }

    @Override
    public void exitIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        Expr condition = getZ3Expr(ctx.expression()).get();
        VariablesScope changedVariablesThen = VariablesScope.resolveFirstScope(ctx.statement(), exceptionBuilder);
        exitBlockHelper(ctx,condition,changedVariablesThen);

    }

    @Override
    public void exitIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        Expr condition = getZ3Expr(ctx.expression()).get();
        VariablesScope changedVariablesThen = VariablesScope.resolveFirstScope(ctx.statementNoShortIf(), exceptionBuilder);
        VariablesScope changedVariablesElse = VariablesScope.resolveFirstScope(ctx.statement(), exceptionBuilder);
        exitBlockHelper(ctx,condition,changedVariablesThen);
        exitBlockHelper(ctx,context.mkNot(condition),changedVariablesElse);
    }

    @Override
    public void exitIfThenElseStatementNoShortIf(Java20Parser.IfThenElseStatementNoShortIfContext ctx) {
        Expr condition = getZ3Expr(ctx.expression()).get();
        VariablesScope changedVariablesThen = VariablesScope.resolveFirstScope(ctx.statementNoShortIf(0), exceptionBuilder);
        VariablesScope changedVariablesElse = VariablesScope.resolveFirstScope(ctx.statementNoShortIf(1), exceptionBuilder);
        exitBlockHelper(ctx,condition,changedVariablesThen);
        exitBlockHelper(ctx,context.mkNot(condition),changedVariablesElse);
    }








    @Override
    public void exitMethodBody(Java20Parser.MethodBodyContext ctx) {
        propagateZ3Expression(ctx,ctx.block());
    }

    @Override
    public void exitBlock(Java20Parser.BlockContext ctx) {
        if (ctx.blockStatements()==null){
            throw exceptionBuilder.inContext(ctx,"Empty method blocks are not supported");
        }

        Java20Parser.BlockStatementContext lastStatement = ctx.blockStatements().blockStatement().getLast();

        try {
            propagateZ3Expression(ctx,lastStatement.statement().statementWithoutTrailingSubstatement().returnStatement());
        } catch (NullPointerException e) {

        }
    }

    @Override
    public void exitReturnStatement(Java20Parser.ReturnStatementContext ctx) {
        propagateZ3Expression(ctx,ctx.expression());
    }


    @Override
    public void exitBlockStatement(Java20Parser.BlockStatementContext ctx) {
        if (ctx.localClassOrInterfaceDeclaration()!=null){
            throw exceptionBuilder.inContext(ctx,"Local Classes or Interfaces are not supported");
        }
    }


    @Override
    public void exitLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        if (ctx.variableDeclaratorList()==null){
            throw exceptionBuilder.inContext(ctx,"Uninitialized Variables are not supported. \n(This might also be a parser error related to variables named var not being parsed correctly)");
        }

        for (Java20Parser.VariableDeclaratorContext variableDeclarator : ctx.variableDeclaratorList().variableDeclarator()) {
            String variableName = variableDeclarator.variableDeclaratorId().Identifier().getText();

            Java20Parser.VariableInitializerContext intializer = variableDeclarator.variableInitializer();
            if (intializer!=null) {
                Expr initialValue = getZ3Expr(intializer).get();
                VariablesScope.initializeVariable(ctx,variableName,initialValue,exceptionBuilder);
            }
        }
    }

    @Override
    public void exitVariableInitializer(Java20Parser.VariableInitializerContext ctx) {
        if (ctx.arrayInitializer()!=null){
            throw exceptionBuilder.inContext(ctx,"Array Initailizers are not supported!");
        } else {
            propagateZ3Expression(ctx,ctx.expression());
        }
    }



    @Override
    public void exitExpression(Java20Parser.ExpressionContext ctx) {
        assert ctx.lambdaExpression() == null;
        propagateZ3Expression(ctx,ctx.assignmentExpression());
    }

    @Override
    public void exitPrimary(Java20Parser.PrimaryContext ctx) {
        if (ctx.arrayCreationExpression()!=null){
            propagateZ3Expression(ctx,ctx.arrayCreationExpression());
        } else {
            propagateZ3Expression(ctx,ctx.primaryNoNewArray());
        }
    }

    @Override
    public void exitPrimaryNoNewArray(Java20Parser.PrimaryNoNewArrayContext ctx) {
        boolean hasColonColon = ctx.COLONCOLON() != null;
        boolean hasSuper = ctx.SUPER() != null;

        if (ctx.arrayType() != null && hasColonColon && ctx.NEW() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.classType() != null && hasColonColon && ctx.NEW() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.typeName() != null && ctx.DOT() != null && hasSuper && hasColonColon && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (hasSuper && hasColonColon && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.referenceType() != null && hasColonColon && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.arrayCreationExpression() != null && hasColonColon && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.expressionName() != null && hasColonColon && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.typeName() != null && ctx.DOT() != null && hasSuper && ctx.DOT() != null && ctx.typeArguments() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (hasSuper && ctx.DOT() != null && ctx.typeArguments() != null && ctx.Identifier() != null && ctx.LPAREN() != null && ctx.RPAREN() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.arrayCreationExpression() != null && ctx.DOT() != null && ctx.typeArguments() != null && ctx.Identifier() != null && ctx.LPAREN() != null && ctx.RPAREN() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.expressionName() != null && ctx.DOT() != null && ctx.typeArguments() != null && ctx.Identifier() != null && ctx.LPAREN() != null && ctx.RPAREN() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.typeName() != null && ctx.DOT() != null && ctx.typeArguments() != null && ctx.Identifier() != null && ctx.LPAREN() != null && ctx.RPAREN() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.methodName() != null && ctx.LPAREN() != null && ctx.RPAREN() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.arrayCreationExpressionWithInitializer() != null && ctx.LBRACK() != null && ctx.expression() != null && ctx.RBRACK() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.expressionName() != null && ctx.LBRACK() != null && ctx.expression() != null && ctx.RBRACK() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.typeName() != null && ctx.DOT() != null && hasSuper && ctx.DOT() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (hasSuper && ctx.DOT() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.arrayCreationExpression() != null && ctx.DOT() != null && ctx.Identifier() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.arrayCreationExpression() != null && ctx.DOT() != null && ctx.unqualifiedClassInstanceCreationExpression() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.expressionName() != null && ctx.DOT() != null && ctx.unqualifiedClassInstanceCreationExpression() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.unqualifiedClassInstanceCreationExpression() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.LPAREN() != null && ctx.expression() != null && ctx.RPAREN() != null) {
            propagateZ3Expression(ctx,ctx.expression());
        } else if (ctx.typeName() != null && ctx.DOT() != null && ctx.THIS() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.THIS() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.classLiteral() != null) {
            throw exceptionBuilder.inContext(ctx, "Not supported.");
        } else if (ctx.literal() != null) {
            propagateZ3Expression(ctx,ctx.literal());
        }
    }

    @Override
    public void exitLiteral(Java20Parser.LiteralContext ctx) {
        String literal = ctx.getChild(0).getText();

        if (ctx.IntegerLiteral()!=null) {
            int i = Integer.parseInt(literal);
            setZ3Expression(ctx,context.mkInt(i));
        } else {
            throw exceptionBuilder.inContext(ctx, "Only Integer literals are supported.");
        }
    }



    @Override
    public void exitShiftExpression(Java20Parser.ShiftExpressionContext ctx) {
        if (ctx.GT(2)!=null){
            throw exceptionBuilder.inContext(ctx,"unsigned right-bit shift operator is not Supported!");
        } else if (ctx.GT(0)!=null) {
            Supplier<Expr> shiftExpr = getZ3Expr(ctx.shiftExpression());
            Supplier<Expr> additiveExpr = getZ3Expr(ctx.additiveExpression());

            Supplier<Expr> bitVecExpr = ()->context.mkBVLSHR(shiftExpr.get(), additiveExpr.get());
            setZ3Expression(ctx,bitVecExpr);
        } else if (ctx.LT(0)!=null){
            Supplier<Expr> shiftExpr = getZ3Expr(ctx.shiftExpression());
            Supplier<Expr> additiveExpr = getZ3Expr(ctx.additiveExpression());
            Supplier<Expr> bitVecExpr = ()->context.mkBVSHL(shiftExpr.get(), additiveExpr.get());
            setZ3Expression(ctx,bitVecExpr);
        } else {
            propagateZ3Expression(ctx,ctx.additiveExpression());
        }
    }

    @Override
    public void exitRelationalExpression(Java20Parser.RelationalExpressionContext ctx) {
        if (ctx.INSTANCEOF()!=null) {
            throw exceptionBuilder.inContext(ctx,"instanceOf Expressions are not supported!");
        }

        Supplier<Expr> shiftExpr = getZ3Expr(ctx.shiftExpression());

        if (ctx.relationalExpression()!=null) {
            Supplier<Expr> relationalExpr = getZ3Expr(ctx.relationalExpression());
            if (ctx.GE()!=null) {
                setZ3Expression(ctx,()->context.mkGe(relationalExpr.get(), shiftExpr.get()));
            } else if (ctx.LE()!=null) {
                setZ3Expression(ctx,()->context.mkLe(relationalExpr.get(), shiftExpr.get()));
            } else if (ctx.GT()!=null) {
                setZ3Expression(ctx, ()->context.mkGt(relationalExpr.get(), shiftExpr.get()));
            } else if (ctx.LT()!=null) {
                setZ3Expression(ctx, ()->context.mkLt(relationalExpr.get(), shiftExpr.get()));
            } else {
                throw exceptionBuilder.inContext(ctx,"Unreacheable Case!");
            }
        }
         else {
            propagateZ3Expression(ctx,ctx.shiftExpression());
        }


    }

    @Override
    public void exitEqualityExpression(Java20Parser.EqualityExpressionContext ctx) {
        Supplier<Expr> expr = () -> context.mkEq(getZ3Expr(ctx.equalityExpression()).get(), getZ3Expr(ctx.relationalExpression()).get());

        if (ctx.NOTEQUAL()!=null) {
            setZ3Expression(ctx,()->context.mkNot(expr.get()));
        } else if (ctx.EQUAL()!=null) {
            setZ3Expression(ctx,expr);
        } else {
            propagateZ3Expression(ctx,ctx.relationalExpression());
        }
    }

    @Override
    public void exitAndExpression(Java20Parser.AndExpressionContext ctx) {
        if (ctx.BITAND()!=null){
            Supplier<BitVecExpr> andExpr = getZ3BVExpr(ctx.andExpression());
            Supplier<BitVecExpr> equalityExpr = getZ3BVExpr(ctx.equalityExpression());


            setZ3Expression(ctx,()->context.mkBVAND(andExpr.get(), equalityExpr.get()));
        } else {
            propagateZ3Expression(ctx,ctx.equalityExpression());
        }
    }

    @Override
    public void exitExclusiveOrExpression(Java20Parser.ExclusiveOrExpressionContext ctx) {
        if (ctx.CARET()!=null){
            setZ3Expression(ctx,()->context.mkBVXOR(getZ3BVExpr(ctx.exclusiveOrExpression()).get(), getZ3BVExpr(ctx.andExpression()).get()));
        } else {
            propagateZ3Expression(ctx,ctx.andExpression());
        }
    }

    @Override
    public void exitInclusiveOrExpression(Java20Parser.InclusiveOrExpressionContext ctx) {
        if (ctx.BITOR()!=null){
            Supplier<BitVecExpr> exclusiveOrExpr = getZ3BVExpr(ctx.exclusiveOrExpression());
            Supplier<BitVecExpr> inclusiveOrExpression = getZ3BVExpr(ctx.inclusiveOrExpression());
            Expr boolExpr = context.mkBVOR(inclusiveOrExpression.get(), exclusiveOrExpr.get());
            setZ3Expression(ctx,boolExpr);
        } else {
            propagateZ3Expression(ctx,ctx.exclusiveOrExpression());
        }
    }

    @Override
    public void exitConditionalAndExpression(Java20Parser.ConditionalAndExpressionContext ctx) {
        if (ctx.AND()!=null){
            setZ3Expression(ctx,()->context.mkAnd(getZ3Expr(ctx.conditionalAndExpression()).get(), getZ3Expr(ctx.inclusiveOrExpression()).get()));
        } else {
            propagateZ3Expression(ctx,ctx.inclusiveOrExpression());
        }
    }

    @Override
    public void exitConditionalOrExpression(Java20Parser.ConditionalOrExpressionContext ctx) {
        if (ctx.OR()!=null){
            setZ3Expression(ctx,()->context.mkOr(getZ3Expr(ctx.conditionalOrExpression()).get(), getZ3Expr(ctx.conditionalAndExpression()).get()));
        } else {
            propagateZ3Expression(ctx,ctx.conditionalAndExpression());
        }
    }

    @Override
    public void exitConditionalExpression(Java20Parser.ConditionalExpressionContext ctx) {
        if (ctx.lambdaExpression()!=null) {
            throw exceptionBuilder.inContext(ctx,"Lambda expressions are not supported!");
        }

        if (ctx.conditionalExpression() != null) {
            Supplier<Expr> condition = getZ3Expr(ctx.conditionalOrExpression());
            Supplier<Expr> expr = getZ3Expr(ctx.expression());
            Supplier<Expr> conditionalExpr = getZ3Expr(ctx.conditionalExpression());
            Supplier<Expr> resultingExpr = () -> context.mkITE(condition.get(),expr.get() , conditionalExpr.get());
            setZ3Expression(ctx,resultingExpr);
        } else {
            propagateZ3Expression(ctx,ctx.conditionalOrExpression());
        }
    }

    @Override
    public void exitAssignmentExpression(Java20Parser.AssignmentExpressionContext ctx) {
        if (ctx.conditionalExpression()!=null) {
            propagateZ3Expression(ctx,ctx.conditionalExpression());
        } else {
            propagateZ3Expression(ctx,ctx.assignment());
        }
    }


    @Override
    public void exitAssignment(Java20Parser.AssignmentContext ctx) {
        if (ctx.leftHandSide().fieldAccess()!=null ){
            throw exceptionBuilder.inContext(ctx.leftHandSide().fieldAccess(),"Field Access not supported in UDF");
        } else if (ctx.leftHandSide().arrayAccess()!=null) {
            throw exceptionBuilder.inContext(ctx.leftHandSide().arrayAccess(),"Array Access not supported in UDF");
        }
        String variableName = ctx.leftHandSide().expressionName().Identifier().getText();
        Expr value = getZ3Expr(ctx.expression()).get();

        Java20Parser.AssignmentOperatorContext op = ctx.assignmentOperator();
        Expr newValue;

        if (op.ASSIGN()!=null) {
            newValue = value;
        } else {
            Expr oldValue = VariablesScope.getVariable(ctx, variableName, exceptionBuilder);
            if(op.OR_ASSIGN()!=null) {
                newValue = context.mkOr(oldValue,value);
            } else if (op.XOR_ASSIGN()!=null) {
                newValue = context.mkBVXOR(oldValue,value);
            }else if (op.AND_ASSIGN()!=null) {
                newValue = context.mkAnd(oldValue,value);
            }else if (op.URSHIFT_ASSIGN()!=null) {
                throw exceptionBuilder.inContext(ctx.assignmentOperator(),">>>= is not Supported!");
            }else if (op.RSHIFT_ASSIGN()!=null) {
                throw exceptionBuilder.inContext(ctx.assignmentOperator(),">>= is not Supported!");
            }else if (op.LSHIFT_ASSIGN()!=null) {
                throw exceptionBuilder.inContext(ctx.assignmentOperator(),"<<= is not Supported!");
            }else if (op.SUB_ASSIGN()!=null) {
                newValue = context.mkSub(oldValue,value);
            }else if (op.ADD_ASSIGN()!=null) {
                newValue = context.mkAdd(oldValue,value);
            }else if (op.MOD_ASSIGN()!=null) {
                newValue = context.mkMod(oldValue,value);
            }else if (op.DIV_ASSIGN()!=null) {
                newValue = context.mkDiv(oldValue,value);
            }else if (op.MUL_ASSIGN()!=null) {
                newValue = context.mkMul(oldValue,value);
            }else {
                throw exceptionBuilder.inContext(ctx.assignmentOperator(),"Unrecognized Assignment Operator");
            }
        }
        VariablesScope.setVariable(ctx, variableName, newValue,exceptionBuilder);
        setZ3Expression(ctx,newValue);
    }

    @Override
    public void exitPostfixExpression(Java20Parser.PostfixExpressionContext ctx) {
        if (ctx.pfE() != null) {
            throw exceptionBuilder.inContext(ctx,"Postfix Expression");
        }

        if (ctx.expressionName() != null) {
            propagateZ3Expression(ctx, ctx.expressionName());
        } else if (ctx.primary() != null) {
            propagateZ3Expression(ctx, ctx.primary());
        }
    }

    @Override
    public void exitUnaryExpression(Java20Parser.UnaryExpressionContext ctx) {
        if (ctx.unaryExpressionNotPlusMinus() != null) {
            propagateZ3Expression(ctx, ctx.unaryExpressionNotPlusMinus());
        } else if (ctx.SUB() != null) {
            //TODO
            throw exceptionBuilder.inContext(ctx,"Negation is not supported as of now.");
//            Expr<Sort> z3Expr = getZ3Expr(ctx.unaryExpression());
//            ArithExpr<R> rArithExpr = context.mkMul(intNum, z3Expr);
        } else if (ctx.ADD() != null) {
            propagateZ3Expression(ctx, ctx.unaryExpression());
        } else if (ctx.preDecrementExpression() != null) {
            propagateZ3Expression(ctx, ctx.preDecrementExpression());
        } else {
            propagateZ3Expression(ctx, ctx.preIncrementExpression());

        }
    }


    @Override
    public void exitUnaryExpressionNotPlusMinus(Java20Parser.UnaryExpressionNotPlusMinusContext ctx) {
        if (ctx.switchExpression() != null) {
            propagateZ3Expression(ctx, ctx.switchExpression());
        } else if (ctx.castExpression() != null) {
            propagateZ3Expression(ctx, ctx.castExpression());
        } else if (ctx.BANG() != null) {
            Supplier<Expr> operand = getZ3Expr(ctx.unaryExpression());
            Supplier<Expr> notExpr = () -> context.mkNot(operand.get());

            setZ3Expression(ctx, notExpr);
        } else if (ctx.TILDE() != null) {
            Supplier<Expr> operand = getZ3Expr(ctx.unaryExpression());
            Supplier<Expr> notExpr = () -> context.mkBVNot(operand.get());

            setZ3Expression(ctx, notExpr);
        } else {
            propagateZ3Expression(ctx, ctx.postfixExpression());
        }
    }


    @Override
    public void exitMultiplicativeExpression(Java20Parser.MultiplicativeExpressionContext ctx) {
        if (ctx.MOD() != null) {
            Supplier<Expr> firstOperand = getZ3Expr(ctx.multiplicativeExpression());
            Supplier<Expr> secondOperand = getZ3Expr(ctx.unaryExpression());

            Supplier<Expr> arithExpr = () -> context.mkMod(firstOperand.get(), secondOperand.get());

            setZ3Expression(ctx, arithExpr);
        } else if (ctx.DIV() != null) {
            Supplier<Expr> firstOperand = getZ3Expr(ctx.multiplicativeExpression());
            Supplier<Expr> secondOperand = getZ3Expr(ctx.unaryExpression());

            Supplier<Expr> arithExpr = () -> context.mkDiv(firstOperand.get(), secondOperand.get());

            setZ3Expression(ctx, arithExpr);
        } else if (ctx.MUL() != null) {
            Supplier<Expr> firstOperand = getZ3Expr(ctx.multiplicativeExpression());
            Supplier<Expr> secondOperand = getZ3Expr(ctx.unaryExpression());

            Supplier<Expr> arithExpr = () -> context.mkMul(firstOperand.get(), secondOperand.get());

            setZ3Expression(ctx, arithExpr);
        } else {
            propagateZ3Expression(ctx, ctx.unaryExpression());
        }
    }

    @Override
    public void exitAdditiveExpression(Java20Parser.AdditiveExpressionContext ctx) {
        if (ctx.SUB() != null) {
            Supplier<Expr> firstOperand = getZ3Expr(ctx.additiveExpression());
            Supplier<Expr> secondOperand = getZ3Expr(ctx.multiplicativeExpression());
            Supplier<Expr> arithExpr = () -> context.mkSub(firstOperand.get(), secondOperand.get());
            setZ3Expression(ctx, arithExpr);
        } else if (ctx.ADD() != null) {
            Supplier<Expr> firstOperand = getZ3Expr(ctx.additiveExpression());
            Supplier<Expr> secondOperand = getZ3Expr(ctx.multiplicativeExpression());
            Supplier<Expr> arithExpr = () -> context.mkAdd(firstOperand.get(), secondOperand.get());
            setZ3Expression(ctx, arithExpr);
        } else {
            propagateZ3Expression(ctx, ctx.multiplicativeExpression());
        }
    }

    @Override
    public void exitExpressionName(Java20Parser.ExpressionNameContext ctx) {
        if (ctx.ambiguousName() != null) {
            throw exceptionBuilder.inContext(ctx,"ambiguous Name not supported in UDF");
        }

        try {
            Expr variable = VariablesScope.getVariable(ctx, ctx.Identifier().getText(),exceptionBuilder);
            setZ3Expression(ctx,variable);
        } catch (AstException e){}
    }

    @Override
    public void enterSwitchExpression(Java20Parser.SwitchExpressionContext ctx) {
        if (ctx.switchBlock().switchBlockStatementGroup(0)!=null){
            throw exceptionBuilder.inContext(ctx.switchBlock().switchBlockStatementGroup(0),"A switch expression cannot use switchBlockStatements!");
        }
    }

    @Override
    public void enterSwitchRule(Java20Parser.SwitchRuleContext ctx) {
        if (ctx.expression() != null){
            VariablesScope.addVariableScope(ctx.expression());
        } else if (ctx.block()!=null) {
            VariablesScope.addVariableScope(ctx.block());
        }
    }

    @Override
    public void exitSwitchExpression(Java20Parser.SwitchExpressionContext ctx) {
        Expr switchExpr = getZ3Expr(ctx.expression()).get();
        //TODO check why switchstatement/Java01.java returns the wrong expr.
        if (ctx.switchBlock().switchBlockStatementGroup(0)!=null){
            throw exceptionBuilder.inContext(ctx.switchBlock().switchBlockStatementGroup(0),"A switch expression cannot use switchBlockStatements!");
        }

        propagateVariablesFromSwitchStatements(ctx, switchExpr, ctx.switchBlock().switchRule());


        Supplier<Expr> resultingExpr = null;
        // We reverse it so that we can construct the expr iteratively
        for (Java20Parser.SwitchRuleContext switchRule : ctx.switchBlock().switchRule().reversed()) {
            Expr caseExpr = null;
            // Construct the expression which case to take.
            if (switchRule.switchLabel().CASE()!=null) {
                for (Java20Parser.CaseConstantContext caseConstantContext : switchRule.switchLabel().caseConstant()) {
                    Supplier<Expr> constExpr = getZ3Expr(caseConstantContext.conditionalExpression());
                    BoolExpr caseConstantExpr = context.mkEq(switchExpr, constExpr.get());
                    //TODO i have a feeling this does not work correctly
                    if (caseExpr == null) {
                        caseExpr = caseConstantExpr;
                    } else {
                        caseExpr = context.mkOr(caseExpr,caseConstantExpr);
                    }
                }
            } else {
                assert switchRule.switchLabel().DEFAULT()!=null;
                caseExpr = context.mkTrue();
            }

            Supplier<Expr> valueExpr;
            if (switchRule.block() != null) {
                valueExpr = getZ3Expr(switchRule.block());
            } else if (switchRule.expression() != null) {
                valueExpr = getZ3Expr(switchRule.expression());
            } else {
                throw exceptionBuilder.inContext(switchRule.throwStatement(),"Throw statements not supported!");
            }


            if (resultingExpr==null) {
                if (switchRule.switchLabel().DEFAULT()==null) {
                    throw exceptionBuilder.inContext(switchRule.switchLabel(),"The last switchLabel must be a default!");
                }
                resultingExpr = valueExpr;
            } else {
                Expr finalCaseExpr = caseExpr;
                Supplier<Expr> finalResultingExpr = resultingExpr;
                resultingExpr = () -> context.mkITE(finalCaseExpr,valueExpr.get(), finalResultingExpr.get());
            }

        }

        setZ3Expression(ctx,resultingExpr);
    }

    @Override
    public void exitSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        //TODO check why switchstatement/Java01.java returns the wrong expr.
        if (ctx.switchBlock().switchBlockStatementGroup(0)!=null){
            throw exceptionBuilder.inContext(ctx.switchBlock().switchBlockStatementGroup(0),"A switch expression cannot use switchBlockStatements!");
        }

        Expr switchExpr = getZ3Expr(ctx.expression()).get();
        propagateVariablesFromSwitchStatements(ctx,switchExpr,ctx.switchBlock().switchRule());
    }

    private void propagateVariablesFromSwitchStatements(ParserRuleContext ctx, Expr switchExpr,List<Java20Parser.SwitchRuleContext> switchRuleContexts) {
        BoolExpr conditionsUntilNow = null;
        // We must propagate assigned variables from the blocks:
        for (Java20Parser.SwitchRuleContext switchRule : switchRuleContexts){
            // Construct the expression which case to take.
            BoolExpr caseExpr = null;
            if (switchRule.switchLabel().CASE()!=null) {
                for (Java20Parser.CaseConstantContext caseConstantContext : switchRule.switchLabel().caseConstant()) {
                    Supplier<Expr> constExpr = getZ3Expr(caseConstantContext.conditionalExpression());
                    BoolExpr caseConstantExpr = context.mkEq(switchExpr, constExpr.get());

                    if (caseExpr == null) {
                        caseExpr = caseConstantExpr;
                    } else {
                        caseExpr = context.mkOr(caseExpr,caseConstantExpr);
                    }
                }
            } else {
                assert switchRule.switchLabel().DEFAULT()!=null;
                caseExpr = context.mkTrue();
            }


            BoolExpr thisExprIsExecuted;
            if (conditionsUntilNow ==null) {
                conditionsUntilNow = caseExpr;
                thisExprIsExecuted = caseExpr;
            }else {
                thisExprIsExecuted = context.mkAnd(context.mkNot(conditionsUntilNow), caseExpr);
                conditionsUntilNow = context.mkOr(conditionsUntilNow,caseExpr);
            }

            VariablesScope changedVars = null;
            if (switchRule.block()!=null){
                changedVars = VariablesScope.resolveFirstScope(switchRule.block(), exceptionBuilder);
            } else if (switchRule.expression()!=null) {
                changedVars = VariablesScope.resolveFirstScope(switchRule.expression(), exceptionBuilder);
            } else if (switchRule.throwStatement()!=null){
                throw exceptionBuilder.inContext(switchRule,"Unreacheable case: ThrowStatement not supported. So the enter method should have cause an exception already!");
            }

            //System.out.println("The case "+caseExpr+" gets executed when " + thisExprIsExecuted + " and sets these Variables:");
            exitBlockHelper(ctx,thisExprIsExecuted,changedVars);

        }
    }
}
