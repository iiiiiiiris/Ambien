package me.iris.ambien.obfuscator.settings.data.implementations;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.settings.data.Setting;

import java.util.List;

public class ListSetting extends Setting<List<String>> {
    @Getter
    private final List<String> options;

    @Getter
    @Setter
    private boolean optionsCleared;

    public ListSetting(String name, List<String> options) {
        super(name);
        this.options = options;
    }
}
