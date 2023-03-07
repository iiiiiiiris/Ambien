package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.transformers.data.Category;
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
        category = Category.MISCELLANEOUS
)
public class LocalVariableRenamer extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            classWrapper.getTransformableMethods().forEach(methodNode -> {
                for (Object localVarObj : methodNode.localVariables) {
                    final LocalVariableNode localVarNode = (LocalVariableNode)localVarObj;
                    if (localVarNode.name.equals("this")) continue;
                    localVarNode.name = StringUtil.randomString(MathUtil.randomInt(10, 50));
                }
            });
        });
    }
}
