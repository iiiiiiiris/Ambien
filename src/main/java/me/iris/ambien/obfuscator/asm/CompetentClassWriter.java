package me.iris.ambien.obfuscator.asm;

import org.objectweb.asm.ClassWriter;

public class CompetentClassWriter extends ClassWriter {

    public CompetentClassWriter(final int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        // First try to get the common super class via class loader
        return super.getCommonSuperClass(type1, type2);
    }
}
