package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

@TransformerInfo(
        name = "invoke-dynamics",
        category = Category.CONTROL_FLOW,
        stability = Stability.EXPERIMENTAL,
        ordinal = Ordinal.HIGH
)
public class InvokeDynamics extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            if (classWrapper.isInterface()) return;
            if (classWrapper.getNode().version < V1_8) {
                Ambien.LOGGER.info("[invoke dynamics] Ignoring class {} (class is too old)", classWrapper.getName());
                return;
            }

            final MethodNode callSite = buildCallSite();
            final Handle handle = new Handle(H_INVOKESTATIC, classWrapper.getNode().name, callSite.name, callSite.desc, false);

            classWrapper.getMethods().forEach(methodWrapper -> {
                final InsnList instructions = methodWrapper.getInstructionsList();
                for (int i = 0; i < instructions.size(); i++) {
                    final AbstractInsnNode insn = instructions.get(i);
                    if (insn.getOpcode() != INVOKESTATIC) continue;

                    final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    final String desc = Type.getMethodDescriptor(Type.getReturnType(methodInsnNode.desc), Arrays.stream(methodInsnNode.desc.split(";")).map(Type::getType).toArray(Type[]::new));

                    final InvokeDynamicInsnNode dynamicInsn = new InvokeDynamicInsnNode(StringUtil.randomString(15), desc, handle);
                    instructions.insertBefore(insn, dynamicInsn);
                    instructions.remove(insn);
                }
            });
        });
    }

    private MethodNode buildCallSite() {
        final MethodBuilder builder = new MethodBuilder();
        final MethodNode node = builder.buildNode();

        node.visitCode(); {
            node.visitInsn(RETURN);
        } node.visitEnd();

        return node;
    }
}
