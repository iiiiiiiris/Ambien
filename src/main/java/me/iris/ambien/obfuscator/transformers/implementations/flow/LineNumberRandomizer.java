package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.Arrays;

@TransformerInfo(
        name = "line-number-randomizer",
        category = Category.CONTROL_FLOW
)
public class LineNumberRandomizer extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            classWrapper.getMethods().forEach(methodNode -> {
                Arrays.stream(methodNode.instructions.toArray()).
                        filter(insn -> insn instanceof LineNumberNode).
                        map(insn -> (LineNumberNode)insn).
                        forEach(lineNode -> lineNode.line = MathUtil.randomInt());
            });
        });
    }
}
