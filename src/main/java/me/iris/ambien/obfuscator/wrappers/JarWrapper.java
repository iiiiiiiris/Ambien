package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.Comment;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

public class JarWrapper {
    @Getter
    private final List<String> directories;

    @Getter
    private final List<ClassWrapper> classes;

    @Getter
    private final HashMap<String, byte[]> resources;

    @Getter
    private final List<ByteArrayOutputStream> outputStreams;

    public JarWrapper() {
        this.directories = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.resources = new HashMap<>();
        this.outputStreams = new ArrayList<>();
    }

    public JarWrapper from(final File file) throws IOException {
        if (!file.exists())
            throw new RuntimeException("Input jar file doesn't exist.");

        // Convert file to jar file
        final JarFile jarFile = new JarFile(file);
        Ambien.LOGGER.info("Loading jar: " + jarFile.getName());

        // Get jar file entries
        final Enumeration<JarEntry> entries = jarFile.entries();

        // Enumerate
        while (entries.hasMoreElements()) {
            // Get element
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            final InputStream stream = jarFile.getInputStream(entry);

            // Load entry
            if (name.endsWith(".class")) {
                // Read stream into node
                final ClassReader reader = new ClassReader(stream);
                final ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_FRAMES);

                classes.add(new ClassWrapper(name, node, false));
                Ambien.LOGGER.info("Loaded class: {}", name);
            } else if (name.endsWith("/"))
                directories.add(name);
            else {
                final byte[] bytes = IOUtil.streamToArray(stream);
                resources.put(name, bytes);
                Ambien.LOGGER.info("Loaded resource: {}", name);
            }
        }

        // Return wrapper
        return this;
    }

    public JarWrapper importLibrary(final String path) throws IOException {
        // Convert file to jar file
        final JarFile jarFile = new JarFile(path);
        Ambien.LOGGER.info("Loading library: " + jarFile.getName());

        // Get jar file entries
        final Enumeration<JarEntry> entries = jarFile.entries();

        // Enumerate
        while (entries.hasMoreElements()) {
            // Get element
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            final InputStream stream = jarFile.getInputStream(entry);

            // Load entry
            if (name.endsWith(".class")) {
                // Read stream into node
                final ClassReader reader = new ClassReader(stream);
                final ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_FRAMES);

                classes.add(new ClassWrapper(name, node, true));
                Ambien.LOGGER.info("Loaded class: {}", name);
            }
        }

        return this;
    }

    public void to() throws IOException {
        // File writer for all our output streams
        final FileOutputStream fileOutputStream = new FileOutputStream(Ambien.get.outputJar);

        // Write custom output streams
        if (!outputStreams.isEmpty()) {
            for (ByteArrayOutputStream outputStream : outputStreams) {
                fileOutputStream.write(outputStream.toByteArray());
            }

            Ambien.LOGGER.debug("Added {} extra output streams", outputStreams.size());
        }

        // Write main jar stream
        // Create output stream
        final JarOutputStream stream = new JarOutputStream(fileOutputStream);

        // Set compression level
        if (Ambien.get.transformerManager.getTransformer("aggressive-compression").isEnabled())
            stream.setLevel(Deflater.BEST_COMPRESSION);
        else
            stream.setLevel(Deflater.DEFAULT_COMPRESSION);

        // Add directories
        directories.forEach(directory -> {
            try {
                IOUtil.writeDirectoryEntry(stream, directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add resources
        resources.forEach((name, bytes) -> {
            try {
                IOUtil.writeEntry(stream, name, bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add classes
        classes.forEach(classWrapper -> {
            // Ignore library classes
            if (classWrapper.isLibraryClass()) return;

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
