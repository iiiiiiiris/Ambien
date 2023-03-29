package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.builders.ClassBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;

/**
 * Adds classes that aren't used to the exported jar (bloat classes, sponsored by microsoft)
 */
@TransformerInfo(
        name = "fake-classes",
        category = Category.PACKAGING,
        stability = Stability.STABLE,
        description = "Adds classes that aren't used"
)
public class FakeClasses extends Transformer {
    public final NumberSetting<Integer> classes = new NumberSetting<>("classes", 50);

    @Override
    public void transform(JarWrapper wrapper) {
        for (int i = 0; i < classes.getValue(); i++) {
            final ClassBuilder builder = new ClassBuilder().
                    setName(StringUtil.randomString(15)).
                    setAccess(ACC_PUBLIC);

            final ClassNode node = builder.buildNode();
            wrapper.getClasses().add(new ClassWrapper(node.name + ".class", node, false));
        }
    }
}
