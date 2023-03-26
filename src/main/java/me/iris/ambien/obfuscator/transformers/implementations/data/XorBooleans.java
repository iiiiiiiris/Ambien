package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TransformerInfo(
        name = "xor-booleans",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW
)
public class XorBooleans extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                final List<Integer> booleanIndexes = new ArrayList<>();

                // Get the index of all booleans in method
                methodNode.localVariables.forEach(localVarNode -> {
                    if (localVarNode.desc == null) return;

                    if (localVarNode.desc.equals("Z"))
                        booleanIndexes.add(localVarNode.index);
                });

                // Obfuscate setting a boolean to true/false explicitly
                Arrays.stream(methodNode.instructions.toArray())
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
                                for (int i = 0; i < MathUtil.RANDOM.nextInt(10); i++) {
                                    xorInsns.add(new InsnNode(ICONST_0));
                                    xorInsns.add(new InsnNode(ICONST_1));
                                    xorInsns.add(new InsnNode(IXOR));
                                }
                            } else if (prevOpcode == ICONST_1) {
                                for (int i = 0; i < MathUtil.RANDOM.nextInt(10); i++) {
                                    xorInsns.add(new InsnNode(ICONST_1));
                                    xorInsns.add(new InsnNode(ICONST_0));
                                    xorInsns.add(new InsnNode(IXOR));
                                }
                            }

                            // replace default value
                            methodNode.instructions.insertBefore(insn, xorInsns);
                            methodNode.instructions.remove(prevInsn);
                        });
            });
        });
    }
}
