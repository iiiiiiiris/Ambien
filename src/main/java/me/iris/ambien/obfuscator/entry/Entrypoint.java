package me.iris.ambien.obfuscator.entry;

import com.beust.jcommander.JCommander;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.Settings;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.io.File;
import java.io.IOException;

public class Entrypoint {
    public static void main(String[] args) throws IOException {
        // Parse arguments
        final Args ambienArgs = new Args();
        final JCommander commander = JCommander.newBuilder().addObject(ambienArgs).build();
        commander.parse(args);

        // Print help
        if (ambienArgs.help || args.length == 0) {
            commander.usage();
            return;
        }

        // Initialize managers
        Ambien.get.init(ambienArgs.noVersionCheck);

        // Create settings file if one wasn't provided or load provided settings from arg
        if (ambienArgs.createConfig) {
            Settings.create();
            return;
        } else
            Settings.load(new File(ambienArgs.configLocation));

        // Import specified jar
        final JarWrapper wrapper = new JarWrapper().from(new File(Ambien.get.inputJar));

        // Transform jar
        final JarWrapper transformedWrapper = Ambien.get.transform(wrapper, ambienArgs.experimentalTransformers);

        // Export specified jar
        transformedWrapper.to();

        // Debugging
        Ambien.LOGGER.debug("finished obfuscation");
    }
}
