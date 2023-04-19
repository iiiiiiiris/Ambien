package me.iris.ambien.obfuscator.asm;

import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.*;

public class SizeEvaluator implements Opcodes {
    public static final int MAX_SIZE = 0xFFFF; // 65535 bytes / 64kb

    /**
     * @param wrapper Owner method wrapper
     * @param list List of instructions that will be added
     * @return True if addition will be more than 64kb
     */
    public static boolean willOverflow(final MethodWrapper wrapper, final InsnList list) {
        return wrapper.getSize() + getInsnListSize(list) >= MAX_SIZE;
    }

    /**
     * @param node Owner method node
     * @param list List of instructions that will be added
     * @return True if addition will be more than 64kb
     */
    public static boolean willOverflow(final MethodNode node, final InsnList list) {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        node.accept(evaluator);
        return evaluator.getMaxSize() + getInsnListSize(list) >= MAX_SIZE;
    }

    /**
     * Sizes taken from CodeSizeEvaluator
     * Returns the max possible size
     *
     * @param list Instruction list
     * @return Size of instructions in bytes
     */
    private static int getInsnListSize(final InsnList list) {
        int size = 0;

        for (AbstractInsnNode insn : list) {
            final int opcode = insn.getOpcode();
            if (insn instanceof IntInsnNode) {
                if (opcode == SIPUSH)
                    size += 3;
                else
                    size += 2;
            } else if (insn instanceof VarInsnNode) {
                final VarInsnNode node = (VarInsnNode)insn;
                if (node.var < 4 && opcode != RET)
                    size += 1;
                else if (node.var >= 256)
                    size += 4;
                else
                    size += 2;
            } else if (insn instanceof TypeInsnNode || insn instanceof FieldInsnNode || insn instanceof LdcInsnNode)
                size += 3;
            else if (insn instanceof MethodInsnNode) {
                if (opcode == INVOKEINTERFACE)
                    size += 5;
                else
                    size += 3;
            } else if (insn instanceof InvokeDynamicInsnNode)
                size += 5;
            else if (insn instanceof JumpInsnNode) {
                if (opcode == GOTO || opcode == JSR)
                    size += 5;
                else
                    size += 8;
            } else if (insn instanceof IincInsnNode) {
                final IincInsnNode node = (IincInsnNode)insn;
                if (node.var > 255 || node.incr > 127 || node.incr < -128)
                    size += 6;
                else
                    size += 3;
            } else if (insn instanceof TableSwitchInsnNode) {
                final TableSwitchInsnNode node = (TableSwitchInsnNode)insn;
                size += 16 + node.labels.size() * 4;
            } else if (insn instanceof LookupSwitchInsnNode) {
                final LookupSwitchInsnNode node = (LookupSwitchInsnNode)insn;
                size += 12 + node.keys.size() * 8;
            } else if (insn instanceof MultiANewArrayInsnNode)
                size += 4;
            else
                size += 1;
        }

        return size;
    }
}
