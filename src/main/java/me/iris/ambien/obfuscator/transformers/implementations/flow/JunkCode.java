package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.builders.InstructionModifier;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.utilities.UnicodeDictionary;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@TransformerInfo(
        name = "junk-code",
        category = Category.EXPLOITS,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Generates junk code."
)
public class JunkCode extends Transformer {
    private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();
    private final String repeat = (StringUtil.randomStringByStringList(4,UnicodeDictionary.arabic) + "\n").repeat(ThreadLocalRandom.current().nextInt(100,200));
    private final String repeatType = "[".repeat(255);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).stream()
                .filter(classWrapper -> !classWrapper.isInterface())
                .forEach(classWrapper -> {
                    List<MethodWrapper> methods = classWrapper.getTransformableMethods().stream()
                            .filter(methodWrapper -> !isSpecialMethod(methodWrapper.getNode()) && !methodWrapper.isInitializer())
                            .collect(Collectors.toList());
                    classMethodsMap.put(classWrapper, methods);
                });

        classMethodsMap.forEach((classWrapper, methods) ->
                methods.forEach(methodWrapper -> injectJunkCode(methodWrapper)));
    }

    public boolean isSpecialMethod(MethodNode node) {
        return (node.access & ACC_NATIVE) != 0 || (node.access & ACC_ABSTRACT) != 0;
    }

    public void injectJunkCode(MethodWrapper methodWrapper) {
        InstructionModifier modifier = new InstructionModifier();
        MethodNode method = methodWrapper.getNode();

        for (AbstractInsnNode instruction : methodWrapper.getInstructionsList()) {
            if (instruction instanceof LabelNode) {
                if (method.instructions.indexOf(instruction) == method.instructions.size() - 1) {
                    continue;
                }

                LabelNode label = new LabelNode();

                InsnList list = new InsnList();
                list.add(label);
                list.add(createNumberNode(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)));
                list.add(new JumpInsnNode(IFGE, (LabelNode) instruction));

                list.add(new InsnNode(ACONST_NULL));
                list.add(new TypeInsnNode(CHECKCAST, repeatType+"L;"));
                list.add(new MethodInsnNode(INVOKESTATIC, repeat, repeat, "(" + repeatType + "L;)V", false));

                modifier.prepend(instruction, list);
            }
        }

        modifier.apply(method);

    }

    public AbstractInsnNode createNumberNode(int value) {
        int opcode = getNumberOpcode(value);
        switch (opcode) {
            case Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5 -> {
                return new InsnNode(opcode);
            }
            default -> {
                if (value >= -128 && value <= 127) {
                    return new IntInsnNode(Opcodes.BIPUSH, value);
                } else if (value >= -32768 && value <= 32767) {
                    return new IntInsnNode(Opcodes.SIPUSH, value);
                } else {
                    return new LdcInsnNode(value);
                }
            }
        }
    }

    public int getNumberOpcode(int value) {
        switch (value) {
            case -1 -> {
                return Opcodes.ICONST_M1;
            }
            case 0 -> {
                return Opcodes.ICONST_0;
            }
            case 1 -> {
                return Opcodes.ICONST_1;
            }
            case 2 -> {
                return Opcodes.ICONST_2;
            }
            case 3 -> {
                return Opcodes.ICONST_3;
            }
            case 4 -> {
                return Opcodes.ICONST_4;
            }
            case 5 -> {
                return Opcodes.ICONST_5;
            }
            default -> {
                if (value >= -128 && value <= 127) {
                    return Opcodes.BIPUSH;
                } else if (value >= -32768 && value <= 32767) {
                    return Opcodes.SIPUSH;
                } else {
                    return Opcodes.LDC;
                }
            }
        }
    }

}
