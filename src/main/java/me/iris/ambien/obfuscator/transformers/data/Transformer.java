package me.iris.ambien.obfuscator.transformers.data;

import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.settings.data.Setting;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Transformer implements Opcodes {
    private final String name = getClass().getAnnotation(TransformerInfo.class).name();
    private final Category category = getClass().getAnnotation(TransformerInfo.class).category();
    private final boolean enabledByDefault = getClass().getAnnotation(TransformerInfo.class).enabledByDefault();

    protected final BooleanSetting enabled = new BooleanSetting("enabled", enabledByDefault);

    public abstract void transform(JarWrapper wrapper);

    public String getName() {
        return name;
    }

    public List<Setting<?>> getSettings() {
        final List<Setting<?>> settings = new ArrayList<>();
        for (Field f : getClass().getFields()) {
            try {
                final Object fieldObj = f.get(this);
                if (fieldObj instanceof Setting)
                    settings.add((Setting<?>)fieldObj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return settings;
    }

    public boolean isEnabled() {
        return enabled.isEnabled();
    }

    public void setEnabled(boolean _enabled) {
        enabled.setEnabled(_enabled);
    }
}
