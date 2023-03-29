package me.iris.ambien.obfuscator.transformers.implementations.flow;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adds extra steps before jumping to another label
 * And no, I couldn't think of a better name for this...
 */
@TransformerInfo(
        name = "goto-shenanigans",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH
)
public class GotoShenanigans extends Transformer {
    public final BooleanSetting aggressive = new BooleanSetting("aggressive", false);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).stream()
                .filter(classWrapper -> !classWrapper.isEnum() && !classWrapper.isInterface())
                .forEach(classWrapper -> {
            classWrapper.getTransformableMethods().stream()
                    .filter(methodNode -> !methodNode.name.equalsIgnoreCase("<clinit>"))
                    .forEach(methodNode -> {
                        final AtomicInteger localCounter = new AtomicInteger(methodNode.maxLocals);

                // Add an if-statement if there isn't one
                if (aggressive.isEnabled()) {
                    // Check if the method already has a goto instruction
                    boolean hasGotoInsn = false;
                    for (AbstractInsnNode insn : methodNode.instructions) {
                        if (insn.getOpcode() == GOTO) {
                            hasGotoInsn = true;
                            break;
                        }
                    }

                    if (!hasGotoInsn) {
                        final InsnList list = new InsnList();

                        // Label for random math
                        final LabelNode mathNode = new LabelNode();
                        list.add(mathNode);

                        // Add 2 random ints together & store it
                        final int[] addInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                        list.add(new IntInsnNode(BIPUSH, addInts[0]));
                        list.add(new IntInsnNode(BIPUSH, addInts[1]));
                        list.add(new InsnNode(IADD));
                        list.add(new VarInsnNode(ISTORE, localCounter.incrementAndGet()));

                        // Check if 2 random ints are equal
                        final int[] cmpInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                        list.add(new IntInsnNode(BIPUSH, cmpInts[0]));
                        list.add(new IntInsnNode(BIPUSH, cmpInts[1]));
                        list.add(new JumpInsnNode(IF_ICMPEQ, mathNode));

                        // Add bullshit goto instruction stuff to start of method
                        methodNode.instructions.insert(list);
                    }
                }

                // Replace generic GOTO instructions
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == GOTO)
                        .map(insn -> (JumpInsnNode)insn)
                        .forEach(insn -> {
                            final InsnList list = new InsnList();

                            if (aggressive.isEnabled()) {
                                // basic compare label (like below) -> int i = 0; if i == 0 -> orig GOTO
                                final LabelNode secondJumpLabel = new LabelNode(), exitLabel = new LabelNode();

                                list.add(new LabelNode());
                                // generate rand num & check if it's equal to itself, if nextboolean
                                final int[] cmpInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                                list.add(new IntInsnNode(BIPUSH, cmpInts[0]));
                                list.add(new IntInsnNode(BIPUSH, cmpInts[1]));
                                list.add(new JumpInsnNode(IF_ICMPEQ, secondJumpLabel));

                                list.add(secondJumpLabel);
                                list.add(new InsnNode(ICONST_0));
                                list.add(new VarInsnNode(ISTORE, localCounter.incrementAndGet()));
                                list.add(new VarInsnNode(ILOAD, localCounter.get()));
                                list.add(new InsnNode(ICONST_0));
                                list.add(new JumpInsnNode(IF_ICMPEQ, exitLabel));

                                list.add(exitLabel);
                            } else {
                                final LabelNode label = new LabelNode();
                                list.add(new LabelNode());
                                final int[] cmpInts = MathUtil.getTwoRandomInts(Short.MIN_VALUE, Short.MAX_VALUE);
                                list.add(new IntInsnNode(BIPUSH, cmpInts[0]));
                                list.add(new IntInsnNode(BIPUSH, cmpInts[1]));
                                list.add(new JumpInsnNode(IF_ICMPEQ, label));
                                list.add(label);
                            }

                            methodNode.instructions.insertBefore(insn, list);
                        });
            });
        });
    }
}
