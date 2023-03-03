package me.iris.ambien.obfuscator.builders;

import org.objectweb.asm.tree.FieldNode;

public class FieldBuilder {
    private int access;
    private String name, desc, signature;
    private Object value;

    public FieldBuilder setAccess(final int access) {
        this.access = access;
        return this;
    }

    public FieldBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public FieldBuilder setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    public FieldBuilder setSignature(final String signature) {
        this.signature = signature;
        return this;
    }

    public FieldBuilder setValue(final Object value) {
        this.value = value;
        return this;
    }

    public FieldNode buildNode() {
        return new FieldNode(access, name, desc, signature, value);
    }
}
