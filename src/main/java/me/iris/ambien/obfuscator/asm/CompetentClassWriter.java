package me.iris.ambien.obfuscator.asm;

import org.objectweb.asm.ClassWriter;

public class CompetentClassWriter extends ClassWriter {
    private static final String OBJECT = "java/lang/Object";

    public CompetentClassWriter(final int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        // First try to get the common super class via class loader
        return super.getCommonSuperClass(type1, type2);
    }

    /*private ClassNode getNodeFromName(final String path) {
        for (ClassWrapper wrapper : classes) {
            if (wrapper.getName().equals(path))
                return wrapper.getNode();
        }

        return null;
    }*/
}
