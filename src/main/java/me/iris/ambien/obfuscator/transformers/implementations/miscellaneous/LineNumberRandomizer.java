package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.Arrays;

@TransformerInfo(
        name = "line-number-randomizer",
        ordinal = Ordinal.LOW,
        category = Category.MISCELLANEOUS
)
public class LineNumberRandomizer extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                Arrays.stream(methodNode.instructions.toArray()).
                        filter(insn -> insn instanceof LineNumberNode).
                        map(insn -> (LineNumberNode)insn).
                        forEach(lineNode -> lineNode.line = MathUtil.randomInt());
            });
        });
    }
}
