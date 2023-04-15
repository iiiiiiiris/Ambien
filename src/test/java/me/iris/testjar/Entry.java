package me.iris.testjar;

public class Entry {
    public static boolean uhhhh = true;

    public static void main(String[] args) throws Throwable {
        AnotherClass.yellow();
        test("test!");

        boolean plaaaaaaaay = !uhhhh;
        for (int i = 0; i < 15; i++) {
            System.out.printf("%b: %d\n", plaaaaaaaay, i);
            plaaaaaaaay = !plaaaaaaaay;
        }

        //makeCallSite("me.iris.testjar.Entry", "test", "(Ljava/lang/String;)V"/*, MethodType.methodType(void.class)*/).dynamicInvoker().invoke();
        //makeCallSite(/*Entry.class*/"me.iris.testjar.Entry", "anotherMethod", "(Ljava/lang/String;)V"/*, MethodType.methodType(void.class)*/).dynamicInvoker().invoke("hahaha");
    }

    private static void test(final String str) {
        System.out.println(str);
    }

    /*private static void anotherMethod(String erm) {
        System.out.println(erm);
    }*/

    /*private static MutableCallSite makeCallSite(final String owner, final String name, final String desc) throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        final Class<?> target = Class.forName(owner);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle handle = lookup.findStatic(target, name, MethodType.fromMethodDescriptorString(desc, target.getClassLoader()));
        return new MutableCallSite(handle);
    }*/
}
