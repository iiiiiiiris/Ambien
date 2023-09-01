package me.iris.ambien.obfuscator;

import com.google.gson.JsonObject;
import me.iris.ambien.obfuscator.transformers.ExclusionManager;
import me.iris.ambien.obfuscator.transformers.TransformerManager;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.WebUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ambien {
    // GLOBALS
    public static final Ambien get = new Ambien();
    public static final Logger LOGGER = LoggerFactory.getLogger("Ambien");
    public static final String VERSION = "1.5.0",
            CLASSIFIER = "beta";

    // MANAGERS
    public TransformerManager transformerManager;
    public ExclusionManager exclusionManager;

    // SETTINGS
    public String inputJar,
            outputJar;
    public String naming;
    public final List<String> excludedClasses = new ArrayList<>(),
            libraries = new ArrayList<>();
    public boolean removeExcludeAnnotations;

    private JarWrapper jarWrapper;

    public void initializeTransformers(boolean ignoreVersionCheck) {
        // Check for new version
        LOGGER.info("Ambien | {}", VERSION);
        if (!ignoreVersionCheck)
            checkVersion();

        // Initialize transformers
        LOGGER.info("Initializing transformer manager...");
        transformerManager = new TransformerManager();
    }

    public void preTransform() throws IOException {
        // Parse input jar
        LOGGER.info("Parsing input jar...");
        jarWrapper = new JarWrapper().from(new File(inputJar));

        // Initialize exclusion manager
        LOGGER.info("Initializing exclusion manager...");
        exclusionManager = new ExclusionManager(jarWrapper);

        // Import libraries
        LOGGER.info("Importing libraries...");
        if (!libraries.isEmpty()) {
            for (final String lib : libraries) {
                jarWrapper = jarWrapper.importLibrary(lib);
                LOGGER.info("Imported library: {}", lib);
            }
        }
    }

    public void transformAndExport(final boolean experimentalTransformers) throws IOException {
        // Transform jar
        LOGGER.info("Transforming jar...");
        final JarWrapper transformedWrapper = transform(jarWrapper, experimentalTransformers);

        // Remove exclude annotations (if setting is enabled)
        if (removeExcludeAnnotations)
            exclusionManager.removeExcludeAnnotations(transformedWrapper);

        // Export specified jar
        LOGGER.info("Exporting jar...");
        transformedWrapper.to();
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
                LOGGER.info("You can download the latest version here: https://github.com/iiiiiiiris/Ambien/releases/latest");
            } else
                LOGGER.info("You are using the latest version of Ambien.");
        } catch (IOException e) {
            LOGGER.error("Failed to check version.", e);
        }
    }

    private JarWrapper transform(JarWrapper wrapper, boolean experimentalTransformers) {
        for (Transformer transformer : transformerManager.getTransformers()) {
            if (!transformer.isEnabled()) continue;
            if (!experimentalTransformers && transformer.getStability().equals(Stability.EXPERIMENTAL)) continue;
            LOGGER.info("Executing transformer: {}", transformer.getName());
            transformer.transform(wrapper);
        }

        return wrapper;
    }
}
