package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@TransformerInfo(
        name = "math-stack-mangler",
        category = Category.DATA
)
public class MathStackMangler extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper ->
                classWrapper.getTransformableMethods().forEach(methodNode -> {
                    if (methodNode.instructions == null || methodNode.instructions.size() == 0) return;
                    AtomicInteger counter = new AtomicInteger(0);
                    // TODO: Check if there are other datatypes on the stack
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(insn -> insn.getOpcode() == BIPUSH)
                            .map(insn -> (IntInsnNode)insn)
                            .forEach(insn -> {
                                if (counter.getAndIncrement() % 2 == 0)
                                    methodNode.instructions.insert(insn, getInstructions());
                            });
        }));
    }

    private InsnList getInstructions() {
        final InsnList list = new InsnList();
        if (MathUtil.RANDOM.nextBoolean()) {
            final int[] initializers = new int[]{ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};
            list.add(new InsnNode(initializers[MathUtil.randomInt(0, initializers.length)]));
        } else {
            list.add(new VarInsnNode(BIPUSH, MathUtil.randomInt(Short.MIN_VALUE, Short.MAX_VALUE)));
            list.add(new VarInsnNode(BIPUSH, MathUtil.randomInt(Short.MIN_VALUE, Short.MAX_VALUE)));

            final int[] operations = new int[]{IADD, ISUB, IMUL, IDIV, IXOR};
            list.add(new InsnNode(operations[MathUtil.randomInt(0, operations.length)]));
        }

        list.add(new InsnNode(POP));
        return list;
    }
}
