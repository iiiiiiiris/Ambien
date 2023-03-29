package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.builders.ClassBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.jar.JarOutputStream;

/**
 * Adds a fake jar before the real jar
 * Most RE tools don't read backwards like the JVM, so they will read the fake jar
 */
@TransformerInfo(
        name = "red-herring",
        category = Category.PACKAGING,
        stability = Stability.STABLE,
        description = "Adds a fake jar before the real jar"
)
public class RedHerring extends Transformer {
    /**
     * Adds junk data instead of a class file, this will result in a smaller jar when using this transformer
     */
    public static final BooleanSetting corrupt = new BooleanSetting("corrupt", false);

    @Override
    public void transform(JarWrapper wrapper) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Write fake jar contents
        if (corrupt.isEnabled()) {
            // Write jar header to stream
            stream.write(0x50);
            stream.write(0x4B);
            stream.write(0x03);
            stream.write(0x04);

            // Get random bytes
            final Random rand = new Random();
            final byte[] bytes = new byte[MathUtil.randomInt(1, 25)];
            rand.nextBytes(bytes);

            // Write random bytes to stream
            try {
                stream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Create random class
            final ClassBuilder classBuilder = new ClassBuilder().setName("Ambien").setSuperName("Ambien").setAccess(ACC_PUBLIC);
            final ClassNode classNode = classBuilder.buildNode();

            // Write jar to separate stream
            final ByteArrayOutputStream jarBufferStream = new ByteArrayOutputStream();
            try (JarOutputStream jarOutputStream = new JarOutputStream(jarBufferStream)) {
                final ClassWrapper classWrapper = new ClassWrapper("Ambien.class", classNode, false);
                IOUtil.writeEntry(jarOutputStream, "Ambien.class", classWrapper.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Write buffer stream to main stream
            try {
                stream.write(jarBufferStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Add red herring stream to jar wrapper streams
        wrapper.getOutputStreams().add(stream);
    }
}
