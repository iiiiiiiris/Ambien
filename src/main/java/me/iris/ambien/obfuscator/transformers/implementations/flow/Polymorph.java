package me.iris.ambien.obfuscator.transformers.implementations.flow;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TransformerInfo(
        name = "polymorph",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "asdasd."
)
public class Polymorph extends Transformer {
    private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();
    SecureRandom random = new SecureRandom();


    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper)
                .forEach(classWrapper -> {
                    List<MethodWrapper> methods = classWrapper.getTransformableMethods().stream()
                            .filter(MethodWrapper::hasInstructions)
                            .collect(Collectors.toList());
                    classMethodsMap.put(classWrapper, methods);
                });

        classMethodsMap.forEach((classWrapper, methods) ->
                methods.forEach(this::handle));
    }

    public void handle(MethodWrapper method) {
        Stream<AbstractInsnNode> instructions = method.getInstructions();

        AtomicInteger index = new AtomicInteger();

        Stream.of(instructions.toArray())
                .forEach(insn -> {
                    if (insn instanceof LdcInsnNode) {
                        if (random.nextBoolean()) {
                            method.getInstructionsList().insertBefore((AbstractInsnNode) insn, new IntInsnNode(BIPUSH, ThreadLocalRandom.current().nextInt(-64, 64)));
                            method.getInstructionsList().insertBefore((AbstractInsnNode) insn, new InsnNode(POP));
                        }
                    } else if (index.getAndIncrement() % 6 == 0) {
                        if (random.nextFloat() > 0.6) {
                            method.getInstructionsList().insertBefore((AbstractInsnNode) insn, new IntInsnNode(BIPUSH, ThreadLocalRandom.current().nextInt(-27, 37)));
                            method.getInstructionsList().insertBefore((AbstractInsnNode) insn, new InsnNode(POP));
                        } else {
                            method.getInstructionsList().insertBefore((AbstractInsnNode) insn, new InsnNode(NOP));
                        }
                    }
                });
    }
}
