package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * Adds code that doesn't do anything to pre-existing methods
 */
@TransformerInfo(
        name = "junk-code",
        ordinal = Ordinal.LOW,
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
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                // Inject NOP instructions
                if (nopInsns.isEnabled())
                    injectNOPInsn(methodNode, classWrapper.getName());
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
}
