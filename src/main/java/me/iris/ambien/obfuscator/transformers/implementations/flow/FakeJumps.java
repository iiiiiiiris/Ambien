package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.data.Category;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@TransformerInfo(
        name = "fake-jumps",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        description = "Adds useless method & jumps to it at the start of methods."
)
public class FakeJumps extends Transformer {
    private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).stream()
                .filter(classWrapper -> !classWrapper.isEnum() && !classWrapper.isInterface())
                .forEach(classWrapper -> {
                    List<MethodWrapper> methods = classWrapper.getTransformableMethods().stream()
                            .filter(methodWrapper -> methodWrapper.hasInstructions() && !methodWrapper.isInitializer())
                            .collect(Collectors.toList());
                    classMethodsMap.put(classWrapper, methods);
                });

        classMethodsMap.forEach((classWrapper, methods) ->
                methods.forEach(methodWrapper -> injectFakeVarJump(classWrapper, methodWrapper)));
    }


    private void injectFakeVarJump(final ClassWrapper wrapper, final MethodWrapper methodWrapper) {
        // Build method that returns a random int
        final String randMethodName = StringUtil.genName(20);
        final MethodBuilder randMethodBuilder = new MethodBuilder().setName(randMethodName).setAccess(ACC_PUBLIC | ACC_STATIC).setDesc("()I").setSignature(null);
        final MethodNode randMethodNode = randMethodBuilder.buildNode();

        // Add instructions to actually return a random int
        randMethodNode.visitCode(); {
            // Visit label
            final Label label = new Label();
            randMethodNode.visitLabel(label);

            // Add return instructions
            randMethodNode.visitLdcInsn(MathUtil.randomInt(1, Integer.MAX_VALUE));
            randMethodNode.visitInsn(IRETURN);
        } randMethodNode.visitEnd();

        // Add random int function to class
        wrapper.addMethod(randMethodNode);

        // Make instruction list to invoke the function
        final InsnList visitInsnList = new InsnList();
        visitInsnList.add(new MethodInsnNode(INVOKESTATIC, wrapper.getNode().name, randMethodName, "()I", false));
        visitInsnList.add(new VarInsnNode(ISTORE, methodWrapper.getNode().maxLocals + 1));

        // Add fake jump
        if (SizeEvaluator.willOverflow(methodWrapper, visitInsnList))
            Ambien.LOGGER.error("Can't xor boolean without method overflowing. Class: {} | Method: {}", wrapper.getName(), methodWrapper.getNode().name);
        else
            methodWrapper.getNode().instructions.insertBefore(methodWrapper.getInstructionsList().getFirst(), visitInsnList);
    }
}
