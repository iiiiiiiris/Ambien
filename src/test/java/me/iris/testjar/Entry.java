package me.iris.testjar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class Entry {
    public static boolean uhhhh = true;

    public static void main(String[] args) throws Throwable {
        AnotherClass.yellow();
        boolean plaaaaaaaay = false;
        for (int i = 0; i < 15; i++) {
            System.out.printf("%b: %d\n", plaaaaaaaay, i);
            plaaaaaaaay = !plaaaaaaaay;
        }

        makeCallSite(Entry.class, "anotherMethod", "(Ljava/lang/String;)V"/*, MethodType.methodType(void.class)*/).dynamicInvoker().invoke("hahaha");
    }

    private static void anotherMethod(String erm) {
        System.out.println(erm);
    }

    private static MutableCallSite makeCallSite(final Class<?> target, final String name, final String desc) throws NoSuchMethodException, IllegalAccessException {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle handle = lookup.findStatic(target, name, MethodType.fromMethodDescriptorString(desc, target.getClassLoader()));
        return new MutableCallSite(handle);
    }
}
