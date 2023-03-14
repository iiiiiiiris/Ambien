package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.builders.FieldBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

/**
 * Adds extra steps before jumping to another label
 * And no, I couldn't think of a better name for this...
 */
@TransformerInfo(
        name = "goto-shenanigans",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE
)
public class GotoShenanigans extends Transformer {
    public final BooleanSetting aggressive = new BooleanSetting("aggressive", false);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            if (classWrapper.isInterface()) return;
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                if (!aggressive.isEnabled() && methodNode.name.equals("<init>")) return;

                // Add an if statement if there isn't one
                if (aggressive.isEnabled()) {
                    // Check if the method already has a goto instruction
                    boolean hasGotoInsn = false;
                    for (AbstractInsnNode insn : methodNode.instructions) {
                        if (insn.getOpcode() == GOTO) {
                            hasGotoInsn = true;
                            break;
                        }
                    }

                    if (hasGotoInsn) return;
                    final InsnList list = new InsnList();

                    // Label for random math
                    final LabelNode mathNode = new LabelNode();
                    list.add(mathNode);

                    // Add 2 random ints together & store it
                    final int[] addInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                    list.add(new IntInsnNode(BIPUSH, addInts[0]));
                    list.add(new IntInsnNode(BIPUSH, addInts[1]));
                    list.add(new InsnNode(IADD));
                    list.add(new VarInsnNode(ISTORE, methodNode.maxLocals + 1));

                    // Check if 2 random ints are equal
                    final int[] cmpInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                    list.add(new IntInsnNode(BIPUSH, cmpInts[0]));
                    list.add(new IntInsnNode(BIPUSH, cmpInts[1]));
                    list.add(new JumpInsnNode(IF_ICMPEQ, mathNode));

                    // Add bullshit goto instruction stuff to start of method
                    methodNode.instructions.insert(list);
                }

                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == GOTO)
                        .map(insn -> (JumpInsnNode)insn)
                        .forEach(insn -> {
                            final InsnList list = new InsnList();

                            if (aggressive.isEnabled()) {
                                // Random numbers we will compare
                                final int[] randInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);

                                // Apply xor to first number
                                final int xorKey = MathUtil.randomInt(1, Short.MAX_VALUE);
                                randInts[0] ^= xorKey;

                                // Random integer
                                final String fieldName = StringUtil.randomString(MathUtil.randomInt(10, 50));
                                final FieldBuilder builder = new FieldBuilder()
                                        .setName(fieldName)
                                        .setDesc("I")
                                        .setAccess(ACC_PUBLIC | ACC_STATIC | ACC_FINAL)
                                        .setValue(randInts[0]);

                                // Build & add field
                                final FieldNode field = builder.buildNode();
                                classWrapper.addField(field);

                                // Get random field
                                list.add(new FieldInsnNode(GETSTATIC, classWrapper.getNode().name, fieldName, "I"));
                                list.add(new IntInsnNode(BIPUSH, xorKey));
                                list.add(new InsnNode(IXOR));

                                // get random number
                                list.add(new IntInsnNode(BIPUSH, randInts[1]));

                                // If they're not equal, jump to original label
                                list.add(new JumpInsnNode(IF_ICMPNE, insn.label));
                            } else {
                                // check if the same number is equal then jump to original label
                                final int randInt = MathUtil.randomInt(Short.MIN_VALUE, Short.MAX_VALUE);
                                list.add(new IntInsnNode(BIPUSH, randInt));
                                list.add(new IntInsnNode(BIPUSH, randInt));
                                list.add(new JumpInsnNode(IF_ICMPEQ, insn.label));
                            }

                            methodNode.instructions.insertBefore(insn, list);
                            methodNode.instructions.remove(insn);
                        });
            });
        });
    }
}
