package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

/**
 * Appends a forward slash to the name of all class files within the jar.
 */
@TransformerInfo(
        name = "folder-classes",
        category = Category.PACKAGING,
        stability = Stability.STABLE
)
public class FolderClasses extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        // Transformation takes place when exporting
    }
}
