package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.LocalVariableNode;

/**
 * Renames local variables in methods to random characters
 */
@TransformerInfo(
        name = "local-variable-renamer",
        category = Category.MISCELLANEOUS,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Renames local variables in methods to random characters"
)
public class LocalVariableRenamer extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodWrapper -> {
                if (!methodWrapper.hasLocalVariables()){
                    return;
                }
                for (Object localVarObj : methodWrapper.getNode().localVariables) {
                    //noinspection CastCanBeRemovedNarrowingVariableType
                    final LocalVariableNode localVarNode = (LocalVariableNode)localVarObj;
                    if (localVarNode.name.equals("this")) continue;
                    localVarNode.name = StringUtil.randomString(MathUtil.randomInt(10, 50));
                }
            });
        });
    }
}
