package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;

/**
 * Shuffles the placement of methods & fields
 */
@TransformerInfo(
        name = "shuffle",
        category = Category.CONTROL_FLOW
)
public class Shuffle extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            final ClassNode node = classWrapper.getNode();
            Collections.shuffle(node.methods);
            Collections.shuffle(node.fields);
        });
    }
}
