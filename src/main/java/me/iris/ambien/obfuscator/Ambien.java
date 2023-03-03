package me.iris.ambien.obfuscator;

import me.iris.ambien.obfuscator.transformers.TransformerManager;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ambien {
    public static final Ambien get = new Ambien();

    public static final Logger LOGGER = LoggerFactory.getLogger("Ambien");
    public static final String VERSION = "1.0.0";

    public TransformerManager transformerManager;

    public String inputJar, outputJar;

    public void init(String[] args) {
        // TODO: Check for version
        LOGGER.info("Ambien | {}", VERSION);

        // Initialize transformers
        transformerManager = new TransformerManager();
    }

    public JarWrapper transform(JarWrapper wrapper) {
        for (Transformer transformer : transformerManager.getTransformers()) {
            if (!transformer.isEnabled()) continue;
            transformer.transform(wrapper);
        }

        return wrapper;
    }
}
