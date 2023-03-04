package me.iris.ambien.obfuscator;

import me.iris.ambien.obfuscator.settings.Settings;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.io.File;
import java.io.IOException;

public class Entrypoint {
    public static void main(String[] args) throws IOException {
        // Initialize managers
        Ambien.get.init(args);

        // Create settings file if one wasn't provided or load provided settings from arg
        if (args.length == 0)
            Settings.create();
        else
            Settings.load(new File(args[0]));

        // Import specified jar
        final JarWrapper wrapper = new JarWrapper().from(new File(Ambien.get.inputJar));

        // Transform jar
        final JarWrapper transformedWrapper = Ambien.get.transform(wrapper);

        // Export specified jar
        transformedWrapper.to();
    }
}
