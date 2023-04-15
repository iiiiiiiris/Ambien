package me.iris.testjar;

import me.iris.ambien.library.annotations.Exclude;

//@Exclude
public class AnotherClass {
    private final int unusedField = 45;

    public static void yellow() {
        final short s = 12356;
        final int i = 23542362;
        final long l = 3445742434654654653L;
        final float f = 3.153453f;
        final double d = 7.2452346D;
        System.out.println(s);
        System.out.println(i);
        System.out.println(l);
        System.out.println(f);
        System.out.println(d);
    }

    @Exclude
    private void neverUsed() {
        System.out.println("wow");
    }
}
