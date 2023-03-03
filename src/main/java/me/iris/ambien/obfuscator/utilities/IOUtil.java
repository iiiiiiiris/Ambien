package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@UtilityClass
public class IOUtil {
    public byte[] streamToArray(final InputStream stream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[512];
        while ((read = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }

        return buffer.toByteArray();
    }

    public void writeDirectoryEntry(JarOutputStream stream, String directory) throws IOException {
        stream.putNextEntry(new JarEntry(directory));
        stream.closeEntry();
    }

    public void writeEntry(JarOutputStream stream, String name, byte[] data) throws IOException {
        stream.putNextEntry(new JarEntry(name));
        stream.write(data);
        stream.closeEntry();
    }
}
