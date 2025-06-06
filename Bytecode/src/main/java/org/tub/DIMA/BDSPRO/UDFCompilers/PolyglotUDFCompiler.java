package org.tub.DIMA.BDSPRO.UDFCompilers;

import javassist.CtClass;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFCompilers.Java.JavaUDFCompiler;
import org.tub.DIMA.BDSPRO.UDFCompilers.Python.PythonUDFCompiler;

public class PolyglotUDFCompiler implements UDFCompiler {

    private final JavaUDFCompiler javaUDFCompiler;
    private final PythonUDFCompiler pythonUDFCompiler;

    public PolyglotUDFCompiler(JavaUDFCompiler javaUDFCompiler, PythonUDFCompiler pythonUDFCompiler) {
        this.javaUDFCompiler = javaUDFCompiler;
        this.pythonUDFCompiler = pythonUDFCompiler;
    }

    @Override
    public CtClass compile(UDF udf) {
        return switch (udf.getLanguage()){
            case PYTHON -> pythonUDFCompiler.compile(udf);
            case JAVA -> javaUDFCompiler.compile(udf);
        };
    }
}
