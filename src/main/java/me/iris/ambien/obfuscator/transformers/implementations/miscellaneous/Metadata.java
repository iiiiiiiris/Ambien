package me.iris.ambien.obfuscator.transformers.implementations.miscellaneous;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

@TransformerInfo(
        name = "metadata",
        category = Category.MISCELLANEOUS,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Edit files' metadata."
)
public class Metadata extends Transformer {
    public static final BooleanSetting corruptTime = new BooleanSetting("time", false);
    public static final StringSetting setComment = new StringSetting("comment", "");
    @Override
    public void transform(JarWrapper wrapper) {

    }
}
