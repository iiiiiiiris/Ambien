package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * Adds instructions that don't do anything
 */
@TransformerInfo(
        name = "useless-instructions",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Adds NOP instructions to methods."
)
public class UselessInstructions extends Transformer {
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
        getClasses(wrapper).forEach(classWrapper ->
                classWrapper.getTransformableMethods().forEach(methodWrapper ->
                        injectNOPInsn(methodWrapper, classWrapper.getName())));
    }

    private void injectNOPInsn(final MethodWrapper methodWrapper, final String className) {
        // Get instruction iterator
        final Iterator<?> iterator = methodWrapper.getInstructions().iterator();

        // integer for iterator index
        int i = 0;
        while (iterator.hasNext()) {
            // Get instruction
            final AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();

            // Check index in iterator to interval
            if (i % nopInsnInterval.getValue() == 0) {
                // Add x amount of NOP instructions
                for (int c = 0; c < nopInsnCount.getValue(); c++) {
                    if (methodWrapper.getSize() + 1 >= SizeEvaluator.MAX_SIZE) {
                        Ambien.LOGGER.error("Can't add NOP instruction without method overflowing. Class: {} | Method: {}", className, methodWrapper.getNode().name);
                        break;
                    }

                    methodWrapper.getInstructionsList().insert(instruction, new InsnNode(NOP));
                    //Ambien.LOGGER.debug("inserted nop instructions @ {} within method {} within class {}", i, methodWrapper.getNode().name, className);
                }
            }

            // Increment index
            i++;
        }
    }
}
