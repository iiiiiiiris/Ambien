package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

@TransformerInfo(
        name = "fake-jumps",
        category = Category.CONTROL_FLOW
)
public class FakeJumps extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                injectFakeVarJump(classWrapper, methodNode);
            });
        });
    }

    private void injectFakeVarJump(final ClassWrapper wrapper, final MethodNode node) {
        // Make sure the class isn't an interface
        if (wrapper.isInterface()) return;

        // Make sure the method has instructions already in it
        if (node.instructions == null || node.instructions.size() == 0) return;

        // Check if it's the class constructor
        if (node.name.equals("<init>")) return;

        // Build method that returns a random int
        final String randMethodName = StringUtil.randomString(20);
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
        visitInsnList.add(new VarInsnNode(ISTORE, 1));
        node.instructions.insertBefore(node.instructions.getFirst(), visitInsnList);
    }
}
