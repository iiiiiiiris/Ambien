package me.iris.ambien.obfuscator.builders;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;

public final class InstructionModifier {
    private static final InsnList EMPTY_LIST = new InsnList();

    private final HashMap<AbstractInsnNode, InsnList> replacements = new HashMap<>();
    private final HashMap<AbstractInsnNode, InsnList> appends = new HashMap<>();
    private final HashMap<AbstractInsnNode, InsnList> prepends = new HashMap<>();

    public void append(AbstractInsnNode original, InsnList append) {
        appends.put(original, append);
    }

    public void prepend(AbstractInsnNode original, InsnList append) {
        prepends.put(original, append);
    }

    public void replace(AbstractInsnNode original, AbstractInsnNode... insns) {
        InsnList singleton = new InsnList();
        for (AbstractInsnNode replacement : insns) {
            singleton.add(replacement);
        }
        replacements.put(original, singleton);
    }

    public void replace(AbstractInsnNode original, InsnList replacements) {
        this.replacements.put(original, replacements);
    }

    public void remove(AbstractInsnNode original) {
        replacements.put(original, EMPTY_LIST);
    }

    public void removeAll(AbstractInsnNode... toRemove) {
        for (AbstractInsnNode insn : toRemove) {
            remove(insn);
        }
    }

    public void removeAll(List<AbstractInsnNode> toRemove) {
        for (AbstractInsnNode insn : toRemove) {
            remove(insn);
        }
    }

    public void apply(MethodNode methodNode) {
        replacements.forEach((insn, list) -> {
            methodNode.instructions.insertBefore(insn, list);
            methodNode.instructions.remove(insn);
        });
        prepends.forEach((insn, list) -> {
            methodNode.instructions.insertBefore(insn, list);
        });
        appends.forEach((insn, list) -> {
            methodNode.instructions.insert(insn, list);
        });
    }
}
