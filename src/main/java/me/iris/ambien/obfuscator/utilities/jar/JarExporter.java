package me.iris.ambien.obfuscator.utilities.jar;

import lombok.experimental.UtilityClass;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.Comment;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

@UtilityClass
public class JarExporter {
    public void exportJar(final JarWrapper wrapper) throws IOException {
        // Create output stream
        final JarOutputStream stream = new JarOutputStream(Files.newOutputStream(new File(Ambien.get.outputJar).toPath()));

        // Set compression level
        if (Ambien.get.transformerManager.getTransformer("aggressive-compression").isEnabled())
            stream.setLevel(Deflater.BEST_COMPRESSION);
        else
            stream.setLevel(Deflater.DEFAULT_COMPRESSION);

        // Add directories
        wrapper.getDirectories().forEach(directory -> {
            try {
                IOUtil.writeDirectoryEntry(stream, directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add resources
        wrapper.getResources().forEach((name, bytes) -> {
            try {
                IOUtil.writeEntry(stream, name, bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add classes
        wrapper.getClasses().forEach(classWrapper -> {
            try {
                String name = classWrapper.getName();
                if (Ambien.get.transformerManager.getTransformer("folder-classes").isEnabled())
                    name += "/";

                IOUtil.writeEntry(stream, name, classWrapper.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Set zip comment
        if (Ambien.get.transformerManager.getTransformer("comment").isEnabled())
            stream.setComment(Comment.commentText.getValue());

        // Close stream
        stream.close();
    }
}
