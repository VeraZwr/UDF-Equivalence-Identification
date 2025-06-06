package org.tub.DIMA.BDSPRO.UDFParsers.Java;

import org.antlr.v4.grammars.Java20Parser;
import org.antlr.v4.grammars.Java20ParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Desugars a for loop into a while loop.
 */
public class JavaForLoopDesugarPreprocessor extends Java20ParserBaseListener {

    @Override
    public void enterBlockStatements(Java20Parser.BlockStatementsContext ctx) {
        int childCount = ctx.getChildCount();
        for (int statementIdx = 0; statementIdx < childCount; statementIdx++) {
            Java20Parser.BlockStatementContext blockStatement;
            Java20Parser.StatementContext statement;
            Java20Parser.BasicForStatementContext forStatement;
            try {
                blockStatement = ctx.blockStatement(statementIdx);
                statement = blockStatement.statement();
                forStatement = statement.forStatement().basicForStatement();
            } catch (NullPointerException e) {
                continue;
            }
            if (forStatement == null) {
                continue;
            }

            throw new UnsupportedOperationException("Not Yet Implemented!");

            // Now essentially rewrite this:
            //    'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
            // to this:
            //    forInit
            //    while expression
            //      statement
            //      forUpdate


            // Construct the blockStatement ->
//            ParseTree forInit = new Java20Parser.BlockStatementContext(ctx, blockStatement.invokingState);
//            //forInit
//
//
//            // Construct an IF-THEN Statement that, when executed repeatedly, performs the same operation as the while loop.
//            Java20Parser.WhileStatementContext whileStatementContext = new Java20Parser.WhileStatementContext((ParserRuleContext) statement, forStatement.invokingState);
//            whileStatementContext.start = forStatement.start;
//            whileStatementContext.stop = forStatement.stop;
//            whileStatementContext.addChild(forStatement.expression());
//            whileStatementContext.addChild(forStatement.statement());
//
//            // Replace the while loop with an IF-THEN Statement
//            statement.children.clear();
//            statement.addChild(whileStatementContext);
//
//            // Build a new list of children.
//            List<ParseTree> statementsBefore = statementIdx > 0 ? ctx.children.subList(0, statementIdx) : Collections.emptyList();
//            List<ParseTree> unrolledLoop = List.of(forInit,whileStatementContext); // TODO: I believe this does NOT deep copy the if statements!!!
//            List<ParseTree> statementsAfter = statementIdx + 1 < childCount ? ctx.children.subList(statementIdx + 1, childCount) : Collections.emptyList();
//            // ...and Replace children list
//            ctx.children = new ArrayList<>();
//            ctx.children.addAll(statementsBefore);
//            ctx.children.addAll(unrolledLoop);
//            ctx.children.addAll(statementsAfter);

        }
    }
}
