package me.iris.ambien.obfuscator.builders;

import org.objectweb.asm.tree.MethodNode;

public class MethodBuilder {
    private int access;
    private String name, desc, signature;
    private String[] exceptions;

    public MethodBuilder setAccess(final int access) {
        this.access = access;
        return this;
    }

    public MethodBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public MethodBuilder setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    public MethodBuilder setSignature(final String signature) {
        this.signature = signature;
        return this;
    }

    public MethodBuilder setExceptions(final String[] exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    public MethodNode buildNode() {
        return new MethodNode(access, name, desc, signature, exceptions);
    }
}
