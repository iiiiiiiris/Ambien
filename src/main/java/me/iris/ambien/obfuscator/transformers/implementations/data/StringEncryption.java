package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

@TransformerInfo(
        name = "string-encryption",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH
)
public class StringEncryption extends Transformer {
    /**
     * Encodes the encrypted string w/ base-64
     */
    public final BooleanSetting encode = new BooleanSetting("encode", false);

    private static final String DECRYPTION_DESCRIPTOR = "(Ljava/lang/String;I)Ljava/lang/String;";

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            if (classWrapper.isInterface()) return;

            classWrapper.getTransformableMethods().forEach(methodNode -> {
                // ignore empty methods
                if (methodNode.instructions == null || methodNode.instructions.size() == 0) return;

                // counter for adding locals
                final AtomicInteger localCounter = new AtomicInteger(methodNode.maxLocals);

                // Find strings to encrypt
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == LDC && insn instanceof LdcInsnNode)
                        .map(insn -> (LdcInsnNode)insn)
                        .forEach(ldc -> {
                            if (!(ldc.cst instanceof String)) return;

                            // Generate key
                            final int key = MathUtil.randomInt(1, Short.MAX_VALUE);

                            // Encrypt string w/ basic shitty xor & encode string if enabled
                            String encryptedStr = encrypt((String)ldc.cst, key);
                            if (encode.isEnabled())
                                encryptedStr = encode(encryptedStr);

                            // Get bytes of encrypted string
                            final byte[] bytes = encryptedStr.getBytes();

                            // Build decryption method
                            final MethodNode decryptorMethod = buildDecryptor();
                            classWrapper.addMethod(decryptorMethod);

                            // debugging
                            Ambien.LOGGER.debug("encrypting \"{}\" with key {}", ldc.cst, key);

                            final InsnList list = new InsnList();

                            // Create new byte array
                            list.add(new IntInsnNode(BIPUSH, bytes.length));
                            list.add(new IntInsnNode(NEWARRAY, T_BYTE));

                            // Add bytes to array
                            for (int i = 0; i < bytes.length; i++) {
                                final byte b = bytes[i];
                                list.add(new InsnNode(DUP));
                                list.add(new IntInsnNode(BIPUSH, i));
                                list.add(new IntInsnNode(BIPUSH, b));
                                list.add(new InsnNode(BASTORE));
                            }

                            // Store byte array
                            final int byteArrayIdx = localCounter.incrementAndGet();
                            list.add(new VarInsnNode(ASTORE, byteArrayIdx));

                            // Create new string w/ byte array
                            list.add(new TypeInsnNode(NEW, "java/lang/String"));
                            list.add(new InsnNode(DUP));
                            list.add(new VarInsnNode(ALOAD, byteArrayIdx));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false));

                            // Add call to decrypt method w/ key
                            list.add(new LdcInsnNode(key));
                            list.add(new MethodInsnNode(INVOKESTATIC, classWrapper.getNode().name,
                                    decryptorMethod.name, DECRYPTION_DESCRIPTOR, false));

                            methodNode.instructions.insertBefore(ldc, list);
                            methodNode.instructions.remove(ldc);
                        });
            });
        });
    }

    private String encrypt(final String str, final int key) {
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= key;
        }

        return new String(chars);
    }

    private String encode(final String str) {
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    private MethodNode buildDecryptor() {
        final MethodBuilder builder = new MethodBuilder()
                .setName(StringUtil.randomString(MathUtil.randomInt(15, 50)))
                .setAccess(ACC_PUBLIC | ACC_STATIC)
                .setDesc(DECRYPTION_DESCRIPTOR);
        final MethodNode node = builder.buildNode();

        // added comments to every instruction because it's easier to read at a quick glance (im retarded)
        // codes kinda messy
        node.visitCode(); {
            if (encode.isEnabled()) {
                final Label labelA = new Label(),
                        labelB = new Label(),
                        labelC = new Label(),
                        labelD = new Label(),
                        labelE = new Label(),
                        labelF = new Label(),
                        labelG = new Label();

                node.visitLabel(labelA);
                node.visitTypeInsn(NEW, "java/lang/String"); // create new string
                node.visitInsn(DUP); // duplicate stack
                node.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false); // get decoder
                node.visitVarInsn(ALOAD, 0); // load encoded string
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false); // get bytes of encoded string
                node.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false); // decode string
                node.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false); // initialize string w/ decoded value
                node.visitVarInsn(ASTORE, 2); // store decoded string

                node.visitLabel(labelB);
                node.visitTypeInsn(NEW, "java/lang/StringBuilder"); // create new string builder
                node.visitInsn(DUP); // duplicate stack
                node.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false); // initialize string builder
                node.visitVarInsn(ASTORE, 3); // store string builder

                node.visitLabel(labelC);
                node.visitInsn(ICONST_0); // push 0 to stack
                node.visitVarInsn(ISTORE, 4); // store 0 at 4 (index in loop)

                node.visitLabel(labelD);
                node.visitVarInsn(ILOAD, 4); // load loop index
                node.visitVarInsn(ALOAD, 2); // load decoded string
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false); // get length of decoded string
                node.visitJumpInsn(IF_ICMPGE, labelG); // goto label g if the loop index equals the length of the decoded string

                node.visitLabel(labelE);
                node.visitVarInsn(ALOAD, 3); // load string builder
                node.visitVarInsn(ALOAD, 2); // load decoded string
                node.visitVarInsn(ILOAD, 4); // load loop index
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false); // get char of decoded string at loop index
                node.visitVarInsn(ILOAD, 1); // load xor key
                node.visitInsn(IXOR); // xor operation
                node.visitInsn(I2C); // convert int to char
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false); // append char to the string builder
                node.visitInsn(POP); // clear stack

                node.visitLabel(labelF);
                node.visitIincInsn(4, 1); // increment loop index
                node.visitJumpInsn(GOTO, labelD); // continue loop

                node.visitLabel(labelG);
                node.visitVarInsn(ALOAD, 3); // load string builder
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false); // convert string builder to a string
                node.visitInsn(ARETURN); // return string
            } else {
                final Label labelA = new Label(),
                        labelB = new Label(),
                        labelC = new Label(),
                        labelD = new Label(),
                        labelE = new Label(),
                        labelF = new Label();

                node.visitLabel(labelA);
                node.visitLineNumber(34, labelA);
                node.visitTypeInsn(NEW, "java/lang/StringBuilder"); // create new string builder
                node.visitInsn(DUP); // duplicate stack
                node.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false); // initialize string builder
                node.visitVarInsn(ASTORE, 2); // store stringbuilder

                node.visitLabel(labelB);
                node.visitLineNumber(38, labelB);
                node.visitInsn(ICONST_0); // push 0 to stack
                node.visitVarInsn(ISTORE, 3); // store 0 at 3 (index in loop)

                node.visitLabel(labelC);
                node.visitVarInsn(ILOAD, 3); // load loop index
                node.visitVarInsn(ALOAD, 0); // load encrypted string
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false); // get length of encrypted string
                node.visitJumpInsn(IF_ICMPGE, labelF); // go to label f if the loop index equals the length of the encrypted string

                node.visitLabel(labelD);
                node.visitLineNumber(39, labelD);
                node.visitVarInsn(ALOAD, 2); // load string builder
                node.visitVarInsn(ALOAD, 0); // load encrypted string
                node.visitVarInsn(ILOAD, 3); // load loop index
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false); // get char of encrypted string at loop index
                node.visitVarInsn(ILOAD, 1); // load xor key
                node.visitInsn(IXOR); // xor operation
                node.visitInsn(I2C); // convert int to char
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false); // append char to string builder
                node.visitInsn(POP); // clear stack

                node.visitLabel(labelE);
                node.visitLineNumber(38, labelE);
                node.visitIincInsn(3, 1); // increment loop index
                node.visitJumpInsn(GOTO, labelC); // continue loop

                node.visitLabel(labelF);
                node.visitLineNumber(42, labelF);
                node.visitVarInsn(ALOAD, 2); // load string builder
                node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false); // convert string builder to a string
                node.visitInsn(ARETURN); // return string
            }
        } node.visitEnd();

        return node;
    }
}
