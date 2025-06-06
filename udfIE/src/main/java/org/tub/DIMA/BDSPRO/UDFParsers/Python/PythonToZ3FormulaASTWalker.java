package org.tub.DIMA.BDSPRO.UDFParsers.Python;

import com.microsoft.z3.*;

import org.antlr.v4.grammars.Python3Parser;
import org.antlr.v4.grammars.Python3ParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.AstException;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.AstUtils;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.VariablesScope;
import org.tub.DIMA.BDSPRO.UDFParsers.AST.ClassContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Remaps a ParseTree of a PythonUDF to a Z3 Expression
 */
public class PythonToZ3FormulaASTWalker extends Python3ParserBaseListener {
    private final AstException.AstExceptionBuilder exceptionBuilder;
    private final Context context;
    private final Python3Parser parser;
    private final UDF udf;
    ClassContext classContext;


    public PythonToZ3FormulaASTWalker(Python3Parser parser, UDF udf, Context context) {
        this.parser = parser;
        this.udf = udf;
        this.context = context;
        this.exceptionBuilder = new AstException.AstExceptionBuilder(udf,parser);
    }

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

    @Override
    public void enterFile_input(Python3Parser.File_inputContext ctx) {
        if (classContext != null) {
            throw exceptionBuilder.inContext(ctx,"There was already a class registerred for this UDF");
        }
        classContext = ClassContext.attachClass(ctx,exceptionBuilder);
        VariablesScope.addVariableScope(ctx);
    }


    @Override
    public void enterFuncdef(Python3Parser.FuncdefContext ctx) {
        Python3Parser.TypedargslistContext args = ctx.parameters().typedargslist();
        if (args == null) {
            VariablesScope.addVariableScope(ctx);
        } else if (args.STAR()!=null) {
            throw exceptionBuilder.inContext(args,"* or ** arguments are not supported!");
        } else if (args.POWER()!=null){
            throw exceptionBuilder.inContext(args,"* or ** arguments are not supported!");
        } else {
            // Effectively we parse this here: tfpdef ('=' test)? (',' tfpdef ('=' test)?)*
            // the * and ** cases are handled above.
            int parameter = 0;
            HashMap<String, Expr> params = new HashMap<>();

            for (int childIdx = 0; childIdx < args.getChildCount(); childIdx++) {
                Python3Parser.TfpdefContext param = (Python3Parser.TfpdefContext) args.getChild(childIdx);

                String paramName = param.name().getText();

                parameter++;

                if (childIdx + 1< args.getChildCount() ) {
                    // WARNING: be careful using the childIdx afterwards!
                    childIdx++;
                    ParseTree test = args.getChild(childIdx);
                    if (test instanceof Python3Parser.TestContext) {
                        // TODO support default variables here?
                        Python3Parser.TestContext testContext = (Python3Parser.TestContext) test;
                        throw exceptionBuilder.inContext(testContext,"test in function params not supported.");
                    }
                }

                Expr<IntSort> paramExpr = context.mkIntConst("p"+(parameter));
                params.put(paramName,paramExpr);
            }
            VariablesScope.addVariableScope(ctx,params);
        }
    }

    @Override
    public void exitFuncdef(Python3Parser.FuncdefContext ctx) {
        Supplier<Expr> z3Expr = getZ3Expr(ctx.block());
        ClassContext classContext = ClassContext.resolveClass(ctx, exceptionBuilder);
        String methodName = ctx.name().getText();

        classContext.declareMethod(methodName,z3Expr.get());
    }

    @Override
    public void exitBlock(Python3Parser.BlockContext ctx) {
        if (ctx.stmt(0)!=null){
            propagateZ3Expression(ctx,ctx.stmt().getLast());
        } else {
            propagateZ3Expression(ctx,ctx.simple_stmts());
        }
    }

