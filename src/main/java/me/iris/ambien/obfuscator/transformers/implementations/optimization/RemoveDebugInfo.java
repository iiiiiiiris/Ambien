package me.iris.ambien.obfuscator.transformers.implementations.optimization;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.exceptions.SettingConflictException;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.Crasher;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;

@TransformerInfo(
        name = "remove-debug-info",
        category = Category.OPTIMIZATION,
        stability = Stability.STABLE,
        description = "Removes information from classes related to debugging."
)
public class RemoveDebugInfo extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        // Check for settings conflicts
        if (Ambien.get.transformerManager.getTransformer("crasher").isEnabled() && Crasher.junkSignatures.isEnabled())
            throw new SettingConflictException("The remove-debug-info transformer can't be used while using the junk-signatures setting in the crasher transformer. (Disable one)");

        // Remove debug info from classes
        getClasses(wrapper).forEach(classWrapper -> {
            final ClassNode classNode = classWrapper.getNode();
            classNode.sourceDebug = "";
            classNode.sourceFile = "";
            classNode.signature = "";
        });
    }
}
