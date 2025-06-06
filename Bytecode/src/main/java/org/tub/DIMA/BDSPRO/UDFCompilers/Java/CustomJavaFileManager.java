package org.tub.DIMA.BDSPRO.UDFCompilers.Java;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed
 * support for multiple compile units.
 */
public class CustomJavaFileManager extends
        ForwardingJavaFileManager<JavaFileManager> {

    public List<CustomJavaFile> getCompiledCode() {
        return compiledCode;
    }

    private final List<CustomJavaFile> compiledCode = new ArrayList<>();

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager
     *            delegate to this file manager
     */
    protected CustomJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location, String className,
            JavaFileObject.Kind kind, FileObject sibling) {

        try {
            CustomJavaFile innerClass = new CustomJavaFile(className);
            compiledCode.add(innerClass);

            return innerClass;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while creating in-memory output file for "
                            + className, e);
        }
    }
}