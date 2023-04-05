package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.stream.Stream;

public class MethodWrapper {
    @Getter
    private final MethodNode node;

    public MethodWrapper(final MethodNode node) {
        this.node = node;
    }

    public boolean isInitializer() {
        return node.name.equals("<init>") || node.name.equals("<clinit>");
    }

    public int getSize() {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        node.accept(evaluator);
        return evaluator.getMaxSize();
    }

    public boolean hasInstructions() {
        return node.instructions != null && node.instructions.size() > 0;
    }

    public Stream<AbstractInsnNode> getInstructions() {
        return Arrays.stream(node.instructions.toArray());
    }

    public InsnList getInstructionsList() {
        return node.instructions;
    }

    public void replaceInstruction(final AbstractInsnNode insn, final InsnList replacement) {
        node.instructions.insertBefore(insn, replacement);
        node.instructions.remove(insn);
    }

    public void replaceInstruction(final AbstractInsnNode insn, final AbstractInsnNode replacementInsn) {
        node.instructions.insertBefore(insn, replacementInsn);
        node.instructions.remove(insn);
    }

    public boolean hasLocalVariables() {
        return node.localVariables != null && !node.localVariables.isEmpty();
    }
}
