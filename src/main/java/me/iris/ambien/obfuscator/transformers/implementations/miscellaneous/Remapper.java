package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.HashMap;
import java.util.Map;

import static me.iris.ambien.obfuscator.utilities.StringUtil.getNewName;

@TransformerInfo(
        name = "remapper",
        category = Category.MISCELLANEOUS,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Renames your shit to random shit :)"
)
public class Remapper extends Transformer {
    public final BooleanSetting classes = new BooleanSetting("classes", true);
    public final BooleanSetting localVariables = new BooleanSetting("local-variables", true);
    /**
     * Options/modes:
     * random ~ Makes names random characters at a random length
     * barcode ~ Example: IllllIIIIlIlIIll
     */
    public final StringSetting dictionary = new StringSetting("dictionary", "random");
    public final StringSetting prefix = new StringSetting("prefix", "");

    @Override
    public void transform(JarWrapper wrapper) {
        if (classes.isEnabled()) remapClasses(wrapper);
        if (localVariables.isEnabled()) remapLocalVariables(wrapper);
    }

    private void remapClasses(JarWrapper jarWrapper) {
        final Map<String, String> map = new HashMap<>();
        final Map<String, ClassWrapper> wrappers = new HashMap<>();

        // Generate map
        jarWrapper.getClasses().forEach(classWrapper -> {
            final ClassNode node = classWrapper.getNode();
            if (StringUtil.containsNonAlphabeticalChars(node.name)) return;

            final String newName = getNewName(dictionary.getValue(), prefix.getValue());
            map.put(node.name, newName);
            wrappers.put(node.name, classWrapper);
        });

        // Apply map
        final SimpleRemapper remapper = new SimpleRemapper(map);
        for (ClassWrapper wrapper : wrappers.values()) {
            // Remap
            final ClassNode remappedNode = new ClassNode();
            final ClassRemapper classRemapper = new ClassRemapper(remappedNode, remapper);
            wrapper.getNode().accept(classRemapper);
            wrapper.setNode(remappedNode);
        }
    }

    private void remapLocalVariables(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodWrapper -> {
                if (!methodWrapper.hasLocalVariables()) return;
                for (Object localVarObj : methodWrapper.getNode().localVariables) {
                    //noinspection CastCanBeRemovedNarrowingVariableType
                    final LocalVariableNode localVarNode = (LocalVariableNode)localVarObj;
                    if (localVarNode.name.equals("this")) continue;
                    localVarNode.name = getNewName(dictionary.getValue(), prefix.getValue());
                }
            });
        });
    }
}
