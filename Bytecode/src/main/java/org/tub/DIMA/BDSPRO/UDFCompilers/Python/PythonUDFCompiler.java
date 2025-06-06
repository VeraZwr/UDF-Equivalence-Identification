package org.tub.DIMA.BDSPRO.UDFCompilers.Python;

import javassist.ClassPool;
import javassist.CtClass;
import org.tub.DIMA.BDSPRO.UDFCompilers.UDFCompiler;
import org.tub.DIMA.BDSPRO.UDFLanguages;
import org.tub.DIMA.BDSPRO.UDF;



public class PythonUDFCompiler implements UDFCompiler {
    ClassPool cp;

    public PythonUDFCompiler(ClassPool cp) {
        this.cp = cp;
    }

    @Override
    public CtClass compile(UDF udf) {
        if (udf.getLanguage() != UDFLanguages.PYTHON) {
            throw new IllegalArgumentException("Tried to load a non-Java UDF with the Java Loader");
        }
        return null;

        // I looked at using Jython. Their jythonc compiler was able to transpile java->python.
        // But this is deprecated. The new tooling seems to do all the heavy lifting. I think they are just fine with
        // using a python interpreter running in java with a bit of interfaces/glue code to make integration integrations.
        // But there does not seem to be a way to transpile using jython.

        //

        //throw new UnsupportedOperationException("TODO :(");
    }
}
