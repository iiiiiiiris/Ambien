package me.iris.ambien.obfuscator.transformers.data;

import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.settings.data.Setting;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Transformer implements Opcodes {
    private final String name = getClass().getAnnotation(TransformerInfo.class).name();

    private final Category category = getClass().getAnnotation(TransformerInfo.class).category();
    private final Ordinal ordinal = getClass().getAnnotation(TransformerInfo.class).ordinal();

    private final boolean enabledByDefault = getClass().getAnnotation(TransformerInfo.class).enabledByDefault();
    public final BooleanSetting enabled = new BooleanSetting("enabled", enabledByDefault);
    public final ListSetting excludedClasses = new ListSetting("excluded-classes", new ArrayList<>());

    public abstract void transform(JarWrapper wrapper);

    protected List<ClassWrapper> getClasses(JarWrapper wrapper) {
        return wrapper.getClasses().stream().filter(classWrapper -> !excludedClasses.getOptions().contains(classWrapper.getNode().name)).collect(Collectors.toList());
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

        // reverse list so the setting from this class are first
        Collections.reverse(settings);
        return settings;
    }

    public String getName() {
        return name;
    }

    public Ordinal getOrdinal() {
        return ordinal;
    }

    public boolean isEnabled() {
        return enabled.isEnabled();
    }

    public void setEnabled(boolean _enabled) {
        enabled.setEnabled(_enabled);
    }
}
