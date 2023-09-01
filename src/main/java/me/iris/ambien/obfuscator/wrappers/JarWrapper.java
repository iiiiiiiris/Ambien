package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.Crasher;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.DuplicateResources;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.Comment;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.FolderClasses;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

@SuppressWarnings("resource")
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

        if (!file.getName().endsWith(".jar"))
            throw new RuntimeException("Input jar isn't a jar file.");

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
        final File file = new File(path);
        if (!file.exists())
            throw new RuntimeException(String.format("Library jar \"%s\" file doesn't exist.", path));

        if (!file.getName().endsWith(".jar"))
            throw new RuntimeException(String.format("Library jar \"%s\" isn't a jar file.", path));

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
                if (Ambien.get.transformerManager.getTransformer("duplicate-resources").isEnabled() &&
                    DuplicateResources.dupResources.isEnabled()
                ) {
                    for (int x = 1; x <= DuplicateResources.dupAmount.getValue(); x++)
                        IOUtil.writeEntry(stream, name + "\u0000".repeat(x), IOUtil.duplicateData(bytes));
                }

                if (Ambien.get.transformerManager.getTransformer("folder-classes").isEnabled() &&
                    FolderClasses.folderResources.isEnabled()
                ) name += "/";

                IOUtil.writeEntry(stream, name, bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicVerifier());
        // Add classes
        classes.forEach(classWrapper -> {
            // Ignore library classes
            if (classWrapper.isLibraryClass()) return;

            classWrapper.getNode().methods.forEach(methodNode -> {
                try {
                    analyzer.analyzeAndComputeMaxs(methodNode.name,methodNode);
                } catch (AnalyzerException e) {
                    e.addSuppressed(new Throwable("Found exception ClassName: " + classWrapper.getNode().name + " MethodName:" + methodNode.name));
                }
            });

            try {
                String name = classWrapper.getName();
                if (Ambien.get.transformerManager.getTransformer("duplicate-resources").isEnabled() &&
                    DuplicateResources.dupClasses.isEnabled()
                ) {
                    for (int x = 1; x <= DuplicateResources.dupAmount.getValue(); x++)
                        IOUtil.writeEntry(stream, name + "\u0000".repeat(x), IOUtil.duplicateData(classWrapper.toByteArray()));
                }
                if (Ambien.get.transformerManager.getTransformer("folder-classes").isEnabled() &&
                    FolderClasses.folderClasses.isEnabled()
                ) name += "/";

                IOUtil.writeEntry(stream, name, classWrapper.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (Ambien.get.transformerManager.getTransformer("crasher").isEnabled() && Crasher.shitClasses.isEnabled()) {
            for (int i = 0; i < Crasher.shitAmount.getValue(); i++) {
                Crasher.addShitClasses(null, stream);
                Crasher.addShitClasses("META-INF/", stream);
            }
        }

        // Set zip comment
        if (Ambien.get.transformerManager.getTransformer("comment").isEnabled())
            stream.setComment(Comment.commentText.getValue());

        // Close stream
        stream.close();
    }
}
