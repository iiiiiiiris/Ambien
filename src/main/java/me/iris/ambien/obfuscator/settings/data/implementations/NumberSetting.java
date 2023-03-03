package me.iris.ambien.obfuscator.settings.data.implementations;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.settings.data.Setting;

public class NumberSetting<N extends Number> extends Setting<N> {
    @Getter
    @Setter
    private N value;

    public NumberSetting(String name, N value) {
        super(name);
        this.value = value;
    }
}
