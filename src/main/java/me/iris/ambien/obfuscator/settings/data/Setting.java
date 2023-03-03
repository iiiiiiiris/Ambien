package me.iris.ambien.obfuscator.settings.data;

public class Setting<O> {
    private final String name;

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
