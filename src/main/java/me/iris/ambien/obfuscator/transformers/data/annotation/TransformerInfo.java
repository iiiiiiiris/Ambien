package me.iris.ambien.obfuscator.transformers.data.annotation;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TransformerInfo {
    String name();

    Category category();
    Stability stability();
    Ordinal ordinal() default Ordinal.STANDARD;
    String description() default "No description provided.";

    boolean enabledByDefault() default false;
}
