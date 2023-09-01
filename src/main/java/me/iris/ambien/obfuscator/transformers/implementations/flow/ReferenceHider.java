package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

@TransformerInfo(
        name = "reference-hider",
        category = Category.CONTROL_FLOW,
        stability = Stability.EXPERIMENTAL,
        ordinal = Ordinal.HIGH
)
public class ReferenceHider extends Transformer {
    /**
     * Will only hide references to methods that don't have any arguments
     */
    public final BooleanSetting onlyNoArgs = new BooleanSetting("only-no-args", true);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).stream()
                .filter(classWrapper -> !classWrapper.isEnum() && !classWrapper.isInterface())
                .forEach(classWrapper -> {
                    if (classWrapper.getNode().version < V1_8) {
                        Ambien.LOGGER.info("[invoke dynamics] Ignoring class {} (class is too old)", classWrapper.getName());
                        return;
                    }

                    // Make sure the class has a method that calls another method
                    boolean hasMethodCalls = false;
                    for (MethodWrapper methodWrapper : classWrapper.getTransformableMethods()) {
                        if (hasMethodCalls) break;
                        for (AbstractInsnNode insn : methodWrapper.getInstructionsList()) {
                            if (insn instanceof MethodInsnNode && insn.getOpcode() == INVOKESTATIC) {
                                hasMethodCalls = true;
                                break;
                            }
                        }
                    }

                    if (!hasMethodCalls) return;

                    // Build & add call site
                    final MethodNode callSite = buildCallSite();
                    classWrapper.getNode().methods.add(callSite);

                    classWrapper.getTransformableMethods().forEach(methodWrapper ->
                            methodWrapper.getInstructions()
                            .filter(insn -> insn.getOpcode() == INVOKESTATIC)
                            .map(insn -> (MethodInsnNode)insn)
                            .forEach(insn -> {
                                // Make sure the owner is loaded
                                boolean isOwnerLoaded = false;
                                for (ClassWrapper classWrapper1 : wrapper.getClasses()) {
                                    if (classWrapper1.getNode().name.equals(insn.owner)) {
                                        isOwnerLoaded = true;
                                        break;
                                    }
                                }
                                if (!isOwnerLoaded) {
                                    Ambien.LOGGER.debug("owner not loaded: " + insn.owner);
                                    return;
                                }

                                // get target info
                                final String desc = insn.desc;
                                final String owner = insn.owner.replaceAll("/", ".");
                                final String targetMethod = insn.name;

                                final InsnList dynamicInvokerList = new InsnList();
                                dynamicInvokerList.add(new LdcInsnNode(owner));
                                dynamicInvokerList.add(new LdcInsnNode(targetMethod));
                                dynamicInvokerList.add(new LdcInsnNode(desc));
                                dynamicInvokerList.add(new MethodInsnNode(INVOKESTATIC, classWrapper.getNode().name, callSite.name, callSite.desc, false));
                                dynamicInvokerList.add(new MethodInsnNode(INVOKEVIRTUAL,
                                        "java/lang/invoke/MutableCallSite", "dynamicInvoker", "()Ljava/lang/invoke/MethodHandle;", false));
                                dynamicInvokerList.add(new MethodInsnNode(INVOKEVIRTUAL,
                                        "java/lang/invoke/MethodHandle", "invoke", desc, false));

                                if (desc.equals("()V") || onlyNoArgs.isEnabled()) {
                                    if (!SizeEvaluator.willOverflow(methodWrapper, dynamicInvokerList))
                                        methodWrapper.replaceInstruction(insn, dynamicInvokerList);
                                    else
                                        Ambien.LOGGER.error("Can't hide method reference without overflowing. Owner: {} | Method: {} | Target method: {}", owner, methodWrapper.getNode().name, targetMethod);
                                } else {
                                    // Get the first argument
                                    final int argCount = desc.substring(desc.indexOf('('), desc.indexOf(')')).split(";").length;
                                    AbstractInsnNode firstArgInsn = null;
                                    for (int i = 0; i < argCount + 1; i++) {
                                        if (i == 0) firstArgInsn = insn;
                                        firstArgInsn = firstArgInsn.getPrevious();
                                    }

                                    if (firstArgInsn != null) {
                                        if (!SizeEvaluator.willOverflow(methodWrapper, dynamicInvokerList)) {
                                            // Remove the invoke call from the list, we have to add this after the method's arguments are pushed onto the stack
                                            final MethodInsnNode invokeNode = (MethodInsnNode) dynamicInvokerList.getLast();
                                            dynamicInvokerList.remove(invokeNode);

                                            methodWrapper.getInstructionsList().insertBefore(firstArgInsn, dynamicInvokerList);
                                            methodWrapper.replaceInstruction(insn, invokeNode);
                                        } else
                                            Ambien.LOGGER.error("Can't hide method reference without overflowing. Owner: {} | Method: {} | Target method: {}", owner, methodWrapper.getNode().name, targetMethod);
                                    }
                                }
                            }));
        });
    }

    private MethodNode buildCallSite() {
        final MethodBuilder builder = new MethodBuilder()
                .setName(StringUtil.genName(MathUtil.randomInt(10, 50)))
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

            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/String;", null, labelA, labelA, 0);
            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/String;", null, labelC, labelC, 1);
            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/String;", null, labelC, labelC, 2);
            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/Class;", null, labelA, labelC, 3);

            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/invoke/MethodHandles$Lookup;", null, labelB, labelC, 4);

            node.visitLocalVariable(StringUtil.genName(MathUtil.randomInt(10, 50)),
                    "Ljava/lang/invoke/MethodHandle;", null, labelC, labelD, 5);
        } node.visitEnd();

        return node;
    }
}
