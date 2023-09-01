package me.iris.ambien.obfuscator.mosey;

import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public final class LightStringEncryptionIntensity {

    private static final Map<String, String> lookup = new HashMap<>();

    public static void accept(ClassWrapper classWrapper) {
        FieldNode fieldNode = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, StringUtil.genName(16), "Ljava/util/Map;", null, null);
        MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, StringUtil.genName(16), "(Ljava/lang/String;II)Ljava/lang/String;", null, null);

        lookup.put(classWrapper.getNode().name, methodNode.name);

        FieldInsnNode getField = new FieldInsnNode(Opcodes.GETSTATIC, classWrapper.getNode().name, fieldNode.name, fieldNode.desc);

        MethodInsnNode append = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");

        int stringLocal = 0;
        int key1Local = 1;
        int key2Local = 2;
        int mapLocal = 3;
        int compoundLocal = 4;
        int cachedLocal = 5;
        int charArray = 6;
        int loop = 7;

        LabelNode decrypt = new LabelNode();
        LabelNode startLoop = new LabelNode();
        LabelNode finishLoop = new LabelNode();

        InsnList instructions = new InsnList();
        instructions.add(getField);
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, mapLocal));
        instructions.add(new InsnNode(Opcodes.MONITORENTER)); // synchronized (map)

        instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, stringLocal));
        instructions.add(append);
        instructions.add(new VarInsnNode(Opcodes.ILOAD, key1Local));
        instructions.add(append);
        instructions.add(new VarInsnNode(Opcodes.ILOAD, key2Local));
        instructions.add(append);
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, compoundLocal)); // String compound = string + key1 + key2;

        instructions.add(getField);
        instructions.add(new VarInsnNode(Opcodes.ALOAD, compoundLocal));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/String"));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, cachedLocal)); // String cached = map.get(compound);

        instructions.add(new VarInsnNode(Opcodes.ALOAD, cachedLocal));
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, decrypt));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, cachedLocal));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, mapLocal));
        instructions.add(new InsnNode(Opcodes.MONITOREXIT)); // free the map from the synchronized block
        instructions.add(new InsnNode(Opcodes.ARETURN)); // if (cached != null) return cached;

        instructions.add(decrypt);
        instructions.add(new VarInsnNode(Opcodes.ALOAD, stringLocal));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C"));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, charArray)); // char[] chars = string.toCharArray();

        instructions.add(new InsnNode(Opcodes.ICONST_0));
        instructions.add(new VarInsnNode(Opcodes.ISTORE, loop));
        instructions.add(startLoop);
        instructions.add(new VarInsnNode(Opcodes.ILOAD, loop));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, charArray));
        instructions.add(new InsnNode(Opcodes.ARRAYLENGTH));
        instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, finishLoop)); // for (int i = 0; i < chars.length; i++)

        instructions.add(new VarInsnNode(Opcodes.ALOAD, charArray));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, loop));
        instructions.add(new InsnNode(Opcodes.CALOAD));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, key1Local));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, key2Local));
        instructions.add(new InsnNode(Opcodes.IXOR));
        instructions.add(new InsnNode(Opcodes.ICONST_M1));
        instructions.add(new InsnNode(Opcodes.IXOR));
        instructions.add(new InsnNode(Opcodes.IXOR));
        instructions.add(new InsnNode(Opcodes.I2C));
        instructions.add(new InsnNode(Opcodes.CASTORE)); // chars[i] = (char) (chars[i] ^ ~(key1 ^ key2));

        instructions.add(new IincInsnNode(loop, 1));
        instructions.add(new JumpInsnNode(Opcodes.GOTO, startLoop)); // i++;

        instructions.add(finishLoop);
        instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String"));
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, charArray));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([C)V"));
        instructions.add(new VarInsnNode(Opcodes.ASTORE, charArray)); // String result = new String(chars);

        instructions.add(getField);
        instructions.add(new VarInsnNode(Opcodes.ALOAD, compoundLocal));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, charArray));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
        instructions.add(new InsnNode(Opcodes.POP)); // map.put(compound, result);

        instructions.add(new VarInsnNode(Opcodes.ALOAD, charArray));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, mapLocal));
        instructions.add(new InsnNode(Opcodes.MONITOREXIT)); // free map from synchronized block
        instructions.add(new InsnNode(Opcodes.ARETURN)); // return result;

        methodNode.instructions = instructions;
        classWrapper.addMethod(methodNode);
        classWrapper.addField(fieldNode);

        classWrapper.getStaticInitializer().instructions.insert(new InsnList() {
            {
                add(new TypeInsnNode(Opcodes.NEW, "java/util/HashMap"));
                add(new InsnNode(Opcodes.DUP));
                add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V"));
                add(new FieldInsnNode(Opcodes.PUTSTATIC, classWrapper.getNode().name, fieldNode.name, fieldNode.desc));
            }
        });
    }

    public static InsnList encrypt(LdcInsnNode ldcInsnNode, ClassWrapper classWrapper) {
        String string = (String) ldcInsnNode.cst;
        int key1 = MathUtil.randomInt(100000, 1000000);
        int key2 = MathUtil.randomInt(100000, 1000000);
        String encrypted = encryptString(string, key1, key2);
        String decryptMethod = lookup.get(classWrapper.getNode().name);

        InsnList instructions = new InsnList();
        instructions.add(new LdcInsnNode(encrypted));
        instructions.add(getOptimizedInt(key2));
        instructions.add(getOptimizedInt(key1));
        instructions.add(new InsnNode(Opcodes.SWAP));
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new InsnNode(Opcodes.POP));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classWrapper.getNode().name, decryptMethod, "(Ljava/lang/String;II)Ljava/lang/String;"));
        return instructions;
    }

    private static String encryptString(String string, int key1, int key2) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ ~(key1 ^ key2));
        }
        return new String(chars);
    }

    public static AbstractInsnNode getOptimizedInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(toConst(value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    private static int toConst(int value) {
        int opcode = value + 3;
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) {
            return opcode;
        }
        throw new IllegalArgumentException(String.format("Value %d can't be converted to a constant opcode.", value));
    }
}
