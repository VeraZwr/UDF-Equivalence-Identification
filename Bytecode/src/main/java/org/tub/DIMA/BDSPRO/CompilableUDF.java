package org.tub.DIMA.BDSPRO;

import javassist.CtClass;
import org.tub.DIMA.BDSPRO.UDFCompilers.PolyglotUDFCompiler;

public class CompilableUDF {
    final PolyglotUDFCompiler udfCompiler;
    public final UDF udf;
    CtClass cachedCtClass = null;

    public CompilableUDF(PolyglotUDFCompiler udfCompiler, UDF udf) {
        this.udfCompiler = udfCompiler;
        this.udf = udf;
    }

    public CtClass compile(){
        if (cachedCtClass==null) {
            cachedCtClass = udfCompiler.compile(udf);
        }
        return cachedCtClass;
    }

    @Override
    public String toString() {
        return udf.toString();
    }
}
