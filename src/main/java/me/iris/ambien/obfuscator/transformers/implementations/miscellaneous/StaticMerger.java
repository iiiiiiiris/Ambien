package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.builders.ClassBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Merges all static fields into a single class
 */
@TransformerInfo(
        name = "static-merger",
        category = Category.MISCELLANEOUS,
        stability = Stability.EXPERIMENTAL
)
public class StaticMerger extends Transformer {
    /**
     * Moves the merged class to a random directory
     */
    public final BooleanSetting randomDirectory = new BooleanSetting("move-to-rand-dir", true);

    @Override
    public void transform(JarWrapper wrapper) {
        // Get name for class
        StringBuilder mergedName = new StringBuilder(StringUtil.randomString(MathUtil.randomInt(10, 50)));
        if (randomDirectory.isEnabled())
            mergedName.insert(0, wrapper.getDirectories().get(MathUtil.randomInt(0, wrapper.getDirectories().size())));
        // TODO: append forward slash
        System.out.println(mergedName);

        // Build class
        final ClassBuilder mergedBuilder = new ClassBuilder()
                .setAccess(ACC_PUBLIC)
                .setName(mergedName.toString())
                .setVersion(V1_8);

        // Create new class wrapper for merged class
        final ClassWrapper mergedWrapper = new ClassWrapper(mergedName + ".class", mergedBuilder.buildNode(), false);

        // Loop through classes to get fields & replace their calls
        getClasses(wrapper).forEach(classWrapper -> {
            // TODO: fix this crashing
            final HashMap<String, String> fieldMap = new HashMap<>();

            // Add public static fields to merged class & remove from class
            Arrays.stream(classWrapper.getFields().toArray())
                    .map(fieldNode -> (FieldNode)fieldNode)
                    .forEach(fieldNode -> {
                        if ((fieldNode.access & ACC_PUBLIC) != 0 && (fieldNode.access & ACC_STATIC) != 0) {
                            final String newName = StringUtil.randomString(MathUtil.randomInt(10, 50));
                            fieldMap.put(classWrapper.getName() + "~~~~~" + fieldNode.name, newName);
                            System.out.println(fieldNode.name);
                            fieldNode.name = newName;

                            mergedWrapper.addField(fieldNode);
                            classWrapper.getFields().remove(fieldNode);
                        }
                    });

            // Replace all GETSTATIC node owner in all classes
            getClasses(wrapper).forEach(classWrapper2 -> {
                classWrapper.getTransformableMethods().forEach(methodNode -> {
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(insn -> insn.getOpcode() == GETSTATIC)
                            .map(insn -> (FieldInsnNode)insn)
                            .forEach(insn -> {
                                insn.owner = mergedName.toString();
                                insn.name = fieldMap.get(insn.name).split("~~~~~")[1];
                            });
                });
            });
        });

        // Add merged class to jar
        wrapper.getClasses().add(mergedWrapper);
    }
}
