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
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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

            classWrapper.getTransformableMethods().forEach(methodNode -> {
                Arrays.stream(methodNode.instructions.toArray())
                        .filter(insn -> insn.getOpcode() == INVOKESTATIC)
                        .map(insn -> (MethodInsnNode)insn)
                        .forEach(insn -> {
                            // TODO
                            /*methodNode.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC,
                                    classWrapper.getNode().name, callSite.name, callSite.desc));*/
                            //methodNode.instructions.insertBefore(insn, new InvokeDynamicInsnNode(StringUtil.randomString(15), insn.desc, handle));
                            //methodNode.instructions.remove(insn);
                        });
            });
        });
    }

    private MethodNode buildCallSite() {
        final MethodBuilder builder = new MethodBuilder();
        final MethodNode node = builder.buildNode();

        node.visitCode(); {

        } node.visitEnd();

        return node;
    }
}
