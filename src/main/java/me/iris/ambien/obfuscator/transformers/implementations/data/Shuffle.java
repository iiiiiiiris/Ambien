package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
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
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Shuffles fields & methods."
)
public class Shuffle extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            final ClassNode node = classWrapper.getNode();
            Collections.shuffle(node.methods);
            Collections.shuffle(node.fields);
        });
    }
}
