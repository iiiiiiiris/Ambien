package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;

@TransformerInfo(
        name = "argument-checker",
        category = Category.MISCELLANEOUS,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Check if arguments are present, exits if so."
)
public class ArgumentChecker extends Transformer {
    public final ListSetting blacklist = new ListSetting("blacklisted-arguments", new ArrayList<>(Arrays.asList("-javaagent", "-agentlib")));

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            final MethodNode initializer = classWrapper.getStaticInitializer();

            final InsnList list = new InsnList();
            final int idx = initializer.maxLocals + 1;

            // Add input argument list
            list.add(new LabelNode());
            list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false));
            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getInputArguments", "()Ljava/util/List;", true));
            list.add(new VarInsnNode(ASTORE, idx)); // arg list

            // Add argument checks
            for (String blacklisedArg : blacklist.getOptions()) {
                list.add(getBlacklistCheckInsns(blacklisedArg, idx));
            }

            // Add instructions
            if (!SizeEvaluator.willOverflow(initializer, list))
                initializer.instructions.insertBefore(initializer.instructions.getLast(), list);
            else
                Ambien.LOGGER.error("Can't add argument checking without overflowing initializer. Class: {}", classWrapper.getName());
        });
    }

    private InsnList getBlacklistCheckInsns(final String arg, final int startingIdx) {
        final InsnList list = new InsnList();

        final LabelNode labelA = new LabelNode(),
                labelB = new LabelNode(),
                labelC = new LabelNode(),
                labelD = new LabelNode(),
                labelE = new LabelNode(),
                labelF = new LabelNode();

        list.add(labelA);
        list.add(new VarInsnNode(ALOAD, startingIdx));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, startingIdx + 1));

        list.add(labelB);
        list.add(new VarInsnNode(ALOAD, startingIdx + 1));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, labelF)); // go to exit node
        list.add(new VarInsnNode(ALOAD, startingIdx + 1)); // load list iterator
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "java/lang/String"));
        list.add(new VarInsnNode(ASTORE, startingIdx + 2)); // store arg

        list.add(labelC);
        list.add(new VarInsnNode(ALOAD, startingIdx + 2));
        list.add(new LdcInsnNode(":"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;", false));
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(AALOAD));
        list.add(new LdcInsnNode(arg));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z", false));
        list.add(new JumpInsnNode(IFEQ, labelE)); // go to label that jumps back to iterator label

        list.add(labelD);
        list.add(new InsnNode(ICONST_0));
        list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false));

        list.add(labelE);
        list.add(new JumpInsnNode(GOTO, labelB));

        list.add(labelF);
        list.add(new InsnNode(NOP));

        return list;
    }
}
