package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.DuplicateResources;
import me.iris.ambien.obfuscator.transformers.implementations.miscellaneous.Metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@UtilityClass
public class IOUtil {
    final long date = LocalDate.of(2022, 2, 24).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
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
        JarEntry cur = new JarEntry(directory);
        if (Ambien.get.transformerManager.getTransformer("metadata").isEnabled()) {
            if (Metadata.corruptTime.isEnabled()) {
                FileTime time = FileTime.fromMillis(date);
                cur.setLastModifiedTime(time);
                cur.setLastAccessTime(time);
                cur.setCreationTime(time);
            }
            if (!Metadata.setComment.getValue().equals("")) {
                cur.setComment(Metadata.setComment.getValue());
            }
        }
        stream.putNextEntry(cur);
        stream.closeEntry();
    }

    public void writeEntry(JarOutputStream stream, String name, byte[] data) throws IOException {
        JarEntry cur = new JarEntry(name);
        if (Ambien.get.transformerManager.getTransformer("metadata").isEnabled()) {
            if (Metadata.corruptTime.isEnabled()) {
                FileTime time = FileTime.fromMillis(date);
                cur.setLastModifiedTime(time);
                cur.setLastAccessTime(time);
                cur.setCreationTime(time);
            }
            if (!Metadata.setComment.getValue().equals("")) {
                cur.setComment(Metadata.setComment.getValue());
            }
        }
        stream.putNextEntry(cur);
        stream.write(data);
        stream.closeEntry();
    }

    public byte[] duplicateData(byte[] bytes) {
        String dupText = DuplicateResources.dupText.getValue();
        if (Ambien.get.transformerManager.getTransformer("duplicate-resources").isEnabled() &&
            DuplicateResources.dupSpoofSize.isEnabled()
        ) {
            return spoofSize(dupText, bytes);
        }
        return dupText.getBytes();
    }

    public static byte[] spoofSize(String input, byte[] original) {
        byte[] content = input.getBytes();
        int targetSize = original.length;
        byte[] resizedArray = new byte[targetSize];
        System.arraycopy(content, 0, resizedArray, 0, Math.min(content.length, targetSize));
        return resizedArray;
    }
}
