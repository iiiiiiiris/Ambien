package me.iris.ambien.obfuscator.entry;

import com.beust.jcommander.Parameter;

public class Args {
    @Parameter(
            names = {"-h", "-help", "-usage"},
            description = "Shows arguments",
            help = true
    )
    public boolean help;

    @Parameter(
            names = {"-about", "--about-transformer", "-info", "--transformer-info"},
            description = "Gets the information about a specified transformer.",
            arity = 1
    )
    public String about;

    @Parameter(
            names = {"--create-config"},
            description = "Creates a new config"
    )
    public boolean createConfig = false;

    @Parameter(
            names = {"-config", "-cfg", "-settings"},
            arity = 1,
            description = "Location of your config file"
    )
    public String configLocation;

    @Parameter(
            names = {"--no-version-check", "--ignore-version-check"},
            description = "Run Ambien without checking for a new version"
    )
    public boolean noVersionCheck = false;

    @Parameter(
            names = {"-experimental", "--experimental-features", "--experimental-transformers"},
            description = "Allows for experimental transformers to run, you shouldn't use this if you don't know what you're doing."
    )
    public boolean experimentalTransformers = false;
}
