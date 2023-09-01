package me.iris.ambien.obfuscator.myj2c;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Arrays;

public class InsnBuilder {

    private final InsnList insnList;

    private InsnBuilder() {
        this.insnList = new InsnList();
    }

    public InsnBuilder insn(AbstractInsnNode... insnNodes) {
        Arrays.stream(insnNodes).forEach(this.insnList::add);
        return this;
    }

    public InsnList getInsnList() {
        return insnList;
    }

    public static InsnBuilder createEmpty() {
        return new InsnBuilder();
    }
}