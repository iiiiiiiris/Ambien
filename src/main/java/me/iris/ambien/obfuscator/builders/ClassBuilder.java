package me.iris.ambien.obfuscator.builders;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class ClassBuilder {
    private String name = null,
            superName = null,
            signature = null;
    private int access = -1,
            version = Opcodes.V1_8;

    public ClassBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public ClassBuilder setSuperName(final String superName) {
        this.superName = superName;
        return this;
    }

    public ClassBuilder setSignature(final String signature) {
        this.signature = signature;
        return this;
    }

    public ClassBuilder setAccess(final int access) {
        this.access = access;
        return this;
    }

    public ClassBuilder setVersion(final int version) {
        this.version = version;
        return this;
    }

    public ClassNode buildNode() {
        final ClassNode node = new ClassNode();
        node.version = version;
        node.name = name;
        node.superName = this.superName == null ? "java/lang/Object" : superName;
        node.signature = signature;
        node.access = access;
        return node;
    }
}
