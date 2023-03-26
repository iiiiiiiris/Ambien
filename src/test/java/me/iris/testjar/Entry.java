package me.iris.testjar;

public class Entry {
    public static boolean uhhhh = true;

    public static void main(String[] args) throws Throwable {
        boolean plaaaaaaaay = false;
        for (int i = 0; i < 15; i++) {
            System.out.printf("%b: %d\n", plaaaaaaaay, i);
            plaaaaaaaay = !plaaaaaaaay;
        }

        //makeCallSite(Entry.class, "anotherMethod", "()V"/*, MethodType.methodType(void.class)*/).dynamicInvoker().invoke();
    }

    /*private static void anotherMethod() {
        System.out.println("yellow");
    }

    private static MutableCallSite makeCallSite(final Class<?> target, final String name, final String desc) throws NoSuchMethodException, IllegalAccessException {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle handle = lookup.findStatic(target, name, MethodType.fromMethodDescriptorString(desc, target.getClassLoader()));
        return new MutableCallSite(handle);
    }*/
}
