package org.tub.DIMA.BDSPRO.UDFParsers.Java;

import org.antlr.v4.grammars.Java20Parser;
import org.antlr.v4.grammars.Java20ParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unrolls while loops in a java udf by copy-pasting the body of the while loop as an if-expression {@value MAX_ITERATIONS} times.
 */
public class JavaWhileLoopUnrollerPreprocessor extends Java20ParserBaseListener {
    static final int MAX_ITERATIONS = 100;


    @Override
    public void enterBlockStatements(Java20Parser.BlockStatementsContext ctx) {
        int childCount = ctx.getChildCount();
        for (int statementIdx = 0; statementIdx < childCount; statementIdx++) {
            Java20Parser.BlockStatementContext blockStatement;
            Java20Parser.StatementContext statement;
            Java20Parser.WhileStatementContext whileStatement;
            try {
                blockStatement = ctx.blockStatement(statementIdx);
                statement = blockStatement.statement();
                whileStatement = statement.whileStatement();
            } catch (NullPointerException e) {
                continue;
            }
            if (whileStatement == null) {
                continue;
            }

            // Construct an IF-THEN Statement that, when executed repeatedly, performs the same operation as the while loop.
            Java20Parser.IfThenStatementContext ifThenStatement = new Java20Parser.IfThenStatementContext((ParserRuleContext) statement, whileStatement.invokingState);
            ifThenStatement.start = whileStatement.start;
            ifThenStatement.stop = whileStatement.stop;
            ifThenStatement.addChild(whileStatement.expression());
            ifThenStatement.addChild(whileStatement.statement());

            // Replace the while loop with an IF-THEN Statement
            statement.children.clear();
            statement.addChild(ifThenStatement);

            // Build a new list of children.
            List<ParseTree> statementsBefore = statementIdx > 0 ? ctx.children.subList(0, statementIdx) : Collections.emptyList();
            List<ParseTree> unrolledLoop = Collections.nCopies(MAX_ITERATIONS, blockStatement); // TODO: I believe this does NOT deep copy the if statements!!!
            List<ParseTree> statementsAfter = statementIdx + 1 < childCount ? ctx.children.subList(statementIdx + 1, childCount) : Collections.emptyList();
            // ...and Replace children list
            ctx.children = new ArrayList<>();
            ctx.children.addAll(statementsBefore);
            ctx.children.addAll(unrolledLoop);
            ctx.children.addAll(statementsAfter);

            // Increase the number of children to iterate over, as we added a bunch more.
            childCount += (MAX_ITERATIONS -1);
            statementIdx += (MAX_ITERATIONS -1);
        }
    }
}
