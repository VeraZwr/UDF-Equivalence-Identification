package org.tub.DIMA.BDSPRO.UDFParsers.Python;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

/**
 * Base Parser from ANTLR Grammars Project for Python
 */
public abstract class Python3ParserBase extends Parser
{
    protected Python3ParserBase(TokenStream input)
    {
	super(input);
    }

    public boolean CannotBePlusMinus()
    {
	return true;
    }

    public boolean CannotBeDotLpEq()
    {
	return true;
    }
}
