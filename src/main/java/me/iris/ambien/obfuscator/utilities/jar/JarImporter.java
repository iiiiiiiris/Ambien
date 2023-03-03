package me.iris.ambien.obfuscator.utilities.jar;

import lombok.experimental.UtilityClass;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@UtilityClass
public class JarImporter {
    public JarWrapper importJar(final File file) throws IOException {
        // Convert file to jar file
        final JarFile jarFile = new JarFile(file);

        // Get jar file entries
        final Enumeration<JarEntry> entries = jarFile.entries();

        // Wrapper all the files inside the jar will be stored in
        final JarWrapper wrapper = new JarWrapper();

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

                wrapper.getClasses().add(new ClassWrapper(name, node));
                Ambien.LOGGER.info("Loaded class: {}", name);
            } else if (name.endsWith("/"))
                wrapper.getDirectories().add(name);
            else {
                final byte[] bytes = IOUtil.streamToArray(stream);
                wrapper.getResources().put(name, bytes);
                Ambien.LOGGER.info("Loaded resource: {}", name);
            }
        }

        // Return wrapper
        return wrapper;
    }
}
