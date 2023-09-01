package me.iris.ambien.obfuscator.colonial;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.function.Consumer;

public class BytecodeHelper {

    public static <T extends AbstractInsnNode> void forEach(InsnList instructions,
                                                            Class<T> type,
                                                            Consumer<T> consumer) {
        AbstractInsnNode[] array = instructions.toArray();
        for (AbstractInsnNode node : array) {
            if (node.getClass() == type) {
                //noinspection unchecked
                consumer.accept((T) node);
            }
        }
    }

    public static void forEach(InsnList instructions, Consumer<AbstractInsnNode> consumer) {
        forEach(instructions, AbstractInsnNode.class, consumer);
    }

}