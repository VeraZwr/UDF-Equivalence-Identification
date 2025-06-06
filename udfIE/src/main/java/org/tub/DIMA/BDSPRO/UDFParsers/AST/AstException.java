package org.tub.DIMA.BDSPRO.UDFParsers.AST;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.tub.DIMA.BDSPRO.UDF;


/**
 * Custom Exception to pretty Print Errors.
 */
public class AstException extends RuntimeException{


    private final String message;
    private final UDF udf;
    private final ParserRuleContext ctx;
    private final Parser parser;

    public AstException(String message, UDF udf, ParserRuleContext ctx, Parser parser) {
        this.message = message;
        this.udf = udf;
        this.ctx = ctx;
        this.parser = parser;
    }

    @Override
    public String toString() {
        if (ctx != null) {
            int startLine = ctx.start.getLine();
            int startCharPos = ctx.start.getCharPositionInLine();
            int endLine = ctx.stop.getLine();
            int endCharPos = ctx.stop.getCharPositionInLine();


            if (ctx.start == ctx.stop) {
                endCharPos += ctx.start.getStopIndex()- ctx.start.getStartIndex();
            }

            String highlightedSourcecode = udf.highlightedSourcecode(startLine,startCharPos,endLine,endCharPos);
            return message.trim()+"\n"+highlightedSourcecode+"Related to this parse Tree:\n"+ctx.toStringTree(parser).indent(4);

        } else {
            return message.trim()+"\n\nAt unknown location."+udf.toString();
        }
    }







    public static class AstExceptionBuilder {
        private final UDF udf;
        private final Parser parser;

        public AstExceptionBuilder(UDF udf, Parser parser) {
            this.udf = udf;
            this.parser = parser;
        }

        public AstException build(){
            return build("Unspecified Problem");
        }

        public AstException build(String message){
            return new AstException(message,udf,null,parser);
        }

        public AstException inContext(ParserRuleContext ctx){
            return inContext(ctx,"Unspecified Problem");
        }

        public AstException inContext(ParserRuleContext ctx, String message){
            return new AstException(message,udf,ctx,parser);
        }



    }
}
