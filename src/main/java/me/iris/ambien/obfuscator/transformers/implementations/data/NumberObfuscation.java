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
     * Adds additional number shifting on top of xor
     */
    public final BooleanSetting aggressive = new BooleanSetting("aggressive", false);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                // ignore empty methods
                if (methodNode.instructions == null || methodNode.instructions.size() == 0) return;

                // TODO: Implement this for longs, floats, doubles + recode this :)
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == BIPUSH)
                        .map(insn -> (IntInsnNode)insn)
                        .forEach(insn -> {
                            // Int instruction node

                            // TODO: check if the operand is a min/max value

                            // List for instructions
                            final InsnList list = new InsnList();

                            // Xor keys
                            final int xor = MathUtil.randomInt(1, Short.MAX_VALUE),
                                    xor2 = MathUtil.randomInt(1, Short.MAX_VALUE);

                            // Apply xor to operand
                            final int xorVal = insn.operand ^ xor;
                            final int xorVal2 = xorVal ^ xor2;

                            if (aggressive.isEnabled()) {
                                //int xorIdx = methodNode.maxLocals + 1;
                                final boolean randSwitch = MathUtil.RANDOM.nextBoolean();

                                // credits itzsomebody for ixor & ior shit

                                // Xor first value & scrap it
                                list.add(new IntInsnNode(BIPUSH, xor2));
                                list.add(new IntInsnNode(BIPUSH, xorVal2));
                                list.add(new InsnNode(IXOR));
                                list.add(new InsnNode(POP));

                                // push xor2 & xorval2 to the stack
                                list.add(new IntInsnNode(BIPUSH, xor2));
                                list.add(new IntInsnNode(BIPUSH, xorVal2));

                                // complicated IOR
                                list.add(new InsnNode(ICONST_M1));
                                list.add(new InsnNode(IXOR));
                                list.add(new InsnNode(IAND));
                                list.add(new IntInsnNode(BIPUSH, xorVal2));
                                list.add(new InsnNode(IADD));

                                // push xor2 & xorval2 to the stack
                                list.add(new IntInsnNode(BIPUSH, xor2));
                                list.add(new IntInsnNode(BIPUSH, xorVal2));

                                // finish xor
                                list.add(new InsnNode(IAND));
                                list.add(new InsnNode(ISUB));

                                // xor xor'd xor2 w/ xor1 (wtf)
                                list.add(new IntInsnNode(BIPUSH, xor));
                                list.add(new InsnNode(IXOR));

                                // negate value twice
                                if (randSwitch)
                                    list.add(new InsnNode(INEG));

                                list.add(new InsnNode(ICONST_M1));
                                list.add(new InsnNode(IXOR));
                                list.add(new InsnNode(ICONST_1));
                                list.add(new InsnNode(IADD));

                                if (!randSwitch)
                                    list.add(new InsnNode(INEG));
                            } else {
                                // Xor the value twice
                                list.add(new IntInsnNode(BIPUSH, xor2));
                                list.add(new IntInsnNode(BIPUSH, xorVal2));
                                list.add(new InsnNode(IXOR));

                                list.add(new IntInsnNode(BIPUSH, xor));
                                list.add(new InsnNode(IXOR));
                            }

                            // Replace current val w/ obfuscated val instructions
                            methodNode.instructions.insertBefore(insn, list);
                            methodNode.instructions.remove(insn);
                });
            });
        });
    }
}
