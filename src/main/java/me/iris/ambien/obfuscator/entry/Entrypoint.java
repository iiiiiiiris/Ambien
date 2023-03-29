package me.iris.ambien.obfuscator.entry;

import com.beust.jcommander.JCommander;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.Settings;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.io.File;
import java.io.IOException;

public class Entrypoint {
    public static void main(String[] args) throws IOException {
        // print ascii
        System.out.println("          :::       :::   :::   ::::::::: ::::::::::: :::::::::: ::::    ::: ");
        System.out.println("       :+: :+:    :+:+: :+:+:  :+:    :+:    :+:     :+:        :+:+:   :+:  ");
        System.out.println("     +:+   +:+  +:+ +:+:+ +:+ +:+    +:+    +:+     +:+        :+:+:+  +:+   ");
        System.out.println("   +#++:++#++: +#+  +:+  +#+ +#++:++#+     +#+     +#++:++#   +#+ +:+ +#+    ");
        System.out.println("  +#+     +#+ +#+       +#+ +#+    +#+    +#+     +#+        +#+  +#+#+#     ");
        System.out.println(" #+#     #+# #+#       #+# #+#    #+#    #+#     #+#        #+#   #+#+#      ");
        System.out.println("###     ### ###       ### ######### ########### ########## ###    ####       ");

        // Parse arguments
        final Args ambienArgs = new Args();
        final JCommander commander = JCommander.newBuilder().addObject(ambienArgs).build();
        commander.parse(args);

        // Print help
        if (ambienArgs.help || args.length == 0) {
            commander.usage();
            return;
        }

        // Debugging
        Ambien.LOGGER.info("Parsed arguments.");

        try {
            // Initialize managers
            Ambien.get.init(ambienArgs.noVersionCheck);

            // Get transformer info
            if (ambienArgs.about != null) {
                // Get the transformer
                final Transformer transformer = Ambien.get.transformerManager.getTransformer(ambienArgs.about);

                // Make sure it exists
                if (transformer == null) {
                    Ambien.LOGGER.info("No transformer found with the name \"{}\"", ambienArgs.about);
                    return;
                }

                // Print info about the transformer
                Ambien.LOGGER.info("Info for transformer \"{}\"", transformer.getName());
                Ambien.LOGGER.info("Category: {}", transformer.getCategory().toString());
                Ambien.LOGGER.info("Stability: {}", transformer.getStability().toString());
                Ambien.LOGGER.info("Description: {}", transformer.getDescription());
                return;
            }

            // Create settings file if one wasn't provided or load provided settings from arg
            if (ambienArgs.createConfig) {
                Settings.create();
                return;
            } else
                Settings.load(new File(ambienArgs.configLocation));

            // Import specified jar
            JarWrapper wrapper = new JarWrapper().from(new File(Ambien.get.inputJar));

            // Import libraries
            if (!Ambien.get.libraries.isEmpty()) {
                for (final String lib : Ambien.get.libraries) {
                    wrapper = wrapper.importLibrary(lib);
                }
            }

            // Transform jar
            final JarWrapper transformedWrapper = Ambien.get.transform(wrapper, ambienArgs.experimentalTransformers);

            // Export specified jar
            transformedWrapper.to();

            // Debugging
            Ambien.LOGGER.debug("finished obfuscation");
        } catch (Exception e) {
            final String javaVersion = System.getProperty("java.version");
            final String javaVendor = System.getProperty("java.vendor");

            // print environment info
            Ambien.LOGGER.error("Exception thrown: {}", e.getMessage());
            Ambien.LOGGER.error("JVM: {}", javaVersion);
            Ambien.LOGGER.error("Vendor: {}", javaVendor);
            Ambien.LOGGER.error("Args: {}", StringUtil.build(args));

            // check for basic fixes
            if (ambienArgs.experimentalTransformers)
                Ambien.LOGGER.info("This exception may have been caused by an experimental transformer. (Run Ambien without the experimental arg)");
            if (!javaVersion.startsWith("1.8"))
                Ambien.LOGGER.info("It is recommended to use Java 8.");

            e.printStackTrace();
        }
    }
}
