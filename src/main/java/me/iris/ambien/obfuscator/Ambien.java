package me.iris.ambien.obfuscator;

import com.google.gson.JsonObject;
import me.iris.ambien.obfuscator.transformers.TransformerManager;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.WebUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ambien {
    public static final Ambien get = new Ambien();

    public static final Logger LOGGER = LoggerFactory.getLogger("Ambien");
    public static final String VERSION = "1.1.0", CLASSIFIER = "beta";

    public TransformerManager transformerManager;

    public String inputJar, outputJar;
    public final List<String> excludedClasses = new ArrayList<>();

    public void init(String[] args) {
        // Check for new version
        LOGGER.info("Ambien | {}", VERSION);
        checkVersion();

        // Initialize transformers
        transformerManager = new TransformerManager();
    }

    private void checkVersion() {
        try {
            final JsonObject obj = WebUtil.requestJsonObject("https://raw.githubusercontent.com/iiiiiiiris/Ambien/main/web/version.json");
            final String newestVersion = obj.get("version").getAsString();
            final String newestClassifier = obj.get("classifier").getAsString();
            if (!VERSION.equals(newestVersion)) {
                LOGGER.warn("!!! You are not using the newest version of Ambien !!!");
                LOGGER.info("Your version: {}-{}", VERSION, CLASSIFIER);
                LOGGER.info("Latest version: {}-{}", newestVersion, newestClassifier);
            } else
                LOGGER.info("You are using the latest version of Ambien.");
        } catch (IOException e) {
            LOGGER.error("Failed to check version.", e);
        }
    }

    public JarWrapper transform(JarWrapper wrapper) {
        for (Transformer transformer : transformerManager.getTransformers()) {
            if (!transformer.isEnabled()) continue;
            transformer.transform(wrapper);
        }

        return wrapper;
    }
}
