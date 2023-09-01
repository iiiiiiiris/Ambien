package me.iris.ambien.obfuscator.mosey;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.concurrent.atomic.AtomicBoolean;

@TransformerInfo(
        name = "mosey-string-encryption",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH,
        description = "Encrypts string using mosey's transformer."
)
public class MoseyStringEncryption extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            final AtomicBoolean hasStrings = new AtomicBoolean(false);
            for (MethodNode methodNode : classWrapper.getNode().methods) {
                for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof String) {
                        hasStrings.set(true);
                        break;
                    }
                }
            }

            if (!hasStrings.get()) return;

            LightStringEncryptionIntensity.accept(classWrapper);

            classWrapper.getMethods().forEach(method -> {
                method.getInstructions().forEach(instruction -> {
                    if (instruction instanceof LdcInsnNode && ((LdcInsnNode) instruction).cst instanceof String) {
                        method.getInstructionsList().insert(instruction, LightStringEncryptionIntensity.encrypt((LdcInsnNode) instruction, classWrapper));
                        method.getInstructionsList().remove(instruction);
                    }
                });
            });
        });
    }
}
