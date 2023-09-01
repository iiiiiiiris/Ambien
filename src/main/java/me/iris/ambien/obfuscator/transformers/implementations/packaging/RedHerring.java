package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.ClassBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    List<String> messagesList = new ArrayList<>(List.of(
            "своего безглазого парнокопытного деда декомпиль, бездарность | you_need_to_train_more"
    ));
    public static BooleanSetting corrupt = new BooleanSetting("corrupt", false);
    public static StringSetting className = new StringSetting("class-name", "Main");
    public final ListSetting watermark = new ListSetting("text", messagesList);

    @Override
    public void transform(JarWrapper wrapper) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (corrupt.isEnabled()) {
            stream.write(0x50);
            stream.write(0x4B);
            stream.write(0x03);
            stream.write(0x04);
            final Random rand = new Random();
            final byte[] bytes = new byte[MathUtil.randomInt(1, 25)];
            rand.nextBytes(bytes);
            try {
                stream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String name = className.getValue();
            final ClassBuilder classBuilder = new ClassBuilder().setName(name).setSuperName(name).setAccess(ACC_PUBLIC).setVersion(V1_5);
            final ClassNode classNode = classBuilder.buildNode();

            if (!watermark.getOptions().get(0).equals("")) {
                List<String> inputList = watermark.getOptions();
                String[][] messages = new String[inputList.size()][2];

                for (int i = 0; i < inputList.size(); i++) {
                    String[] parts = inputList.get(i).split("\\s*\\|\\s*", -1);
                    messages[i][0] = parts[0].trim(); // Текст до |
                    messages[i][1] = parts[1].trim(); // Текст после |
                }

                for (String[] messageData : messages) {
                    FieldNode fieldNode = new FieldNode(ACC_STATIC, messageData[1], "Ljava/lang/String;", null, null);
                    classNode.fields.add(fieldNode);
                }

                MethodNode clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                InsnList clinitInstructions = clinit.instructions;

                for (String[] messageData : messages) {
                    clinitInstructions.add(new LdcInsnNode(messageData[0]));
                    clinitInstructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, name, messageData[1], "Ljava/lang/String;"));
                }

                clinitInstructions.add(new InsnNode(Opcodes.RETURN));
                classNode.methods.add(clinit);
            }

            final ByteArrayOutputStream jarBufferStream = new ByteArrayOutputStream();
            try (JarOutputStream jarOutputStream = new JarOutputStream(jarBufferStream)) {
                final ClassWrapper classWrapper = new ClassWrapper(name + ".class", classNode, false);
                IOUtil.writeEntry(jarOutputStream, name + ".class", classWrapper.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
