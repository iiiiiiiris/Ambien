package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

@TransformerInfo(
        name = "reference-hider",
        category = Category.CONTROL_FLOW,
        stability = Stability.EXPERIMENTAL,
        ordinal = Ordinal.HIGH
)
public class ReferenceHider extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        // TODO: Check for classes that have methods that have references to hide

        getClasses(wrapper).forEach(classWrapper -> {
            if (classWrapper.isInterface()) return;
            if (classWrapper.getNode().version < V1_8) {
                Ambien.LOGGER.info("[invoke dynamics] Ignoring class {} (class is too old)", classWrapper.getName());
                return;
            }

            // Build & add call site
            final MethodNode callSite = buildCallSite();
            classWrapper.getNode().methods.add(callSite);

            classWrapper.getMethods().forEach(methodWrapper ->
                    methodWrapper.getInstructions()
                    .filter(insn -> insn.getOpcode() == INVOKESTATIC)
                    .map(insn -> (MethodInsnNode)insn)
                    .forEach(insn -> {
                        if (!insn.desc.equals("()V")) return;

                        final String ownerDesc = insn.owner.replaceAll("/", ".");
                        final String targetMethod = insn.name;

                        // TODO: encrypt these strings
                        final InsnList invokeDynamicList = new InsnList();
                        invokeDynamicList.add(new LdcInsnNode(ownerDesc));
                        invokeDynamicList.add(new LdcInsnNode(targetMethod));
                        invokeDynamicList.add(new LdcInsnNode("()V"));
                        invokeDynamicList.add(new MethodInsnNode(INVOKESTATIC, classWrapper.getNode().name, callSite.name, callSite.desc, false));
                        invokeDynamicList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/invoke/MutableCallSite", "dynamicInvoker", "()Ljava/lang/invoke/MethodHandle;", false));

                        // TODO: add support for methods w/ arguments
                        // arguments for the method should go here (need to implement this :)
                        // don't forget to update descriptor in the invoke method :)

                        invokeDynamicList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", "()V", false));

                        // replace instructions
                        methodWrapper.replaceInstruction(insn, invokeDynamicList);
                    }));
        });
    }

    private MethodNode buildCallSite() {
        final MethodBuilder builder = new MethodBuilder()
                .setName(StringUtil.randomString(MathUtil.randomInt(10, 50)))
                .setDesc("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/MutableCallSite;")
                .setAccess(ACC_PRIVATE | ACC_STATIC)
                .setExceptions(new String[]{
                        "java/lang/NoSuchMethodException",
                        "java/lang/IllegalAccessException",
                        "java/lang/ClassNotFoundException"
                });
        final MethodNode node = builder.buildNode();

        node.visitCode(); {
            final Label labelA = new Label(),
                    labelB = new Label(),
                    labelC = new Label(),
                    labelD = new Label();

            node.visitLabel(labelA);
            node.visitLineNumber(33, labelA);
            node.visitVarInsn(ALOAD, 0); // load owner string
            node.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            node.visitVarInsn(ASTORE, 3); // store class instance @ idx 3

            node.visitLabel(labelB);
            node.visitLineNumber(34, labelB);
            node.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
            node.visitVarInsn(ASTORE, 4); // store lookup instance @ idx 4

            node.visitLabel(labelC);
            node.visitLineNumber(35, labelC);
            node.visitVarInsn(ALOAD, 4); // load lookup
            node.visitVarInsn(ALOAD, 3); // load target class
            node.visitVarInsn(ALOAD, 1); // load method name
            node.visitVarInsn(ALOAD, 2); // load method desc
            node.visitVarInsn(ALOAD, 3); // load target class
            node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
            node.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
            node.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
            node.visitVarInsn(ASTORE, 5); // store handle @ idx 5

            node.visitLabel(labelD);
            node.visitLineNumber(36, labelD);
            node.visitTypeInsn(NEW, "java/lang/invoke/MutableCallSite");
            node.visitInsn(DUP);
            node.visitVarInsn(ALOAD, 5); // load handle
            node.visitMethodInsn(INVOKESPECIAL, "java/lang/invoke/MutableCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false);
            node.visitInsn(ARETURN);

            // TODO: obfuscate variable names
        } node.visitEnd();

        return node;
    }
}
