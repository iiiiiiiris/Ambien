package me.iris.ambien.obfuscator.transformers.implementations.packaging;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

@TransformerInfo(
        name = "aggressive-compression",
        category = Category.PACKAGING
)
public class AggressiveCompression extends Transformer {
    @Override
    public void transform(JarWrapper wrapper) {
        // Transformation takes place during exportation
    }
}