    @Override
    public void exitStmt(Python3Parser.StmtContext ctx) {
        if(ctx.simple_stmts()!=null) {
            propagateZ3Expression(ctx,ctx.simple_stmts());
        }
    }

    @Override
    public void exitSimple_stmts(Python3Parser.Simple_stmtsContext ctx) {
        propagateZ3Expression(ctx,ctx.simple_stmt().getLast());
    }

    @Override
    public void exitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        if (ctx.assert_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.assert_stmt(),"assert statements not supported!");
        } else if (ctx.nonlocal_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.nonlocal_stmt(),"nonlocal statements not supported!");
        } else if (ctx.global_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.global_stmt(),"global statements not supported!");
        } else if (ctx.import_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.import_stmt(),"import statements not supported!");
        } else if (ctx.flow_stmt()!=null) {
            propagateZ3Expression(ctx,ctx.flow_stmt());
        } else if (ctx.pass_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.pass_stmt(),"pass statements not supported!");
        } else if (ctx.del_stmt()!=null) {
            throw exceptionBuilder.inContext(ctx.del_stmt(),"del statements not supported!");
        } else {
            propagateZ3Expression(ctx,ctx.expr_stmt());
        }
    }


    @Override
    public void enterTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
        int child_count = ctx.test().size() + ctx.star_expr().size();
        if (child_count>1) throw exceptionBuilder.inContext(ctx,"Assignments only support single return value");
    }

    @Override
    public void exitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {

        String variableName;
        // Python grammar is fun :))
        {
            Python3Parser.Testlist_star_exprContext assignedVar = ctx.testlist_star_expr(0);
            Python3Parser.TestContext testContext = assignedVar.test(0);
            if (assignedVar.star_expr(0)!=null) throw exceptionBuilder.inContext(assignedVar,"Assignments dont support star expression");
            if ( testContext.IF()!=null) throw exceptionBuilder.inContext(testContext,"Cannot assign to conditional expression");
            if ( testContext.lambdef()!=null) throw exceptionBuilder.inContext(testContext.lambdef(),"Cannot assign to lambda expression");

            Python3Parser.Or_testContext orTestContext = testContext.or_test(0);
            if ( orTestContext.OR(0)!=null) throw exceptionBuilder.inContext(orTestContext,"Cannot assign to OR expression");

            Python3Parser.And_testContext andTestContext = orTestContext.and_test(0);
            if ( andTestContext.AND(0)!=null) throw exceptionBuilder.inContext(andTestContext,"Cannot assign to AND expression");

            Python3Parser.Not_testContext notTestContext = andTestContext.not_test(0);
            if ( notTestContext.NOT()!=null) throw exceptionBuilder.inContext(notTestContext,"Cannot assign to not expression");

            Python3Parser.ComparisonContext comparison = notTestContext.comparison();
            if ( comparison.comp_op(0)!=null) throw exceptionBuilder.inContext(comparison.comp_op(0),"Cannot assign to comparison expression");

            Python3Parser.ExprContext expr = comparison.expr(0);
            Python3Parser.Atom_exprContext atomExprContext = expr.atom_expr();
            if (atomExprContext ==null) throw exceptionBuilder.inContext(expr,"Cannot assign to a non-atom expression");
            if (atomExprContext.AWAIT() !=null) throw exceptionBuilder.inContext(expr,"Async/Await is not supported");
            if (atomExprContext.trailer(0) !=null) throw exceptionBuilder.inContext(atomExprContext.trailer(0),"Variable trailers are forbidden");

            Python3Parser.AtomContext atom = atomExprContext.atom();
            if (atom.name()==null) throw exceptionBuilder.inContext(atom,"Only assigning directly to a variable is supported");

            variableName = atom.name().getText();
        }

        Supplier<Expr> newValue;

        if (ctx.ASSIGN(0)!=null) {
            if (ctx.ASSIGN().size()>1) throw exceptionBuilder.inContext(ctx,"multiple assignments in one expression not supported!");
            if (ctx.yield_expr(0)!=null) throw exceptionBuilder.inContext(ctx.yield_expr(0),"yield expressions are not supported!");
            // We must use index 1 here as there is already another testlist_star_expr in the grammar rule
            newValue = getZ3Expr(ctx.testlist_star_expr(1));
        } else if (ctx.augassign()!=null){
            throw exceptionBuilder.inContext(ctx,"Augmented Assignments are not supported!");
        } else if (ctx.annassign()!=null) {
            newValue = getZ3Expr(ctx.annassign());
        } else {
            throw exceptionBuilder.inContext(ctx,"Uninitialized Variables not supported!");
        }
        Expr expr = newValue.get();

        VariablesScope.setOrinitializeVariable(ctx,variableName, expr,exceptionBuilder);
        setZ3Expression(ctx,expr);
    }

    @Override
    public void exitAnnassign(Python3Parser.AnnassignContext ctx) {
        if (ctx.test(1)==null) throw exceptionBuilder.inContext(ctx,"Annotated assignment did not have a expression!");
        propagateZ3Expression(ctx,ctx.test(1));
    }

    @Override
    public void exitTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
        if (ctx.star_expr(0)!=null) throw exceptionBuilder.inContext(ctx,"Star expressions are not supported!");
        propagateZ3Expression(ctx,ctx.test(0));
    }

    @Override
    public void exitTest(Python3Parser.TestContext ctx) {
        if (ctx.lambdef()!=null) throw exceptionBuilder.inContext(ctx,"Lambda Definitions not supported!");

        if (ctx.IF()!=null){
            Supplier<Expr> truthy = getZ3Expr(ctx.or_test(0));
            Supplier<Expr> condition = getZ3Expr(ctx.or_test(1));
            Supplier<Expr> falsy = getZ3Expr(ctx.test());

            setZ3Expression(ctx,()->context.mkITE(condition.get(),truthy.get(),falsy.get()));
        }else {
            propagateZ3Expression(ctx,ctx.or_test(0));
        }
    }

    @Override
    public void exitOr_test(Python3Parser.Or_testContext ctx) {
        if (ctx.OR(0)!=null){
            throw exceptionBuilder.inContext(ctx,"Python OR semantics not supported!");
        }else {
            propagateZ3Expression(ctx,ctx.and_test(0));
        }
    }

    @Override
    public void exitAnd_test(Python3Parser.And_testContext ctx) {
        if (ctx.AND(0)!=null){
            throw exceptionBuilder.inContext(ctx,"Python AND semantics not supported!");
        }else {
            propagateZ3Expression(ctx,ctx.not_test(0));
        }
    }

    @Override
    public void exitNot_test(Python3Parser.Not_testContext ctx) {
        if (ctx.NOT()!=null){
            Supplier<Expr> expr = getZ3Expr(ctx.not_test());
            setZ3Expression(ctx,()->context.mkNot((BoolExpr) expr.get()));
        }else {
            propagateZ3Expression(ctx,ctx.comparison());
        }
    }

    @Override
    public void exitComparison(Python3Parser.ComparisonContext ctx) {
        if (ctx.comp_op().size()==0){
            propagateZ3Expression(ctx,ctx.expr(0));
        }else if (ctx.comp_op().size()>1){
            throw exceptionBuilder.inContext(ctx,"Multi Comparison not supported!");
        } else {
            // we have a single comparison
            Supplier<Expr> a = getZ3Expr(ctx.expr(0));
            Supplier<Expr> b = getZ3Expr(ctx.expr(1));
            Python3Parser.Comp_opContext comparator = ctx.comp_op(0);

            Supplier<Expr> newExpr;
            //TODO fill out more operators
            if (comparator.IS()!=null ) {
                throw exceptionBuilder.inContext(comparator,"\"is\" operator not supported!");
            } else if (comparator.IN()!=null ) {
                throw exceptionBuilder.inContext(comparator,"\"in\" operator not supported!");
            } else  if (comparator.NOT_EQ_1()!=null || comparator.NOT_EQ_2()!=null ) {
                newExpr = () -> context.mkNot(context.mkEq(a.get(),b.get()));
            } else if (comparator.LT_EQ()!=null ) {
                newExpr = () -> context.mkLe(a.get(),b.get());
            } else if (comparator.GT_EQ()!=null ) {
                newExpr = () -> context.mkGe(a.get(),b.get());
            } else if (comparator.EQUALS()!=null ) {
                newExpr = () -> context.mkEq(a.get(),b.get());
            } else if (comparator.LESS_THAN()!=null ) {
                newExpr = () -> context.mkLt(a.get(),b.get());
            } else if (comparator.GREATER_THAN()!=null ) {
                newExpr = () -> context.mkGt(a.get(),b.get());
            } else {
                throw exceptionBuilder.inContext(comparator,"This comparator is not supported!");
            }

            setZ3Expression(ctx,newExpr);

        }
    }

    @Override
    public void exitExpr(Python3Parser.ExprContext ctx) {
        Python3Parser.ExprContext a = ctx.expr(0);
        Python3Parser.ExprContext b = ctx.expr(1);


        if (b==null) {
            if (ctx.atom_expr()!=null) {
                propagateZ3Expression(ctx,ctx.atom_expr());
            }  else {
                throw exceptionBuilder.inContext(ctx,"This expression is not yet supported!");
            }
        } else {
            setZ3Expression(ctx,()->{
                Supplier<BitVecExpr> unresolvedA_BV = getZ3BVExpr(a);
                Supplier<BitVecExpr> unresolvedB_BV = getZ3BVExpr(b);

                Supplier<IntExpr> unresolvedA_Int = getZ3IntExpr(a);
                Supplier<IntExpr> unresolvedB_Int = getZ3IntExpr(b);


                if (ctx.OR_OP()!=null){
                    return context.mkBVOR(unresolvedA_BV.get(),unresolvedB_BV.get());
                } else if (ctx.XOR()!=null){
                    return context.mkBVXOR(unresolvedA_BV.get(),unresolvedB_BV.get());
                } else if (ctx.AND_OP()!=null){
                    return context.mkBVAND(unresolvedA_BV.get(),unresolvedB_BV.get());
                } else if (ctx.LEFT_SHIFT()!=null){
                    return context.mkBVLSHR(unresolvedA_BV.get(),unresolvedB_BV.get());
                } else if (ctx.RIGHT_SHIFT()!=null){
                    return context.mkBVLSHR(unresolvedA_BV.get(),unresolvedB_BV.get());
                } else if (ctx.MINUS(0)!=null){
                    return context.mkSub(unresolvedA_Int.get(),unresolvedB_Int.get());
                } else if (ctx.ADD(0)!=null){
                    return context.mkAdd(unresolvedA_Int.get(),unresolvedB_Int.get());
                } else if (ctx.IDIV()!=null){
                    throw exceptionBuilder.inContext(ctx,"This expression is not yet supported!");
                } else if (ctx.MOD()!=null){
                    return context.mkMod(unresolvedA_Int.get(),unresolvedB_Int.get());
                } else if (ctx.DIV()!=null){
                    return context.mkDiv(unresolvedA_Int.get(),unresolvedB_Int.get());
                } else if (ctx.AT()!=null){
                    throw exceptionBuilder.inContext(ctx,"This expression is not yet supported!");
                }else if (ctx.STAR()!=null){
                    return context.mkMul(unresolvedA_Int.get(),unresolvedB_Int.get());
                }else if (ctx.POWER()!=null){
                    return context.mkPower(unresolvedA_Int.get(),unresolvedB_Int.get());
                }else {
                    throw exceptionBuilder.inContext(ctx,"This expression is not yet supported!");
                }
            });
        }

    }

    @Override
    public void exitAtom_expr(Python3Parser.Atom_exprContext ctx) {
        if (ctx.AWAIT()!=null) throw exceptionBuilder.inContext(ctx,"Async/Await not supported!");
        if (ctx.trailer(0)!=null) throw exceptionBuilder.inContext(ctx.trailer(0),"Trailer expressions not supported!");

        propagateZ3Expression(ctx,ctx.atom());
    }

    @Override
    public void exitAtom(Python3Parser.AtomContext ctx) {
        if (ctx.FALSE()!=null) {
            setZ3Expression(ctx,context.mkFalse());
        } else if (ctx.TRUE()!=null){
            setZ3Expression(ctx,context.mkTrue());
        }else if (ctx.NONE()!=null){
            throw exceptionBuilder.inContext(ctx,"None not supported!");
        }else if (ctx.ELLIPSIS()!=null){
            throw exceptionBuilder.inContext(ctx,"Ellipsis not supported! (The \"...\" )");
        }else if (ctx.STRING(0)!=null){
            throw exceptionBuilder.inContext(ctx,"Strings not supported!");
        }else if (ctx.NUMBER()!=null){
            String number = ctx.NUMBER().getText();
            Integer i = Integer.valueOf(number);
            setZ3Expression(ctx,context.mkInt(i));
        }else if (ctx.name()!=null){
            String variableName = ctx.name().getText();
            setZ3Expression(ctx,()->{
                try {
                    return VariablesScope.getVariable(ctx, variableName, exceptionBuilder);
                } catch (AstException e ){
                    // we ignore the fact that the expression might not have been set.
                    return null;
                }
            });
        }else if (ctx.dictorsetmaker()!=null){
            throw exceptionBuilder.inContext(ctx,"Dicts/Sets notation not supported!");
        }else if (ctx.testlist_comp()!=null && ctx.OPEN_BRACK()!=null  && ctx.CLOSE_BRACK()!=null ){
            throw exceptionBuilder.inContext(ctx,"Array Access not supported");
        }else if (ctx.testlist_comp()!=null && ctx.OPEN_PAREN()!=null  && ctx.CLOSE_PAREN()!=null){
            propagateZ3Expression(ctx,ctx.testlist_comp());
        } else {
            throw exceptionBuilder.inContext(ctx,"Unsupported Atom expression");
        }
    }

    @Override
    public void exitFlow_stmt(Python3Parser.Flow_stmtContext ctx) {
        if (ctx.return_stmt() != null) {
            propagateZ3Expression(ctx,ctx.return_stmt());
        } else {
            throw exceptionBuilder.inContext(ctx,"Unsupported flow statement");
        }
    }

    @Override
    public void exitReturn_stmt(Python3Parser.Return_stmtContext ctx) {
        if (ctx.testlist()==null) {
            throw exceptionBuilder.inContext(ctx,"Empty Return is not supported!");
        }

        propagateZ3Expression(ctx,ctx.testlist());
    }

    @Override
    public void enterTestlist(Python3Parser.TestlistContext ctx) {
        if (ctx.test().size()>1) throw exceptionBuilder.inContext(ctx,"Comma separation not supported!");
    }

    @Override
    public void exitTestlist(Python3Parser.TestlistContext ctx) {
        propagateZ3Expression(ctx,ctx.test(0));
    }

    @Override
    public void exitCompound_stmt(Python3Parser.Compound_stmtContext ctx) {
        Supplier<Expr> newValue;
        if (ctx.funcdef()!=null){
            // this statement does not have an expression that represents them.
        } else if (ctx.match_stmt()!=null){
            // this statement does not have an expression that represents them.
        } else if (ctx.if_stmt()!=null){
            // this statement does not have an expression that represents them.
        } else {
            throw exceptionBuilder.inContext(ctx,"This compound statement is not yet supported!");
        }
        // currently not needed...
        //setZ3Expression(ctx,newValue);
    }

    @Override
    public void enterCase_block(Python3Parser.Case_blockContext ctx) {
        if (ctx.guard()!=null) {
            throw exceptionBuilder.inContext(ctx,"Guarded expression not supported!");
        }

        VariablesScope.addVariableScope(ctx.block());
    }

    @Override
    public void exitMatch_stmt(Python3Parser.Match_stmtContext ctx) {
        Expr switchExpr = getZ3Expr(ctx.subject_expr()).get();

        BoolExpr conditionsUntilNow = null;
        // We must propagate assigned variables from the blocks:
        for (Python3Parser.Case_blockContext caseBlock : ctx.case_block()){
            // Construct the expression which case to take.
            BoolExpr caseExpr =  null;
            // This block figures out the expression to call this block
            {
                // Because the grammar is a bit unwieldy, we collect all the patterns in a List
                List<Python3Parser.Closed_patternContext> patterns = new ArrayList<>();

                if (caseBlock.patterns().pattern() !=null) {
                    patterns.addAll(caseBlock.patterns().pattern().or_pattern().closed_pattern());
                } else {
                    assert caseBlock.patterns().open_sequence_pattern() !=null;

                    Python3Parser.Open_sequence_patternContext openSequencePatternContext = caseBlock.patterns().open_sequence_pattern();

                    // Declare Helper Function:
                    Consumer<Python3Parser.Maybe_star_patternContext> addMaybeStarPattern = maybe_star_pattern -> patterns.addAll(maybe_star_pattern.pattern().or_pattern().closed_pattern());



                    addMaybeStarPattern.accept(openSequencePatternContext.maybe_star_pattern());
                    if (openSequencePatternContext.maybe_sequence_pattern() != null) {
                        for (Python3Parser.Maybe_star_patternContext maybeStarPatternContext : openSequencePatternContext.maybe_sequence_pattern().maybe_star_pattern()) {
                            addMaybeStarPattern.accept(maybeStarPatternContext);
                        }
                    }
                }

                caseExpr = patterns.stream().map(pattern -> {
                    if (pattern.wildcard_pattern()!=null) {
                        return context.mkTrue();
                    } else {
                        return context.mkEq(switchExpr, getZ3Expr(pattern).get());
                    }
                }).reduce(null,(init,pattern)->{
                    if (init==null) {
                        return pattern;
                    } else {
                        return context.mkOr(init,pattern);
                    }
                });
            }

            // Now we figure out if this block is actually called, or if a previous block was called.
            BoolExpr thisExprIsExecuted;
            if (conditionsUntilNow ==null) {
                conditionsUntilNow = caseExpr;
                thisExprIsExecuted = caseExpr;
            }else {
                thisExprIsExecuted = context.mkAnd(context.mkNot(conditionsUntilNow), caseExpr);
                conditionsUntilNow = context.mkOr(conditionsUntilNow,caseExpr);
            }

            VariablesScope changedVars = VariablesScope.resolveFirstScope(caseBlock.block(), exceptionBuilder);

            //System.out.println("The case "+caseExpr+" gets executed when " + thisExprIsExecuted + " and sets these Variables:");
            exitBlockHelper(ctx,thisExprIsExecuted,changedVars);
        }
    }

    @Override
    public void enterStar_named_expressions(Python3Parser.Star_named_expressionsContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Star named expression not supported!");
    }

    @Override
    public void enterStar_named_expression(Python3Parser.Star_named_expressionContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Star named pattern not supported!");
    }

    @Override
    public void enterStar_pattern(Python3Parser.Star_patternContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Star pattern not supported!");
    }

    @Override
    public void enterDouble_star_pattern(Python3Parser.Double_star_patternContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Double Star pattern not supported!");
    }

    @Override
    public void enterStar_expr(Python3Parser.Star_exprContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Star expr not supported!");
    }

    @Override
    public void enterComp_for(Python3Parser.Comp_forContext ctx) {
        throw exceptionBuilder.inContext(ctx,"Comp for not supported!");
    }



    @Override
    public void exitTestlist_comp(Python3Parser.Testlist_compContext ctx) {
        propagateZ3Expression(ctx,ctx.test(0));
    }

    @Override
    public void enterSubject_expr(Python3Parser.Subject_exprContext ctx) {
        if (ctx.COMMA()!=null) {
            throw exceptionBuilder.inContext(ctx,"star_named_expression in subject_expr not supported!");
        }
    }

    @Override
    public void exitSubject_expr(Python3Parser.Subject_exprContext ctx) {
        propagateZ3Expression(ctx,ctx.test());
    }

    @Override
    public void enterAs_pattern(Python3Parser.As_patternContext ctx) {
        throw exceptionBuilder.inContext(ctx,"as pattern not supported!");
    }


    @Override
    public void exitClosed_pattern(Python3Parser.Closed_patternContext ctx) {
        if (ctx.value_pattern()!=null) {
            propagateZ3Expression(ctx,ctx.value_pattern());
        } else if (ctx.wildcard_pattern()!=null) {
            // Nothing to propagate here!
        } else if (ctx.literal_pattern()!=null) {
            propagateZ3Expression(ctx,ctx.literal_pattern());
        } else {
            throw exceptionBuilder.inContext(ctx,"this closed_pattern not supported!");
        }
    }

    @Override
    public void exitLiteral_pattern(Python3Parser.Literal_patternContext ctx) {

        if (ctx.signed_number()==null) {
            throw exceptionBuilder.inContext(ctx,"This literal pattern is not supported!");
        }

        propagateZ3Expression(ctx,ctx.signed_number());
    }

    @Override
    public void exitSigned_number(Python3Parser.Signed_numberContext ctx) {
        String literalText = ctx.NUMBER().getText();
        Integer literalValue = Integer.valueOf(literalText);
        if (ctx.MINUS()!=null) {
            literalValue = -literalValue;
        }
        setZ3Expression(ctx,context.mkInt(literalValue));
    }

    @Override
    public void enterIf_stmt(Python3Parser.If_stmtContext ctx) {
        for (Python3Parser.BlockContext blockContext : ctx.block()) {
            VariablesScope.addVariableScope(blockContext);
        }
    }

    @Override
    public void exitIf_stmt(Python3Parser.If_stmtContext ctx) {
        List<Python3Parser.TestContext> ifConditions = ctx.test();
        List<Python3Parser.BlockContext> blocks = ctx.block();

        BoolExpr conditionsUntilNow = null;
        for (int i = 0; i < ifConditions.size(); i++) {
            BoolExpr ifCondition = (BoolExpr) getZ3Expr(ifConditions.get(i)).get();
            Python3Parser.BlockContext block = blocks.get(i);
            VariablesScope changedVariables = VariablesScope.resolveFirstScope(block, exceptionBuilder);

            // Now we figure out if this block is actually called, or if a previous block was called.
            BoolExpr thisExprIsExecuted;
            if (conditionsUntilNow ==null) {
                conditionsUntilNow = ifCondition;
                thisExprIsExecuted = ifCondition;
            }else {
                thisExprIsExecuted = context.mkAnd(context.mkNot(conditionsUntilNow), ifCondition);
                conditionsUntilNow = context.mkOr(conditionsUntilNow,ifCondition);
            }

            exitBlockHelper(ctx,thisExprIsExecuted,changedVariables);
        }

        // We have an else block, that
        if (ctx.ELSE()!=null) {
            VariablesScope changedVariables = VariablesScope.resolveFirstScope(blocks.getLast(), exceptionBuilder);
            exitBlockHelper(ctx,context.mkNot(conditionsUntilNow),changedVariables);
        }
    }
}

