package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

@TransformerInfo(
        name = "number-obfuscation",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH,
        description = "Replaces numbers with random math operations to get the original value."
)
public class NumberObfuscation extends Transformer {
    /**
     * Adds additional math operations on top of generic xoring
     * Probably shouldn't use this is a server or game/mod if you care about performance
     */
    public final BooleanSetting aggressive = new BooleanSetting("aggressive", false);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                // ignore empty methods
                if (methodNode.instructions == null || methodNode.instructions.size() == 0) return;

                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == BIPUSH || insn.getOpcode() == SIPUSH || insn.getOpcode() == LDC)
                        .forEach(insn -> {
                            final InsnList list = new InsnList();

                            // Obfuscate number
                            if (insn.getOpcode() == LDC)
                                list.add(obfuscateLDC((LdcInsnNode)insn));
                            else
                                list.add(obfuscatePUSH(insn.getOpcode(), ((IntInsnNode)insn).operand));

                            // Replace instructions
                            if (list.size() > 0) {
                                methodNode.instructions.insertBefore(insn, list);
                                methodNode.instructions.remove(insn);
                            }
                        });
            });
        });
    }

    private InsnList obfuscateLDC(final LdcInsnNode node) {
        final InsnList list = new InsnList();
        // TODO
        return list;
    }

    private InsnList obfuscatePUSH(final int pushOpcode, final int operand) {
        final InsnList list = new InsnList();

        if (aggressive.isEnabled()) {
            // get or keys
            final int[] orKeys = MathUtil.getTwoRandomInts(1, Short.MAX_VALUE);
            final int orKey = orKeys[0] | orKeys[1];

            // get xor key
            final int xorKey = MathUtil.randomInt(1, Short.MAX_VALUE);

            // perform operations on original value
            final int val = operand ^ (xorKey | orKey);

            // perform or operation on or keys
            list.add(new IntInsnNode(pushOpcode, orKeys[0]));
            list.add(new IntInsnNode(pushOpcode, orKeys[1]));
            list.add(new InsnNode(IOR));

            // perform or operation on xor key
            list.add(new IntInsnNode(pushOpcode, xorKey));
            list.add(new InsnNode(IOR));

            // perform xor operation on value
            list.add(new IntInsnNode(pushOpcode, val));
            list.add(new InsnNode(IXOR));
        } else {
            // perform basic xor operation
            final int key = MathUtil.randomInt(1, Short.MAX_VALUE);
            final int xorVal = operand ^ key;

            list.add(new IntInsnNode(pushOpcode, key)); // push key to stack
            list.add(new IntInsnNode(pushOpcode, xorVal)); // push xor'd value to stack
            list.add(new InsnNode(IXOR)); // perform xor operation

            // negate the value twice
            for (int i = 0; i < 2; i++)
                list.add(new InsnNode(INEG));
        }

        return list;
    }
}
