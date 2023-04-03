package me.iris.ambien.obfuscator.asm;

import me.iris.ambien.obfuscator.Ambien;
import org.objectweb.asm.ClassWriter;

public class CompetentClassWriter extends ClassWriter {
    private static final String OBJECT = "java/lang/Object";

    public CompetentClassWriter(int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        // Debugging
        Ambien.LOGGER.debug("{}-{}", type1, type2);





        // uhhhh
        return super.getCommonSuperClass(type1, type2);

        /*ClassLoader classLoader = getClassLoader();
        Class<?> class1;
        try {
            class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type1, e);
        }

        Class<?> class2;
        try {
            class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type2, e);
        }

        if (class1.isAssignableFrom(class2))
            return type1;
        if (class2.isAssignableFrom(class1))
            return type2;

        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }*/
    }

    private String getCommonSuperName(final String type1, final String type2) {
        return "";
    }
}
