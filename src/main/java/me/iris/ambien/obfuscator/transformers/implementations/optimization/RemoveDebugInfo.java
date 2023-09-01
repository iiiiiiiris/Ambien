package me.iris.ambien.obfuscator.transformers.implementations.optimization;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.exceptions.SettingConflictException;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.Crasher;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@TransformerInfo(
        name = "remove-debug-info",
        category = Category.OPTIMIZATION,
        stability = Stability.STABLE,
        description = "Removes information from classes related to debugging."
)
public class RemoveDebugInfo extends Transformer {
    public final BooleanSetting removeInnerClasses = new BooleanSetting("remove-inner-classes", false);
    public final StringSetting sourceDebug = new StringSetting("source-debug", "");
    public final StringSetting sourceFile = new StringSetting("source-file", "");
    public final StringSetting signature = new StringSetting("signature", "");

    @Override
    public void transform(JarWrapper wrapper) {
        // Check for settings conflicts
        if (Ambien.get.transformerManager.getTransformer("crasher").isEnabled() && Crasher.junkSignatures.isEnabled())
            throw new SettingConflictException("The remove-debug-info transformer can't be used while using the junk-signatures setting in the crasher transformer. (Disable one)");

        // Remove debug info from classes
        getClasses(wrapper).forEach(classWrapper -> {
            final ClassNode classNode = classWrapper.getNode();
            classNode.sourceDebug = sourceDebug.getValue().equals("keep") ? classNode.sourceDebug : sourceDebug.getValue();
            classNode.sourceFile = sourceFile.getValue().equals("keep") ? classNode.sourceFile : sourceFile.getValue();
            classNode.signature = signature.getValue().equals("keep") ? classNode.signature : signature.getValue();
            if (signature.getValue().equals("crash")) {
                classNode.signature = randomSignature();
            }

            if (removeInnerClasses.isEnabled())
                classNode.innerClasses = new ArrayList<>();
        });
    }

    private String randomSignature() {
        int i = ThreadLocalRandom.current().nextInt(0, 4);
        switch (i) {
            case 1 -> {
                return "[I";
            }
            case 2 -> {
                return "[Z";
            }
            case 3 -> {
                return "[J";
            }
            default -> {
                return "[B";
            }

        }
    }
}
