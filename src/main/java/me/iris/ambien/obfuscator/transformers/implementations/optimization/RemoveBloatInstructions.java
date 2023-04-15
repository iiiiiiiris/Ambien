package me.iris.ambien.obfuscator.transformers.implementations.optimization;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.exceptions.SettingConflictException;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.Arrays;

@TransformerInfo(
        name = "remove-bloat-instructions",
        category = Category.OPTIMIZATION,
        stability = Stability.STABLE,
        description = "Removes instructions that don't do anything"
)
public class RemoveBloatInstructions extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        // Check for settings conflicts
        if (Ambien.get.transformerManager.getTransformer("junk-code").isEnabled())
            throw new SettingConflictException("The remove-bloat-instructions transformer can't be used alongside the junk-code transformer. (Disable one)");

        // Remove NOP & line instructions
        getClasses(wrapper).forEach(classWrapper ->
                classWrapper.getTransformableMethods().forEach(methodWrapper ->
                        methodWrapper.getInstructions().forEach(insn -> {
            if (insn instanceof LineNumberNode || insn.getOpcode() == NOP)
                methodWrapper.getNode().instructions.remove(insn);
        })));
    }
}
