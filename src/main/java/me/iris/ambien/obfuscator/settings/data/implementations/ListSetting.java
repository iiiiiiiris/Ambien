package me.iris.ambien.obfuscator.settings.data.implementations;

import me.iris.ambien.obfuscator.settings.data.Setting;

import java.util.List;

public class ListSetting extends Setting<List<String>> {
    private final List<String> options;

    public ListSetting(String name, List<String> options) {
        super(name);
        this.options = options;
    }

    public List<String> getOptions() {
        return options;
    }
}
