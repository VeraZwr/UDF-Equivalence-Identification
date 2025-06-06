package org.tub.DIMA.BDSPRO.UDFParsers;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.tub.DIMA.BDSPRO.UDF;


/**
 * Generic UDF Parser defines a way to transform a UDF into a z3 Expression
 */
public abstract class UDFParser {
    public abstract Expr parseToZ3Expr(Context context, UDF udf);
}
