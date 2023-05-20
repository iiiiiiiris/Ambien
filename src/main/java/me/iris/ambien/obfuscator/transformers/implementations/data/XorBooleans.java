package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TransformerInfo(
        name = "xor-booleans",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Adds xor instruction to generic true/false to all local booleans."
)
public class XorBooleans extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().stream().filter(MethodWrapper::hasLocalVariables).forEach(methodWrapper -> {
                final List<Integer> booleanIndexes = new ArrayList<>();

                // Get the index of all booleans in method
                methodWrapper.getNode().localVariables.forEach(localVarNode -> {
                    if (localVarNode.desc == null) return;

                    if (localVarNode.desc.equals("Z"))
                        booleanIndexes.add(localVarNode.index);
                });

                // Obfuscate setting a boolean to true/false explicitly
                methodWrapper.getInstructions()
                        .filter(insn ->
                                insn instanceof VarInsnNode &&
                                        (insn.getPrevious().getOpcode() == ICONST_0 || insn.getPrevious().getOpcode() == ICONST_1) &&
                                        (insn.getOpcode() == ISTORE) &&
                                        booleanIndexes.contains(((VarInsnNode)insn).var))
                        .map(insn -> (VarInsnNode) insn)
                        .forEach(insn -> {
                            // get previous instruction & opcode
                            final AbstractInsnNode prevInsn = insn.getPrevious();
                            final int prevOpcode = prevInsn.getOpcode();

                            // basic xor operation on true & false
                            // repeating the operation can cause some decompilers to produce a retarded output
                            final InsnList xorInsns = new InsnList();
                            if (prevOpcode == ICONST_0) {
                                xorInsns.add(new InsnNode(ICONST_0));
                                xorInsns.add(new InsnNode(ICONST_1));
                                xorInsns.add(new InsnNode(IXOR));
                            } else if (prevOpcode == ICONST_1) {
                                xorInsns.add(new InsnNode(ICONST_1));
                                xorInsns.add(new InsnNode(ICONST_0));
                                xorInsns.add(new InsnNode(IXOR));
                            }

                            // replace default value
                            if (SizeEvaluator.willOverflow(methodWrapper, xorInsns))
                                Ambien.LOGGER.error("Can't xor boolean without method overflowing. Class: {} | Method: {}", classWrapper.getName(), methodWrapper.getNode().name);
                            else {
                                methodWrapper.getNode().instructions.insertBefore(insn, xorInsns);
                                methodWrapper.getNode().instructions.remove(prevInsn);
                            }
                        });
            });
        });
    }
}
