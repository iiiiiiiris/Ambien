package me.iris.ambien.obfuscator.settings.data.implementations;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.settings.data.Setting;

public class StringSetting extends Setting<String> {
    @Getter
    @Setter
    private String value;

    public StringSetting(String name, String value) {
        super(name);
        this.value = value;
    }
}
