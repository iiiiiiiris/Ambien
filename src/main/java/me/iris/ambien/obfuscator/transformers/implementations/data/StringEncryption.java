package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.builders.FieldBuilder;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@TransformerInfo(
        name = "string-encryption",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH
)
public class StringEncryption extends Transformer {
    // TODO: Randomize descriptor argument order & add decoy args
    private static final String DECRYPTOR_DESCRIPTOR = "(Ljava/lang/String;II)Ljava/lang/String;";

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            if (classWrapper.isInterface()) return;

            // TODO: Encrypt string fields

            classWrapper.getTransformableMethods().forEach(methodNode -> {
                // ignore empty methods
                if (methodNode.instructions == null || methodNode.instructions.size() == 0) return;

                // counter for adding locals
                final AtomicInteger localCounter = new AtomicInteger(methodNode.maxLocals);

                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == LDC && insn instanceof LdcInsnNode)
                        .map(insn -> (LdcInsnNode)insn)
                        .forEach(ldc -> {
                            if (!(ldc.cst instanceof String)) return;

                            // Generate encryption keys
                            final int[] keys = MathUtil.getTwoRandomInts(1, Short.MAX_VALUE);

                            // Encrypt string
                            final String encryptedLDC = encrypt((String)ldc.cst, keys[0], keys[1]);
                            ldc.cst = encryptedLDC;

                            // Build & add decryptor method
                            final MethodNode decryptorNode = buildDecryptor();
                            classWrapper.addMethod(decryptorNode);

                            // Add decryption routine
                            final InsnList list = new InsnList();
                            list.add(buildDecryptionRoutine(classWrapper, decryptorNode, encryptedLDC, keys[0], keys[1], localCounter));

                            methodNode.instructions.insertBefore(ldc, list);
                            methodNode.instructions.remove(ldc);
                        });
            });
        });
    }

    private String encrypt(String str, int key1, int randSeed) {
        // Create a random instance with a specific seed
        final Random rand = new Random(randSeed);

        // Generate a random int in range (1-MAX_SHORT_VAL)
        final int key2 = 1 + (rand.nextInt(Short.MAX_VALUE));

        // Xor keys for a single key
        final int key = key1 ^ key2;

        // Apply xor to string
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= key;
        }

        // Encode string w/ base64
        final String encryptedString = new String(chars);
        final byte[] bytes = encryptedString.getBytes();
        return new String(Base64.getEncoder().encode(bytes));
    }

    private InsnList buildDecryptionRoutine(ClassWrapper classWrapper, final MethodNode decryptionMethod,
                                            String encryptedString, int key1, int key2, final AtomicInteger localCounter) {
        // Add first key as a field
        final String keyFieldName = StringUtil.randomString(MathUtil.randomInt(10, 50));
        final FieldBuilder fieldBuilder = new FieldBuilder()
                .setName(keyFieldName)
                .setDesc("I")
                .setValue(key1)
                .setAccess(ACC_PRIVATE | ACC_STATIC);
        final FieldNode fieldNode = fieldBuilder.buildNode();
        classWrapper.addField(fieldNode);

        // List for our decryption routine
        final InsnList list = new InsnList();

        // Convert encrypted string into byte array
        final byte[] bytes = encryptedString.getBytes();

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

        // Get the first decryption key
        list.add(new FieldInsnNode(GETSTATIC, classWrapper.getNode().name, keyFieldName, "I"));

        // Push second decryption key onto stack]
        list.add(new LdcInsnNode(key2));

        // Add decryptor method
        list.add(new MethodInsnNode(INVOKESTATIC, classWrapper.getNode().name, decryptionMethod.name, decryptionMethod.desc, false));

        return list;
    }

    private MethodNode buildDecryptor() {
        final MethodBuilder builder = new MethodBuilder()
                .setName(StringUtil.randomString(MathUtil.randomInt(10, 50)))
                .setAccess(ACC_PRIVATE | ACC_STATIC)
                .setDesc(DECRYPTOR_DESCRIPTOR);
        final MethodNode node = builder.buildNode();

        node.visitCode(); {
            // labels
            final Label labelA = new Label(),
                    labelB = new Label(),
                    labelC = new Label(),
                    labelD = new Label(),
                    labelE = new Label(),
                    labelF = new Label(),
                    labelG = new Label(),
                    labelH = new Label(),
                    labelI = new Label(),
                    labelJ = new Label();

            // Create new random instance
            node.visitLabel(labelA);
            node.visitLineNumber(50, labelA);
            node.visitTypeInsn(NEW, "java/util/Random"); // new random class
            node.visitInsn(DUP); // duplicate stack
            node.visitVarInsn(ILOAD, 2); // load rand seed
            node.visitInsn(I2L); // int to long
            node.visitMethodInsn(INVOKESPECIAL, "java/util/Random", "<init>", "(J)V", false); // initialize random w/ seed
            node.visitVarInsn(ASTORE, 3); // store random instance @ index 3

            // Generate "random" key
            node.visitLabel(labelB);
            node.visitLineNumber(51, labelB);
            node.visitInsn(ICONST_0); // push 0
            node.visitInsn(ICONST_1); // push 1
            node.visitInsn(IXOR); // xor 0 w/ 1 (a ^= 1) a(ICONST_0) will become 1, cooler way of doing ICONST_1 on its own :))
            node.visitVarInsn(ALOAD, 3); // load random instance
            node.visitIntInsn(SIPUSH, 32767); // push 32767 onto stack (as a short)
            node.visitMethodInsn(INVOKEVIRTUAL, "java/util/Random", "nextInt", "(I)I", false); // get next int
            node.visitInsn(IADD); // add 1 & next int value
            node.visitVarInsn(ISTORE, 4); // store second key @ index 4

            // Generate a final key from key1 & "random" second key
            node.visitLabel(labelC);
            node.visitLineNumber(52, labelC);
            node.visitVarInsn(ILOAD, 1); // load key1
            node.visitVarInsn(ILOAD, 4); // load second "random" key
            node.visitInsn(IXOR); // perform xor operation on keys
            node.visitVarInsn(ISTORE, 5); // store final key @ index 5

            // decode encoded string
            node.visitLabel(labelD);
            node.visitLineNumber(54, labelD);
            node.visitTypeInsn(NEW, "java/lang/String"); // create new string
            node.visitInsn(DUP); // duplicate stack
            node.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false); // get decoder
            node.visitVarInsn(ALOAD, 0); // load encoded string
            node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false); // get bytes of string
            node.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false); // decode string
            node.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false); // initialize new string w/ decoded string
            node.visitVarInsn(ASTORE, 6); // store decoded string @ index 6

            // convert decoded string into a char array
            node.visitLabel(labelE);
            node.visitLineNumber(55, labelE);
            node.visitVarInsn(ALOAD, 6); // load decoded string
            node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false); // get chars
            node.visitVarInsn(ASTORE, 7); // store chars @ index 7

            // create & store index for looping through chars
            node.visitLabel(labelF);
            node.visitLineNumber(56, labelF);
            node.visitInsn(ICONST_0);
            node.visitVarInsn(ISTORE, 8); // store loop index @ index 8

            // check if loop index > char array length
            node.visitLabel(labelG);
            node.visitVarInsn(ILOAD, 8); // load loop index
            node.visitVarInsn(ALOAD, 7); // load char array
            node.visitInsn(ARRAYLENGTH); // get length of array
            node.visitJumpInsn(IF_ICMPGE, labelJ); // goto label j if they're the same

            // xor char @ index of the loop
            node.visitLabel(labelH);
            node.visitLineNumber(57, labelH);
            node.visitVarInsn(ALOAD, 7); // load char array
            node.visitVarInsn(ILOAD, 8); // load loop index
            node.visitInsn(DUP2); // duplicate these 2 (^) variables
            node.visitInsn(CALOAD); // load char @ index
            node.visitVarInsn(ILOAD, 5); // load final key
            node.visitInsn(IXOR); // xor operation
            node.visitInsn(I2C); // revert xor'd char back to a char
            node.visitInsn(CASTORE); // store xor'd char in array

            // increment loop index
            node.visitLabel(labelI);
            node.visitLineNumber(56, labelI);
            node.visitIincInsn(8, 1); // increment loop index (at index 8) by 1
            node.visitJumpInsn(GOTO, labelG); // jump back to start of loop

            // return char array as a new string
            node.visitLabel(labelJ);
            node.visitLineNumber(60, labelJ);
            node.visitTypeInsn(NEW, "java/lang/String"); // new string
            node.visitInsn(DUP); // duplicate stack
            node.visitVarInsn(ALOAD, 7); // load char array
            node.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false); // initialize string
            node.visitInsn(ARETURN);
        } node.visitEnd();

        return node;
    }
}
