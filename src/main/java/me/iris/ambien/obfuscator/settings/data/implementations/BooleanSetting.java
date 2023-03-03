package me.iris.ambien.obfuscator.settings.data.implementations;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.settings.data.Setting;

public class BooleanSetting extends Setting<Boolean> {
    @Getter
    @Setter
    private boolean enabled;

    public BooleanSetting(String name, boolean enabled) {
        super(name);
    }
}
