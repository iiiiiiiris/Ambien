package me.iris.ambien.obfuscator.utilities;

import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ASMUtils {

    public static boolean isString(AbstractInsnNode node) {
        return node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof String;
    }

    public static String getString(AbstractInsnNode node) {
        return ((LdcInsnNode) node).cst.toString();
    }

    public static Number getNumber(AbstractInsnNode node) {
        if (node instanceof LdcInsnNode) {
            return (Number) ((LdcInsnNode) node).cst;
        } else if (node instanceof IntInsnNode) {
            int opcode = node.getOpcode();
            if (opcode == Opcodes.SIPUSH) {
                return (short) ((IntInsnNode) node).operand;
            } else if (opcode == Opcodes.BIPUSH) {
                return (byte) ((IntInsnNode) node).operand;
            }
        } else {
            int opcode = node.getOpcode();
            switch (opcode) {
                case Opcodes.ICONST_M1:
                    return -1;
                case Opcodes.ICONST_0:
                    return 0;
                case Opcodes.ICONST_1:
                    return 1;
                case Opcodes.ICONST_2:
                    return 2;
                case Opcodes.ICONST_3:
                    return 3;
                case Opcodes.ICONST_4:
                    return 4;
                case Opcodes.ICONST_5:
                    return 5;
                case Opcodes.LCONST_0:
                    return 0L;
                case Opcodes.LCONST_1:
                    return 1L;
                case Opcodes.FCONST_0:
                    return 0.0F;
                case Opcodes.FCONST_1:
                    return 1.0F;
                case Opcodes.FCONST_2:
                    return 2.0F;
                case Opcodes.DCONST_0:
                    return 0.0;
                case Opcodes.DCONST_1:
                    return 1.0;
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean isNumber(AbstractInsnNode node) {
        int opcode = node.getOpcode();
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.SIPUSH) {
            return true;
        }

        if (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Number) {
            return true;
        }

        return false;
    }

    public static AbstractInsnNode createNumberNode(int value) {
        int opcode = getNumberOpcode(value);
        switch (opcode) {
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
                return new InsnNode(opcode);
            default:
                if (value >= -128 && value <= 127) {
                    return new IntInsnNode(Opcodes.BIPUSH, value);
                }
                if (value >= -32768 && value <= 32767) {
                    return new IntInsnNode(Opcodes.SIPUSH, value);
                }
                return new LdcInsnNode(value);
        }
    }

    public static AbstractInsnNode createNumberNode(short value) {
        switch (value) {
            case -1:
                return new InsnNode(Opcodes.ICONST_M1);
            case 0:
                return new InsnNode(Opcodes.ICONST_0);
            case 1:
                return new InsnNode(Opcodes.ICONST_1);
            case 2:
                return new InsnNode(Opcodes.ICONST_2);
            case 3:
                return new InsnNode(Opcodes.ICONST_3);
            case 4:
                return new InsnNode(Opcodes.ICONST_4);
            case 5:
                return new InsnNode(Opcodes.ICONST_5);
            default:
                if (value >= -128 && value <= 127) {
                    return new IntInsnNode(Opcodes.BIPUSH, value);
                }
                return new IntInsnNode(Opcodes.SIPUSH, value);
        }
    }

    public static AbstractInsnNode createNumberNode(byte value) {
        switch (value) {
            case -1:
                return new InsnNode(Opcodes.ICONST_M1);
            case 0:
                return new InsnNode(Opcodes.ICONST_0);
            case 1:
                return new InsnNode(Opcodes.ICONST_1);
            case 2:
                return new InsnNode(Opcodes.ICONST_2);
            case 3:
                return new InsnNode(Opcodes.ICONST_3);
            case 4:
                return new InsnNode(Opcodes.ICONST_4);
            case 5:
                return new InsnNode(Opcodes.ICONST_5);
            default:
                return new IntInsnNode(Opcodes.BIPUSH, value);
        }
    }

    public static int getNumberOpcode(int value) {
        switch (value) {
            case -1:
                return Opcodes.ICONST_M1;
            case 0:
                return Opcodes.ICONST_0;
            case 1:
                return Opcodes.ICONST_1;
            case 2:
                return Opcodes.ICONST_2;
            case 3:
                return Opcodes.ICONST_3;
            case 4:
                return Opcodes.ICONST_4;
            case 5:
                return Opcodes.ICONST_5;
            default:
                if (value >= -128 && value <= 127) {
                    return Opcodes.BIPUSH;
                }
                return (value >= -32768 && value <= 32767) ? Opcodes.SIPUSH : Opcodes.LDC;
        }
    }

    public static MethodNode getClinitMethodNode(ClassNode node) {
        return getMethodNode(node, "<clinit>");
    }

    public static MethodNode getInitMethodNode(ClassNode node) {
        MethodNode methodNode = getMethodNode(node, "<init>");

        if (methodNode == null) {
            System.err.println("WTF?! " + node.name + " doesn't have an init method??!");
            methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodNode.visitCode();
            methodNode.visitVarInsn(Opcodes.ALOAD, 0);
            methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodNode.visitInsn(Opcodes.RETURN);
            methodNode.visitEnd();
        }

        return methodNode;
    }

    public static MethodNode getMethodNode(ClassNode node, String methodName) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public static MethodNode getClinitMethodNodeOrCreateNew(ClassNode node) {
        MethodNode method = getMethodNode(node, "<clinit>");

        if (method == null) {
            method = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            node.methods.add(method);
        }

        return method;
    }

    public static void computeMaxLocals(MethodNode method) {
        int maxLocals = Type.getArgumentsAndReturnSizes(method.desc) >> 2;

        for (AbstractInsnNode node : method.instructions) {
            if (node instanceof VarInsnNode) {
                int local = ((VarInsnNode) node).var;
                int size = (node.getOpcode() == Opcodes.LLOAD ||
                        node.getOpcode() == Opcodes.DLOAD ||
                        node.getOpcode() == Opcodes.LSTORE ||
                        node.getOpcode() == Opcodes.DSTORE) ? 2 : 1;
                maxLocals = Math.max(maxLocals, local + size);
            } else if (node instanceof IincInsnNode) {
                int local = ((IincInsnNode) node).var;
                maxLocals = Math.max(maxLocals, local + 1);
            }
        }

        method.maxLocals = maxLocals;
    }
}
