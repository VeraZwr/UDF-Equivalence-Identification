package org.tub.DIMA.BDSPRO.UDFCompilers.Java;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class CustomJavaFile extends SimpleJavaFileObject {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public CustomJavaFile(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
    }

    // When the java compiler requests an Output stream where to place the resulting classfile, we simply give it a buffer.
    @Override
    public OutputStream openOutputStream() {
        return baos;
    }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }
}
