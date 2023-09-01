package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.colonial.BytecodeHelper;
import me.iris.ambien.obfuscator.colonial.NodeUtils;
import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@TransformerInfo(
        name = "colonial-string-encryption",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH,
        description = "Encrypts string using colonial's transformer."
)
public class ColonialStringEncryption extends Transformer {
    public final ListSetting stringBlacklist = new ListSetting("string-blacklist", new ArrayList<>());

    private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper)
                .stream()
                .filter(classWrapper -> !classWrapper.isEnum() && !classWrapper.isInterface())
                .forEach(classWrapper -> {
                    List<MethodWrapper> methods = classWrapper.getTransformableMethods();
                    classMethodsMap.put(classWrapper, methods);
                });

        classMethodsMap.forEach((classWrapper, methods) -> modify(classWrapper.getNode()));
    }

    private static String XOR(int i, int j, String string, int k, int l, char[] s) {
        StringBuilder sb = new StringBuilder();
        int i1 = 0;
        for(char c : string.toCharArray()) {
            sb.append((char)((((c ^ s[i1 % s.length]) ^ (i ^ k + i1)) ^ j) ^ l));
            i1++;
        }
        return sb.toString();
    }

    private static String EncryptKey(String s, char[] b) {
        final char[] charArray = s.toCharArray();
        final int i = charArray.length;
        for (int n = 0; i > n; ++n) {
            final int n2 = n;
            final char c = charArray[n2];
            char c2 = '\0';
            switch (n % 7) {
                case 0: {
                    c2 = b[0];
                    break;
                }
                case 1: {
                    c2 = b[1];
                    break;
                }
                case 2: {
                    c2 = b[2];
                    break;
                }
                case 3: {
                    c2 = b[3];
                    break;
                }
                case 4: {
                    c2 = b[4];
                    break;
                }
                case 5: {
                    c2 = b[5];
                    break;
                }
                default: {
                    c2 = b[6];
                    break;
                }
            }
            charArray[n2] = (char)(c ^ c2);
        }
        return new String(charArray).intern();
    }

    public void modify(ClassNode node) {
        if ((node.access & ACC_INTERFACE) != 0)
            return;
        try {
            if(node.methods != null && node.methods.size() > 0) {

                String key = StringUtil.genName(100);
                Random ran = new Random();
                char[] key2 = new char[] {(char) ran.nextInt(126), (char) ran.nextInt(126), (char) ran.nextInt(126)
                        , (char) ran.nextInt(126), (char) ran.nextInt(126), (char) ran.nextInt(126), (char) ran.nextInt(126)};

                //		String NAME2 = NameGen.colonial() + NameGen.String(2);
                String NAME3 = StringUtil.genName(40);

                {
                    FieldVisitor fieldVisitor = node.visitField(ACC_STATIC, NAME3, "[C", null, null);
                    fieldVisitor.visitEnd();
                }

                String name = StringUtil.genName(40);
                for(MethodNode mn : node.methods) {
                    BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldc -> {
                        if (ldc.cst instanceof String s) {
                            if (stringBlacklist.getOptions().contains(s)) return;
                            int k1 = new Random().nextInt();
                            int k2 = new Random().nextInt();
                            int k3 = new Random().nextInt();
                            int k4 = new Random().nextInt();
                            InsnList il = new InsnList();
                            il.add(new LdcInsnNode(k1));
                            il.add(new LdcInsnNode(k2));
                            il.add(new LdcInsnNode(XOR(k1, k2, s, k3, k4, key.toCharArray())));
                            il.add(new LdcInsnNode(k3));
                            il.add(new LdcInsnNode(k4));
                            il.add(new MethodInsnNode(INVOKESTATIC, node.name, name, "(IILjava/lang/String;II)Ljava/lang/String;", false));
                            mn.instructions.insert(ldc, il);
                            mn.instructions.remove(ldc);
                        }
                    });
                }
                {
                    MethodVisitor methodVisitor = node.visitMethod(ACC_PRIVATE | ACC_STATIC, name, "(IILjava/lang/String;II)Ljava/lang/String;", null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    methodVisitor.visitInsn(DUP);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                    methodVisitor.visitVarInsn(ASTORE, 5);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitInsn(ICONST_0);
                    methodVisitor.visitVarInsn(ISTORE, 6);
                    Label label2 = new Label();
                    methodVisitor.visitLabel(label2);
                    methodVisitor.visitVarInsn(ALOAD, 2);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
                    methodVisitor.visitInsn(DUP);
                    methodVisitor.visitVarInsn(ASTORE, 10);
                    methodVisitor.visitInsn(ARRAYLENGTH);
                    methodVisitor.visitVarInsn(ISTORE, 9);
                    methodVisitor.visitInsn(ICONST_0);
                    methodVisitor.visitVarInsn(ISTORE, 8);
                    Label label3 = new Label();
                    methodVisitor.visitJumpInsn(GOTO, label3);
                    Label label4 = new Label();
                    methodVisitor.visitLabel(label4);
                    methodVisitor.visitFrame(Opcodes.F_FULL, 11, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/StringBuilder", Opcodes.INTEGER, Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER, "[C"}, 0, new Object[] {});
                    methodVisitor.visitVarInsn(ALOAD, 10);
                    methodVisitor.visitVarInsn(ILOAD, 8);
                    methodVisitor.visitInsn(CALOAD);
                    methodVisitor.visitVarInsn(ISTORE, 7);
                    Label label5 = new Label();
                    methodVisitor.visitLabel(label5);
                    methodVisitor.visitVarInsn(ALOAD, 5);
                    methodVisitor.visitVarInsn(ILOAD, 7);
                    methodVisitor.visitFieldInsn(GETSTATIC, node.name, NAME3, "[C");
                    methodVisitor.visitVarInsn(ILOAD, 6);
                    methodVisitor.visitFieldInsn(GETSTATIC, node.name, NAME3, "[C");
                    methodVisitor.visitInsn(ARRAYLENGTH);
                    methodVisitor.visitInsn(IREM);
                    methodVisitor.visitInsn(CALOAD);
                    methodVisitor.visitInsn(IXOR);
                    methodVisitor.visitVarInsn(ILOAD, 0);
                    methodVisitor.visitVarInsn(ILOAD, 3);
                    methodVisitor.visitVarInsn(ILOAD, 6);
                    methodVisitor.visitInsn(IADD);
                    methodVisitor.visitInsn(IXOR);
                    methodVisitor.visitInsn(IXOR);
                    methodVisitor.visitVarInsn(ILOAD, 1);
                    methodVisitor.visitInsn(IXOR);
                    methodVisitor.visitVarInsn(ILOAD, 4);
                    methodVisitor.visitInsn(IXOR);
                    methodVisitor.visitInsn(I2C);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
                    methodVisitor.visitInsn(POP);
                    Label label6 = new Label();
                    methodVisitor.visitLabel(label6);
                    methodVisitor.visitIincInsn(6, 1);
                    Label label7 = new Label();
                    methodVisitor.visitLabel(label7);
                    methodVisitor.visitIincInsn(8, 1);
                    methodVisitor.visitLabel(label3);
                    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    methodVisitor.visitVarInsn(ILOAD, 8);
                    methodVisitor.visitVarInsn(ILOAD, 9);
                    methodVisitor.visitJumpInsn(IF_ICMPLT, label4);
                    Label label8 = new Label();
                    methodVisitor.visitLabel(label8);
                    methodVisitor.visitVarInsn(ALOAD, 5);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                    methodVisitor.visitInsn(ARETURN);
                    Label label9 = new Label();
                    methodVisitor.visitLabel(label9);
                    methodVisitor.visitMaxs(5, 11);
                    methodVisitor.visitEnd();
                }
                {
                    {
                        //MethodVisitor methodVisitor = node.visitMethod(ACC_PRIVATE | ACC_STATIC, NAME2, "()V", null, null);
                        MethodNode methodVisitor = new MethodNode();
                        methodVisitor.visitCode();
                        methodVisitor.visitLdcInsn(EncryptKey(key, key2));
                        methodVisitor.visitInsn(ICONST_M1);
                        Label label0 = new Label();
                        methodVisitor.visitJumpInsn(GOTO, label0);
                        Label label1 = new Label();
                        methodVisitor.visitLabel(label1);
                        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/String"});


                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
                        methodVisitor.visitFieldInsn(PUTSTATIC, node.name, NAME3, "[C");

                        //	methodVisitor.visitVarInsn(ASTORE, 1);

                        Label label2 = new Label();
                        methodVisitor.visitJumpInsn(GOTO, label2);
                        methodVisitor.visitLabel(label0);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 0, new Object[] {}, 2, new Object[] {"java/lang/String", Opcodes.INTEGER});
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
                        methodVisitor.visitInsn(DUP);
                        methodVisitor.visitInsn(ARRAYLENGTH);
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitInsn(ICONST_0);
                        methodVisitor.visitVarInsn(ISTORE, 0);
                        Label label3 = new Label();
                        methodVisitor.visitJumpInsn(GOTO, label3);
                        Label label4 = new Label();
                        methodVisitor.visitLabel(label4);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 3, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C"});
                        methodVisitor.visitInsn(DUP);
                        methodVisitor.visitVarInsn(ILOAD, 0);
                        Label label5 = new Label();
                        methodVisitor.visitLabel(label5);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 5, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER});
                        methodVisitor.visitInsn(DUP2);
                        methodVisitor.visitInsn(CALOAD);
                        methodVisitor.visitVarInsn(ILOAD, 0);
                        methodVisitor.visitIntInsn(BIPUSH, 7);
                        methodVisitor.visitInsn(IREM);
                        Label label6 = new Label();
                        Label label7 = new Label();
                        Label label8 = new Label();
                        Label label9 = new Label();
                        Label label10 = new Label();
                        Label label11 = new Label();
                        Label label12 = new Label();
                        methodVisitor.visitTableSwitchInsn(0, 5, label12, label6, label7, label8, label9, label10, label11);
                        methodVisitor.visitLabel(label6);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[0]);
                        Label label13 = new Label();
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label7);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[1]);
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label8);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[2]);
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label9);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[3]);
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label10);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[4]);
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label11);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[5]);
                        methodVisitor.visitJumpInsn(GOTO, label13);
                        methodVisitor.visitLabel(label12);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 6, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitIntInsn(BIPUSH, key2[6]);
                        methodVisitor.visitLabel(label13);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 7, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[C", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER});
                        methodVisitor.visitInsn(IXOR);
                        methodVisitor.visitInsn(I2C);
                        methodVisitor.visitInsn(CASTORE);
                        methodVisitor.visitIincInsn(0, 1);
                        methodVisitor.visitLabel(label3);
                        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {Opcodes.INTEGER}, 3, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "[C"});
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitInsn(DUP_X1);
                        methodVisitor.visitVarInsn(ILOAD, 0);
                        methodVisitor.visitJumpInsn(IF_ICMPGT, label4);
                        methodVisitor.visitTypeInsn(NEW, "java/lang/String");
                        methodVisitor.visitInsn(DUP_X1);
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false);
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitInsn(POP);
                        methodVisitor.visitInsn(SWAP);
                        methodVisitor.visitInsn(POP);
                        methodVisitor.visitJumpInsn(GOTO, label1);
                        methodVisitor.visitLabel(label2);
                        methodVisitor.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
                        //	methodVisitor.visitVarInsn(ALOAD, 1);
                        //	methodVisitor.visitInsn(RETURN);

                        MethodNode clInit = NodeUtils.getMethod(node, "<clinit>");
                        if (clInit == null) {
                            clInit = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, new String[0]);
                            node.methods.add(clInit);
                        }
                        if (clInit.instructions == null)
                            clInit.instructions = new InsnList();


                        if (clInit.instructions == null || clInit.instructions.getFirst() == null) {
                            clInit.instructions.add(methodVisitor.instructions);
                            clInit.instructions.add(new InsnNode(Opcodes.RETURN));
                        } else {
                            clInit.instructions.insertBefore(clInit.instructions.getFirst(), methodVisitor.instructions);
                        }


                    }
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
