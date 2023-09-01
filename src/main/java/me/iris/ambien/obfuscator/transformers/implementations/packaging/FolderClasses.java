package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
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
        stability = Stability.STABLE,
        description = "Appends a forward slash to the name of all class files within the jar. This will cause some decompilers to ignore the class, thinking it's a directory."
)
public class FolderClasses extends Transformer {
    public static final BooleanSetting folderResources = new BooleanSetting("resources", false);
    public static final BooleanSetting folderClasses = new BooleanSetting("classes", false);
    @Override
    public void transform(JarWrapper wrapper) {
        // Transformation takes place when exporting
    }
}
