package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.FieldBuilder;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * Adds code that doesn't do anything to pre-existing methods
 */
@TransformerInfo(
        name = "junk-code",
        category = Category.CONTROL_FLOW
)
public class JunkCode extends Transformer {
    /**
     * Adds NOP instructions
     */
    public final BooleanSetting nopInsns = new BooleanSetting("nop-instructions", true);
    /**
     * Interval NOP instructions will be placed ~ every x instructions will be NOP(s)
     */
    public final NumberSetting<Integer> nopInsnInterval = new NumberSetting<>("nop-instruction-interval", 5);
    /**
     * How many NOP instructions will be inserted
     */
    public final NumberSetting<Integer> nopInsnCount = new NumberSetting<>("nop-instruction-count", 1);

    @Override
    public void transform(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            //final ClassNode node = classWrapper.getNode();
            classWrapper.getMethods().forEach(methodNode -> {
                // Inject NOP instructions
                if (nopInsns.isEnabled())
                    injectNOPInsn(methodNode, classWrapper.getName());

                // Inject fake jumps
                injectJump(classWrapper, methodNode);
            });
        });
    }

    private void injectNOPInsn(final MethodNode node, final String className) {
        // Get instruction iterator
        final Iterator<?> iterator = node.instructions.iterator();

        // integer for iterator index
        int i = 0;
        while (iterator.hasNext()) {
            // Get instruction
            final AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();

            // Check index in iterator to interval
            if (i % nopInsnInterval.getValue() == 0) {
                // Add x amount of NOP instructions
                for (int c = 0; c < nopInsnCount.getValue(); c++) {
                    node.instructions.insert(instruction, new InsnNode(NOP));
                    Ambien.LOGGER.debug("inserted nop instructions @ {} within method {} within class {}", i, node.name, className);
                }
            }

            // Increment index
            i++;
        }
    }

    private void injectJump(final ClassWrapper wrapper, final MethodNode node) {
        // Make sure the class isn't an interface
        if (wrapper.isInterface()) return;

        // Make sure the method has instructions already in it
        if (node.instructions == null || node.instructions.size() == 0) return;

        // Build useless method
        final String methodName = StringUtil.randomString(20);
        final MethodBuilder builder = new MethodBuilder().setName(methodName).setAccess(ACC_PUBLIC | ACC_STATIC).setDesc("()I").setSignature(null);
        final MethodNode uselessNode = builder.buildNode();

        // Visit code & add return instructions to useless method node
        uselessNode.visitCode(); {
            // Visit label
            final Label label = new Label();
            uselessNode.visitLabel(label);
            uselessNode.visitLineNumber(12, label);

            // Return a random integer
            uselessNode.visitLdcInsn(MathUtil.randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
            uselessNode.visitInsn(IRETURN);
        } uselessNode.visitEnd();

        // Add useless method to class
        wrapper.addMethod(uselessNode);

        final InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, wrapper.getNode().name, methodName, "()I", false));
        node.instructions.insertBefore(node.instructions.getFirst(), list);
    }
}
