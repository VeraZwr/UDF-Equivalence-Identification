package org.tub.DIMA.BDSPRO.UDFCompilers.Java;

import javassist.ClassPool;
import javassist.CtClass;
import org.tub.DIMA.BDSPRO.UDF;
import org.tub.DIMA.BDSPRO.UDFCompilers.UDFCompiler;
import org.tub.DIMA.BDSPRO.UDFLanguages;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;


public class JavaUDFCompiler implements UDFCompiler {
    ClassPool cp;

    public JavaUDFCompiler(ClassPool cp) {
        this.cp = cp;
    }

    @Override
    public CtClass compile(UDF udf) {
        if (udf.getLanguage() != UDFLanguages.JAVA) {
            throw new IllegalArgumentException("Tried to load a non-Java UDF with the Java Loader");
        }


        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager standardFileManager = javac.getStandardFileManager(null, null, null);
        CustomJavaFileManager fileManager = new CustomJavaFileManager(standardFileManager);

        Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = standardFileManager.getJavaFileObjectsFromFiles(Set.of(udf.getFile()));

        Boolean result = javac.getTask(null, fileManager, null, null, null, javaFileObjectsFromFiles).call();

        // For better diagnostics, see https://github.com/trung/InMemoryJavaCompiler/blob/d84c404975dc308ee5a418c5af8a6629c5e6c995/src/main/java/org/mdkt/compiler/InMemoryJavaCompiler.java#L83C3-L108C4
        if (!result) {
            throw new RuntimeException("Compilation failed");
        }

        List<CustomJavaFile> compiledCode = fileManager.getCompiledCode();
        CustomJavaFile customJavaFile = compiledCode.get(0);

        ByteArrayInputStream classBytecode = new ByteArrayInputStream(customJavaFile.getByteCode());

        try {
            return cp.makeClass(classBytecode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
