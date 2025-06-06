package org.tub.DIMA.BDSPRO.UDFCompilers;

import javassist.CtClass;
import org.tub.DIMA.BDSPRO.UDF;

public interface UDFCompiler {
    CtClass compile(UDF udf);
}
